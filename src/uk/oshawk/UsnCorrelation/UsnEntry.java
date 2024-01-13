package uk.oshawk.UsnCorrelation;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TimeUtilities;
import org.sleuthkit.datamodel.TskCoreException;

public class UsnEntry implements Comparable<UsnEntry> {
    // Represents an antry in a $UsnJrnl file.
    
    public final long position;
    public final Long fileSystemId;
    public final int size;
    public final short majorVersion;
    public final short minorVersion;
    public final long fileRecordInformation;
    public final long parentFileRecordInformation;
    public final long updateSequenceNumber;
    public final long updateTimestamp;
    public final int updateSourceFlags;
    public final int updateReasonFlags;
    public final int securityDescriptorID;
    public final int fileAttributeFlags;
    public final short nameSize;
    public final short nameOffset;
    public final String name;
    
    public static final int DEFAULT_DATA_OVERWRITE = 1 << 0;
    public static final int DEFAULT_DATA_EXTEND = 1 << 1;
    public static final int DEFAULT_DATA_TRUNCATE = 1 << 2;
    public static final int NAMED_DATA_OVERWRITE = 1 << 4;
    public static final int NAMED_DATA_EXTEND = 1 << 5;
    public static final int NAMED_DATA_TRUNCATE = 1 << 6;
    public static final int FILE_CREATE = 1 << 8;
    public static final int FILE_DELETE = 1 << 9;
    public static final int EA_CHANGE = 1 << 10;
    public static final int SECURITY_CHANGE = 1 << 11;
    public static final int RENAME_OLD_NAME = 1 << 12;
    public static final int RENAME_NEW_NAME = 1 << 13;
    public static final int INDEXABLE_CHANGE = 1 << 14;
    public static final int BASIC_INFORMATION_CHANGE = 1 << 15;
    public static final int HARD_LINK_CHANGE = 1 << 16;
    public static final int COMPRESSION_CHANGE = 1 << 17;
    public static final int ENCRYPTION_CHANGE = 1 << 18;
    public static final int OBJECT_ID_CHANGE = 1 << 19;
    public static final int REPARSE_POINT_CHANGE = 1 << 20;
    public static final int STREAM_CHANGE = 1 << 21;
    public static final int TRANSACTED_CHANGE = 1 << 22;
    public static final int INTEGRITY_CHANGE = 1 << 23;
    public static final int CLOSE = 1 << 31;
    
