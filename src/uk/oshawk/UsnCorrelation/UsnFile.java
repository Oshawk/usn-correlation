package uk.oshawk.UsnCorrelation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

public class UsnFile {
    // Represents a $UsnJrnl file, comprising many entries.
    
    private final long PAGE_SIZE = 0x1000;
    
    public final Long fileSystemId;
    public final List<UsnEntry> entries;
    public final Map<Long, List<UsnEntry>> entriesByFileIndex;
    public final Map<Long, List<UsnEntry>> entriesByFileParentIndex;
      
    public UsnFile(AbstractFile usn, boolean testing) {
        // Avoid the need for an Autopsy database by intercepting avoiding getFileSystemIdForFile when testing.
        if (testing) {
            fileSystemId = 0L;
        } else {
            fileSystemId = getFileSystemIdForFile(usn);
        }
            
        long position = 0;
        long lastGoodPagePosition = 0;
        
        // Scratch space for field extraction.
        ByteBuffer buffer = ByteBuffer.allocate(0x250);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // Iterates until the end of the file is reached.
        int size;
        short majorVersion;
        entries = new ArrayList<>();
        entriesByFileIndex = new HashMap<>();
        entriesByFileParentIndex = new HashMap<>();
        while (position < usn.getSize()) {
            // Attempts to read into the buffer. Breaking (EOF) if unsuccessful.
            try {
                usn.read(buffer.array(), position, 6);
            } catch (TskCoreException e) {
                break;
            }
            
            if (UsnUtil.TEST_LOGGING) {
                Logger.getLogger("uk.oshawk.UsnCorrelation").info("[F22] Read $UsnJrnl check headers.");
            }
            
            // Reads the first two headers.
            buffer.position(0);
            size = buffer.getInt();
            majorVersion = buffer.getShort();
            
            // Uses the first two headers to determine if this is a valid entry.
            // If not, go to the next page and continue to the next iteration.
            if (size < 0x38 || size > 0x250 || majorVersion != 2) {
                lastGoodPagePosition += PAGE_SIZE;
                position = lastGoodPagePosition;
                continue;
            }
            
            // At a page boundry.
            if (position % PAGE_SIZE == 0) {
                lastGoodPagePosition = position;
            }
            
            // Create the entry.
            UsnEntry entry;
            try {
                entry = new UsnEntry(usn, position, buffer, fileSystemId);
            } catch (TskCoreException e) {
                break;
            }
            
            // Add the entry to the list of entries.
            entries.add(entry);
            
            // Add the list to the map of entries grouped by index.
            if (!entriesByFileIndex.containsKey(entry.getFileIndex())) {
                entriesByFileIndex.put(entry.getFileIndex(), new ArrayList<UsnEntry>());
            }
            entriesByFileIndex.get(entry.getFileIndex()).add(entry);
            
            // Add the list to the map of entries grouped by parent index.
            if (!entriesByFileParentIndex.containsKey(entry.getParentFileIndex())) {
                entriesByFileParentIndex.put(entry.getParentFileIndex(), new ArrayList<UsnEntry>());
            }
            entriesByFileParentIndex.get(entry.getParentFileIndex()).add(entry);
            
            // Increments the position by the size of the entry.
            position += size;
        }
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F19] $UsnJrnl processed for file system %d (entries=%d).", fileSystemId, entries.size()));
        }
    }
    
    public UsnFile(AbstractFile usn) {
        // By default not testing.
        
        this(usn, false);
    }
    
    public static Long getFileSystemIdForFile(AbstractFile file) {
        // Gets the file system ID for an AbstractFile.
        // This functionality is avaliable in new TSK versions without using SQL but Autopsy hasn't adopted it yet/
        
        SleuthkitCase sleautkitCase;
        try {
            sleautkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        // Manually queries Autopsy's database with the AbstractFile's ID to get its file system ID.
        try (SleuthkitCase.CaseDbQuery query = sleautkitCase.executeQuery(String.format("SELECT fs_obj_id FROM tsk_files WHERE tsk_files.obj_id = %d", file.getId()))) {
            ResultSet results = query.getResultSet();
            
            // No results, likely an invalid file.
            if (!results.next()) {
                return null;
            }
            
            // Retrieves the file system ID.
            long id = results.getLong("fs_obj_id");
            
            if (UsnUtil.TEST_LOGGING) {
                Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F18] $UsnJrnl file system identified (id=%d).", id));
            }
            
            return id;
        } catch (TskCoreException | SQLException e) {
            return null;
        }
    }
    
    private List<UsnEntry> getEntries(long index, long sequenceNumber, boolean child) {
        // Gets the parent or child entries associated with an index and sequence number.
        
        ArrayList<UsnEntry> entries = new ArrayList<>();
        
        // Not a valid file, return no entries.
        if (index == 0 && sequenceNumber == 0) {
            return entries;
        }
        
        // For efficiency, gets the index group and further filters it by the sequence number.
        if (child && entriesByFileIndex.containsKey(index)) {
            for (UsnEntry entry: entriesByFileIndex.get(index)) {
                if (entry.getFileSequenceNumber() == sequenceNumber) {
                    entries.add(entry);
                }
            }
        }
        
        // For efficiency, gets the patent index group and further filters it by the sequence number.
        if (!child && entriesByFileParentIndex.containsKey(index)) {
            for (UsnEntry entry: entriesByFileParentIndex.get(index)) {
                if (entry.getParentFileSequenceNumber() == sequenceNumber) {
                    entries.add(entry);
                }
            }
        }
        
        return entries;
    }
    
    private List<UsnEntry> getEntries(AbstractFile file, boolean child) {
        // Gets the parent or file entries associated with an AbstractFile.
        
        long index = file.getMetaAddr();
        long sequenceNumber = file.getMetaSeq();
        
        return getEntries(index, sequenceNumber, child);
    }
    
    public List<UsnEntry> getChildEntries(long index, long sequenceNumber) {
        // Gets the child entries associated with an index and sequence number.
        
        return getEntries(index, sequenceNumber, true);
    }
    
    public List<UsnEntry> getChildEntries(AbstractFile file) {
        // Gets the child entries associated with an AbstractFIle.
    
        return getEntries(file, true);
    }
    
    public List<UsnEntry> getParentEntries(long index, long sequenceNumber) {
        // Gets the parent entries associated with an index and sequence number.
        
        return getEntries(index, sequenceNumber, false);
    }
    
    public List<UsnEntry> getParentEntries(AbstractFile file) {
        // Gets the parent entries associated with an AbstractFIle.
        
        return getEntries(file, false);
    }
}
