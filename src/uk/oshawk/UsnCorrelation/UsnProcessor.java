package uk.oshawk.UsnCorrelation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.FileSystem;
import org.sleuthkit.datamodel.Image;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.SleuthkitCase.CaseDbQuery;
import org.sleuthkit.datamodel.TskCoreException;
import org.sleuthkit.datamodel.TskData;
import org.sleuthkit.datamodel.Volume;
import org.sleuthkit.datamodel.VolumeSystem;

public enum UsnProcessor {
    // A singleton used to get $UsnJrnl UsnFiles and AbstractFiles.
    
    INSTANCE();
    
    private final Map<Long, UsnFile> usnFiles;

    private UsnProcessor() {
        // Instantiates the singleton.
        
        usnFiles = new HashMap<>();
    }
    
    public synchronized UsnFile getUsnFile(AbstractFile usn) {
        // Gets the UsnFile for a $UsnJrnl AbstractFile.
        // Processes the file into entries if it hasn't already been done.
        // Syncronised to avoid duplicate processing.
        
        long hash = usn.getId();
        
        // Checks if $UsnJrnl has already been processed.
        UsnFile usnFile = usnFiles.get(hash);
        
        // If the $UnsJrnl has been processed, return the processed UsnFile.
        if (usnFile != null) {
            return usnFile;
        }
        
        // Process the $UsnJrnl.
        usnFile = new UsnFile(usn);
        
        // Add the $UsnJrnl to the map of already proecessed files.
        usnFiles.put(hash, usnFile);
        
        // Return the processed UsnFile.
        return usnFile;
    }
    
    public static AbstractFile getUsnForFileSystem(long fileSystemId) {
        // Gets the $UsnJrnl AbstractFile for a file system ID using SQL.
        
        SleuthkitCase sleautkitCase;
        try {
            sleautkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        // Use a manual SQL query to get the $UsnJrnl file.
        List<AbstractFile> usns;
        try {
            usns = sleautkitCase.findAllFilesWhere(String.format("fs_obj_id = %d AND name = '$UsnJrnl:$J' AND parent_path = '/$Extend/'", fileSystemId));
        } catch (TskCoreException e) {
            return null;
        }
        
        // If there are no $UsnJrnl files, return null.
        if (usns.isEmpty()) {
            if (UsnUtil.TEST_LOGGING) {
                Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F21] $UsnJrnl does not exist for file system %d.", fileSystemId));
            }
            
            return null;
        }
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F21] $UsnJrnl exists for file system %d.", fileSystemId));
        }
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F22] Got $UsnJrnl for file system %d.", fileSystemId));
        }
        
        // Return the first found $UsnJrnl AbstractFIle.
        return usns.get(0);
    }
    
    public static AbstractFile getUsnForFile(AbstractFile file) {
        // Gets the $UsnJrnl file associated with an AbstractFile using SQL and getUsnForFileSystem.
        
        SleuthkitCase sleautkitCase;
        try {
            sleautkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        // Use a manual SQL query to get the file system ID and file system type for the file.
        long fileSystemId;
        int fileSystemType;
        try (CaseDbQuery query = sleautkitCase.executeQuery(String.format("SELECT fs_obj_id, fs_type FROM tsk_files INNER JOIN tsk_fs_info ON fs_obj_id = tsk_fs_info.obj_id WHERE tsk_files.obj_id = %d", file.getId()))) {
            ResultSet results = query.getResultSet();
            
            // If there are no results, return null.
            if (!results.next()) {
                return null;
            }
            
            // Retrieve the file system ID and file system type.
            fileSystemId = results.getLong("fs_obj_id");
            fileSystemType = results.getInt("fs_type");
        } catch (TskCoreException | SQLException e) {
            return null;
        }
        
        
        // If the file system type is not NTFS, return null.
        if (fileSystemType != TskData.TSK_FS_TYPE_ENUM.TSK_FS_TYPE_NTFS.ordinal()) {
            return null;
        }
        
        // Return the $UsnJrnl AbstractFile for the file system ID.
        return getUsnForFileSystem(fileSystemId);
    }
    
    public static List<AbstractFile> getUsnsForDataSource(Content dataSource) {
        // Gets the $UsnJrnl AbstractFiles for a data source, recursing if needed.
        // This algorithm is documented in the report.
        
        List<AbstractFile> usns = new ArrayList<>();
        if (dataSource instanceof Image || dataSource instanceof VolumeSystem || dataSource instanceof Volume) {  // If the data source is an image, volume system or volume, recursivley call for its children.
            try {
                for (Content child: dataSource.getChildren()) {
                    usns.addAll(getUsnsForDataSource(child));
                }
            } catch (TskCoreException e) {}
        } else if (dataSource instanceof FileSystem) {  // If the data source is a file system, extract the $UsnJrnl files.
            AbstractFile usn = getUsnForFileSystem(((FileSystem)dataSource).getId());
            if (usn != null) {
                usns.add(usn);
            }
        }
        
        return usns;
    }
}
