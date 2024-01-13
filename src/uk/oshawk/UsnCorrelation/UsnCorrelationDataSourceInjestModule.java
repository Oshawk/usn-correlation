package uk.oshawk.UsnCorrelation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModule;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData.TSK_FS_ATTR_TYPE_ENUM;
import org.sleuthkit.datamodel.TskData.TSK_FS_NAME_FLAG_ENUM;

public class UsnCorrelationDataSourceInjestModule implements DataSourceIngestModule {
    // The ingest module used to recover information about deleted files.
    
    private IngestJobContext context;
    
    @Override
    public void startUp(IngestJobContext context) throws IngestModuleException {
        // Called before any data is processed.
        
        // The context is stored so that it can later be used to check if the ingest has been cancelled.
        this.context = context;
    }
    
    private class FileTree {
        // Facilitates building a file hierarchy using $UsnJrnl entries.
        
        public final long recordInformation;
        public String name;
        public boolean isFile;
        public FileTree parent;
        public final List<FileTree> children;

        FileTree(long recordInformation, String name, boolean isFile, FileTree parent) {
            // Creates a new node in the hierarchy, identified by the recod information.
            
            this.recordInformation = recordInformation;
            this.name = name;
            this.isFile = isFile;
            this.parent = parent;
            children = new ArrayList<>();
        }
    }
    
    private class ProgressBarWrapper {
        // Used to wrap the progress bar so that it can be advanced one unit at a time.
        
        private final DataSourceIngestModuleProgress progressBar;
        private int units;
        
        public ProgressBarWrapper(DataSourceIngestModuleProgress progressBar) {
            // Creates the progress bar wrapper, with an initial progress of 0.
            
            this.progressBar = progressBar;
            units = 0;
        }
        
        public void progress() {
            // Advances the wrapped progress bar by one unit.
            
            units += 1;
            this.progressBar.progress(units);
        }
    }
    