    UsnEntry(AbstractFile usnFile, long position, ByteBuffer buffer, Long fileSystemId) throws TskCoreException {
        // Creates the entry.
        // usnFile is the $UsnJrnl file.
        // position is the offset of the entry in the file.
        // buffer is some scratch space to help with processing.
        // fileSystemId is the ID of the file system, needed by various methods.
        
        this.position = position;
        this.fileSystemId = fileSystemId;
        
        // Read 60 bytes of $UsnJrnl from position into the buffer.
        usnFile.read(buffer.array(), this.position, 60);
        buffer.position(0);
        
        // Extracts the fields from the buffer.
        size = buffer.getInt();
        majorVersion = buffer.getShort();
        minorVersion = buffer.getShort();
        fileRecordInformation = buffer.getLong();
        parentFileRecordInformation = buffer.getLong();
        updateSequenceNumber = buffer.getLong();
        updateTimestamp = buffer.getLong();
        updateReasonFlags = buffer.getInt();
        updateSourceFlags = buffer.getInt();
        securityDescriptorID = buffer.getInt();
        fileAttributeFlags = buffer.getInt();
        nameSize = buffer.getShort();
        nameOffset = buffer.getShort();
        
        // Extracts the entry's name using the offset and size fields.
        usnFile.read(buffer.array(), this.position + nameOffset, nameSize);
        name = new String(buffer.array(), 0, nameSize, StandardCharsets.UTF_16LE);
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F22] Read $UsnJrnl entry (USN=%d).", updateSequenceNumber));
        }
    }
    
    
    public long getFileIndex() {
        // Gets the entry's index from its record information (index and sequence number combined).
        
        return UsnUtil.getRecordInformationIndex(fileRecordInformation);
    }
    
    public short getFileSequenceNumber() {
         // Gets the entry's sequence number from its record information (index and sequence number combined).
        
        return UsnUtil.getRecordInformationSequenceNumber(fileRecordInformation);
    }
    
    public long getParentFileIndex() {
         // Gets the entry's parent index from its parent record information (index and sequence number combined).
        
        return UsnUtil.getRecordInformationIndex(parentFileRecordInformation);
    }
    
    public short getParentFileSequenceNumber() {
        // Gets the entry's parent sequence number from its parent record information (index and sequence number combined).
        
        return UsnUtil.getRecordInformationSequenceNumber(parentFileRecordInformation);
    }
    
    public AbstractFile getFileFromRecordInformation(long fileSystemId, long recordInformation) {
        // A wrapper to facilitate unit testing since static methods can't be intercepted.
        
        return UsnUtil.getFileFromRecordInformation(fileSystemId, recordInformation);
    }
    
    public AbstractFile getFile() {
        // Gets the AbstractFile for the entry.
        
        return getFileFromRecordInformation(fileSystemId, fileRecordInformation);
    }
    
    public AbstractFile getParentFile() {
        // Gets the parent AbstractFile for rhe entry.
        
        return getFileFromRecordInformation(fileSystemId, parentFileRecordInformation);
    }
    
    public String getUpdateTimestampString() {
        // Gets the entry's timestamp as a string.
        
        // https://github.com/sleuthkit/sleuthkit/blob/master/tools/logicalimager/RegistryAnalyzer.cpp#L102
        return TimeUtilities.epochToTime(Long.divideUnsigned(updateTimestamp, 10000000L) - 11644473600L);
    }
    
    public String getUpdateReasonFlagsString() {
        // Gets the entry's update reason flags as a string.
        
        ArrayList<String> flags = new ArrayList<>();
        
        if ((updateReasonFlags & DEFAULT_DATA_OVERWRITE) != 0) flags.add("DEFAULT_DATA_OVERWRITE");
        if ((updateReasonFlags & DEFAULT_DATA_EXTEND) != 0) flags.add("DEFAULT_DATA_EXTEND");
        if ((updateReasonFlags & DEFAULT_DATA_TRUNCATE) != 0) flags.add("DEFAULT_DATA_TRUNCATE");
        if ((updateReasonFlags & NAMED_DATA_OVERWRITE) != 0) flags.add("NAMED_DATA_OVERWRITE");
        if ((updateReasonFlags & NAMED_DATA_EXTEND) != 0) flags.add("NAMED_DATA_EXTEND");
        if ((updateReasonFlags & NAMED_DATA_TRUNCATE) != 0) flags.add("NAMED_DATA_TRUNCATE");
        if ((updateReasonFlags & FILE_CREATE) != 0) flags.add("FILE_CREATE");
        if ((updateReasonFlags & FILE_DELETE) != 0) flags.add("FILE_DELETE");
        if ((updateReasonFlags & EA_CHANGE) != 0) flags.add("EA_CHANGE");
        if ((updateReasonFlags & SECURITY_CHANGE) != 0) flags.add("SECURITY_CHANGE");
        if ((updateReasonFlags & RENAME_OLD_NAME) != 0) flags.add("RENAME_OLD_NAME");
        if ((updateReasonFlags & RENAME_NEW_NAME) != 0) flags.add("RENAME_NEW_NAME");
        if ((updateReasonFlags & INDEXABLE_CHANGE) != 0) flags.add("INDEXABLE_CHANGE");
        if ((updateReasonFlags & BASIC_INFORMATION_CHANGE) != 0) flags.add("BASIC_INFORMATION_CHANGE");
        if ((updateReasonFlags & HARD_LINK_CHANGE) != 0) flags.add("HARD_LINK_CHANGE");
        if ((updateReasonFlags & COMPRESSION_CHANGE) != 0) flags.add("COMPRESSION_CHANGE");
        if ((updateReasonFlags & ENCRYPTION_CHANGE) != 0) flags.add("ENCRYPTION_CHANGE");
        if ((updateReasonFlags & OBJECT_ID_CHANGE) != 0) flags.add("OBJECT_ID_CHANGE");
        if ((updateReasonFlags & REPARSE_POINT_CHANGE) != 0) flags.add("REPARSE_POINT_CHANGE");
        if ((updateReasonFlags & STREAM_CHANGE) != 0) flags.add("STREAM_CHANGE");
        if ((updateReasonFlags & TRANSACTED_CHANGE) != 0) flags.add("TRANSACTED_CHANGE");
        if ((updateReasonFlags & INTEGRITY_CHANGE) != 0) flags.add("INTEGRITY_CHANGE");
        if ((updateReasonFlags & CLOSE) != 0) flags.add("CLOSE");
        
        return String.join(" | ", flags);
    }

    @Override
    public int compareTo(UsnEntry o) {
        // Compares entries based on their USN. Used for sorting.
        
        return Long.compare(updateSequenceNumber, o.updateSequenceNumber);
    }
}