    private ProcessResult resolveFileTree(AbstractFile parentFile, long fileSystemId, Collection<FileTree> fileTreeFiles, ProgressBarWrapper progressBar) {
        // Recursivley resolves a $UsnJrnl file tree, integrating it with Autopsy's file hierarchy.
        // parentFile is the parent of fileTreeFiles.
        
        // Iterate over the child files.
        for (FileTree fileTreeFile: fileTreeFiles) {
            // Return if the user has requested to cancel.
            if (context.dataArtifactIngestIsCancelled()) {
                return ProcessResult.ERROR;
            }
            
            // Attempts get the Autopsy file associated with the $UsnJrnl file tree file.
            // TODO: Make this faster.
            AbstractFile file = UsnUtil.getFileFromRecordInformation(fileSystemId, fileTreeFile.recordInformation);
            
            if (file == null) {  // Autopsy does not know about the file, so add it as deleted.
                if (UsnUtil.TEST_LOGGING) {
                    Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F09] File does not exist (index=%d, sequence_number=%d).", UsnUtil.getRecordInformationIndex(fileTreeFile.recordInformation), UsnUtil.getRecordInformationSequenceNumber(fileTreeFile.recordInformation)));
                }
                
                SleuthkitCase sleautkitCase;
                try {
                    sleautkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
                } catch (NoCurrentCaseException e) {
                    return ProcessResult.ERROR;
                }
                
                try {
                    // Add the $UsnJrnl file tree file to Autopsy as a deleted file.
                    // TODO: Make this faster.
                    file = sleautkitCase.addFileSystemFile(parentFile.getDataSourceObjectId(), fileSystemId, fileTreeFile.name, UsnUtil.getRecordInformationIndex(fileTreeFile.recordInformation), UsnUtil.getRecordInformationSequenceNumber(fileTreeFile.recordInformation), TSK_FS_ATTR_TYPE_ENUM.TSK_FS_ATTR_TYPE_DEFAULT, 0, TSK_FS_NAME_FLAG_ENUM.UNALLOC, (short)0, 0, 0, 0, 0, 0, fileTreeFile.isFile, parentFile);
                    
                    if (UsnUtil.TEST_LOGGING) {
                        Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F10] Added deleted file (index=%d, sequence_number=%d).", UsnUtil.getRecordInformationIndex(fileTreeFile.recordInformation), UsnUtil.getRecordInformationSequenceNumber(fileTreeFile.recordInformation)));
                    }
                } catch (TskCoreException e) {
                    return ProcessResult.ERROR;
                }
                
                // Create an aretefact for the newly added Autopsy file, indicating how it was recovered.
                try {
                    file.newDataArtifact(UsnDerivedFileArtefact.getArtefactType(), Arrays.asList(
                        new BlackboardAttribute(UsnDerivedFileArtefact.getFilesystemIdAttributeType(), UsnUtil.NAME, fileSystemId),
                        new BlackboardAttribute(UsnDerivedFileArtefact.getIndexAttributeType(), UsnUtil.NAME, UsnUtil.getRecordInformationIndex(fileTreeFile.recordInformation)),
                        new BlackboardAttribute(UsnDerivedFileArtefact.getSequenceNumberAttributeType(), UsnUtil.NAME, UsnUtil.getRecordInformationSequenceNumber(fileTreeFile.recordInformation))
                    ));
                } catch (TskCoreException e) {
                    return ProcessResult.ERROR;
                }
            } else {  // Autopsy already knows about the file.
                if (UsnUtil.TEST_LOGGING) {
                    Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F09] File exists (index=%d, sequence_number=%d).", UsnUtil.getRecordInformationIndex(fileTreeFile.recordInformation), UsnUtil.getRecordInformationSequenceNumber(fileTreeFile.recordInformation)));
                }
            }
            
            // Advance the progress bar.
            progressBar.progress();
            
            // Recursivley call with:
            // - The found or newly added Autopsy file as the parent.
            // - The children of the $UsnJrnl file tree file as the children.
            ProcessResult resolveFileTreeResult = resolveFileTree(file, fileSystemId, fileTreeFile.children, progressBar);
            
            // If there is an error (likely the user has cancelled), return immediately.
            if (resolveFileTreeResult != ProcessResult.OK) {
                return resolveFileTreeResult;
            }
        }
        
        return ProcessResult.OK;
    }

    @Override
    public ProcessResult process(Content dataSource, DataSourceIngestModuleProgress progressBar) {
        // Called when a data source is added.
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F07] Processing data source (name=%s).", dataSource.getName()));
        }
        
        SleuthkitCase sleautkitCase;
        try {
            sleautkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return ProcessResult.ERROR;
        }
        
        // Iterates over all the $UsnJrnl files associated with the data source.
        List<AbstractFile> usns = UsnProcessor.getUsnsForDataSource(dataSource);
        for (AbstractFile usn: usns) {
            if (context.dataArtifactIngestIsCancelled()) {
                return ProcessResult.ERROR;
            }
            
            // This algorithm is explained in the report.
            // All $UsnJrnl entries are iterated over.
            // heads contain files with no known parent.
            // tree contanins all files.
            // Both are mapped to file record information (index and sequence number).
            HashMap<Long, FileTree> heads = new HashMap<>();
            HashMap<Long, FileTree> tree = new HashMap<>();
            UsnFile usnFile = UsnProcessor.INSTANCE.getUsnFile(usn);
            for (UsnEntry entry: usnFile.entries) {
                // Attempts to get the file and its parent from the tree.
                FileTree file = tree.get(entry.fileRecordInformation);
                FileTree parentFile = tree.get(entry.parentFileRecordInformation);
                
                if (parentFile == null) {  // No parent file. Add one with an empty name.
                    parentFile = new FileTree(entry.parentFileRecordInformation, "", false, null);
                    
                    heads.put(entry.parentFileRecordInformation, parentFile);
                    tree.put(entry.parentFileRecordInformation, parentFile);
                } else {  // Parent file. It has children so can no loger be a file.
                    parentFile.isFile = false;
                }
                
                if (file == null) {  // No file. Add one with the entry's name.
                    file = new FileTree(entry.fileRecordInformation, entry.name, true, parentFile);
                    
                    parentFile.children.add(file);
                   
                    tree.put(entry.fileRecordInformation, file);
                } else {  // File. Update reparent and update the name.
                    file.name = entry.name;
                    
                    if (file.parent != null) {
                        file.parent.children.remove(file);
                    }
                    
                    parentFile.children.add(file);
                    file.parent = parentFile;
                    
                    heads.remove(entry.fileRecordInformation);
                }
            }
            
            // Use the $OrphanFiles directory if Autopsy doesn't have a parent for one of the heads.
            List<AbstractFile> orphanFiles;
            try {
                orphanFiles = sleautkitCase.findAllFilesWhere(String.format("fs_obj_id = %d AND name = '$OrphanFiles' AND parent_path = '/'", usnFile.fileSystemId));
            } catch (TskCoreException e) {
                return ProcessResult.ERROR;
            }
            
            if (orphanFiles.isEmpty()) {
                return ProcessResult.ERROR;
            }
            
            if (UsnUtil.TEST_LOGGING) {
                Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F08] Built tree (nodes=%d, heads=%d).", tree.size(), heads.size()));
            }
            
            // Set the progress bar's size equal to the number of files in the tree.
            progressBar.switchToDeterminate(tree.size());
            
            // Recursively resolve the file tree.
            ProcessResult resolveFileTreeResult = resolveFileTree(orphanFiles.get(0), usnFile.fileSystemId, heads.values(), new ProgressBarWrapper(progressBar));
            
            // If there is an error (likely the user has cancelled), return immediately.
            if (resolveFileTreeResult != ProcessResult.OK) {
                return resolveFileTreeResult;
            }
        }
        
        return ProcessResult.OK;
    }
}
