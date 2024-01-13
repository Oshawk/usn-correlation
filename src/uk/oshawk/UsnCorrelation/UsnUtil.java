package uk.oshawk.UsnCorrelation;

import java.awt.Color;
import java.awt.Insets;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

public class UsnUtil {
    // Contains constants and utility functons.
    
    // Include testing logs.
    // Disable in production as it geneates a lot of output.
    public static final boolean TEST_LOGGING = true;
    
    // The file cache, used to drastically imporve timeline performance.
    private static final Map<BigInteger, AbstractFile> fileCache = new HashMap<>();
    
    // Constants used for colour generation.
    private static final double GOLDEN_RATIO = 	1.618033988749894;
    private static final double GOLDEN_RATIO_CONJUGATE = 1. / GOLDEN_RATIO;
    private static double hue = 0.;
    
    // High level constants.
    public static final String NAME = "USN Correlation";
    public static final String DESCRIPTION = "USN Correlation";
    public static final String VERSION = "0.0.1";
    
    // Tabular timeline constants.
    public static final String VIEWER_TITLE = "History";
    public static final String VIEWER_TOOLTIP = "The file's history. Extracted from $UsnJrnl.";
    public static final String VIEWER_TAB_CHILD = "As Child";
    public static final String VIEWER_TAB_PARENT = "As Parent";
    public static final String VIEWER_TAB_HELP = "Help";
    public static final String VIEWER_TABLE_TIME = "Time";
    public static final String VIEWER_TABLE_ACTION = "Action";
    public static final String VIEWER_TABLE_CHILD = "Child";
    public static final String VIEWER_TABLE_PARENT = "Parent";
    public static final String[] VIEWER_TABLE_COLUMNS = new String[]{VIEWER_TABLE_TIME, VIEWER_TABLE_ACTION, VIEWER_TABLE_CHILD, VIEWER_TABLE_PARENT};
    
    // Tabulat timeline help HTML.
    public static final String HELP_HTML = String.join(
            "",
            "<html>",
            "<table>",
            "<tr><th>Name</th><th>Description</th></tr>",
            "<tr><td>DEFAULT_DATA_OVERWRITE</td><td>The default data stream was overwritten.</td></tr>",
            "<tr><td>DEFAULT_DATA_EXTEND</td><td>The default data stream was extended.</td></tr>",
            "<tr><td>DEFAULT_DATA_TRUNCATE</td><td>The default data stream was truncated.</td></tr>",
            "<tr><td>NAMED_DATA_OVERWRITE</td><td>An alternate data stream was overwritten.</td></tr>",
            "<tr><td>NAMED_DATA_EXTEND</td><td>Ab alternate data stream was extended.</td></tr>",
            "<tr><td>NAMED_DATA_TRUNCATE</td><td>An alternate data stream was truncated.</td></tr>",
            "<tr><td>FILE_CREATE</td><td>The file was created.</td></tr>",
            "<tr><td>FILE_DELETE</td><td>The file was deleted.</td></tr>",
            "<tr><td>EA_CHANGE</td><td>The extended attributes were changed.</td></tr>",
            "<tr><td>SECURITY_CHANGE</td><td>The security descriptor was changed.</td></tr>",
            "<tr><td>RENAME_OLD_NAME</td><td>The name was changed (old name).</td></tr>",
            "<tr><td>RENAME_NEW_NAME</td><td>The name was changed (new name).</td></tr>",
            "<tr><td>INDEXABLE_CHANGE</td><td>The indexed status was changed.</td></tr>",
            "<tr><td>BASIC_INFORMATION_CHANGE</td><td>The basic attributes or timestamps were changed.</td></tr>",
            "<tr><td>HARD_LINK_CHANGE</td><td>A hard link was added or removed.</td></tr>",
            "<tr><td>COMPRESSION_CHANGE</td><td>The compression status was changed.</td></tr>",
            "<tr><td>ENCRYPTION_CHANGE</td><td>The encryption status was changed.</td></tr>",
            "<tr><td>OBJECT_ID_CHANGE</td><td>The object ID was changed</td></tr>",
            "<tr><td>REPARSE_POINT_CHANGE</td><td>A reparse point was added, removed or changed.</td></tr>",
            "<tr><td>STREAM_CHANGE</td><td>An alternate data stream was added, removed or renamed.</td></tr>",
            "<tr><td>TRANSACTED_CHANGE</td><td>A data stream was modified via a transaction.</td></tr>",
            "<tr><td>INTEGRITY_CHANGE</td><td>A data stream's integrity attribute was changed.</td></tr>",
            "<tr><td>CLOSE</td><td>The file was closed.</td></tr>",
            "</table>",
            "</html>"
    );
    
    // Graphical timeline right-click action names.
    public static final String TIMELINE_ACTION_CHILD_NAME = "View Timeline (Child)";
    public static final String TIMELINE_ACTION_PARENT_NAME = "View Timeline (Parent)";
    
    // Graphical timeline top compoonent (outer grid) constants.
    public static final String TIMELINE_TOP_COMPONENT_NAME = "Timeline";
    public static final int TIMELINE_TOP_COMPONENET_FOLDER_PADDING = 16;
    public static final Color TIMELINE_TOP_COMPONENT_FOLDER_COLOUR = Color.BLACK;
    
    // Graphical timeline folder header constants.
    public static final int TIMELINE_FOLDER_HEADER_INSET = 4;
    public static final Insets TIMELINE_FOLDER_HEADER_INSETS = new Insets(TIMELINE_FOLDER_HEADER_INSET, TIMELINE_FOLDER_HEADER_INSET, TIMELINE_FOLDER_HEADER_INSET, TIMELINE_FOLDER_HEADER_INSET);
    
    // Graphical timeline entry constants.
    public static final int TIMELINE_ENTRY_PADDING = TIMELINE_TOP_COMPONENET_FOLDER_PADDING / 2;
    public static final int TIMELINE_ENTRY_COLUMNS = 3;
    public static final int TIMELINE_ENTRY_INSET = 4;
    public static final Insets TIMELINE_ENTRY_INSETS = new Insets(TIMELINE_ENTRY_INSET, TIMELINE_ENTRY_INSET, TIMELINE_ENTRY_INSET, TIMELINE_ENTRY_INSET);
    public static final int TIMELINE_ENTRY_ICON_WIDTH = 32;
    
    // CSV expoer right-click action names.
    public static final String EXPORT_ACTION_CHILD_NAME = "Export Timeline (Child)";
    public static final String EXPORT_ACTION_PARENT_NAME = "Export Timeline (Parent)";
    
    public static long getRecordInformationIndex(long recordInformation) {
        // Gets the index from file record information (index and sequence number combined).
        
        return (recordInformation & 0xffffffffL) + ((recordInformation & 0xffff00000000L) >>> 8);
    }

    public static short getRecordInformationSequenceNumber(long recordInformation) {
        // Gets the sequence number from file record information (index and sequence number combined).
        
        return (short)((recordInformation & 0xffff000000000000L) >>> 48);
    }
    
    public static AbstractFile getFileFromIndexSequenceNumber(long fileSystemId, long index, short sequenceNumber) {
        // Gets a AbstractFile from its file system ID, index and sequence number.
        // Uses caching to drastically imporve performance.
        
        // Construct the cache key from the three arguments.
        BigInteger bigFileSystemId = BigInteger.valueOf(fileSystemId);
        BigInteger bigIndex = BigInteger.valueOf(index);
        BigInteger bigSequenceNumber = BigInteger.valueOf(sequenceNumber);
        BigInteger cacheKey = bigFileSystemId.shiftLeft(64).or(bigIndex).shiftLeft(16).or(bigSequenceNumber);
        
        // If the cache contains the key, return the file from the cache.
        AbstractFile file = fileCache.get(cacheKey);
        if (file != null) {
            return file;
        }
        
        SleuthkitCase sleautkitCase;
        try {
            sleautkitCase = Case.getCurrentCaseThrows().getSleuthkitCase();
        } catch (NoCurrentCaseException e) {
            return null;
        }
        
        // Use a manual SQL query to get the file from the three arguments.
        List<AbstractFile> files;
        try {
            files = sleautkitCase.findAllFilesWhere(String.format("fs_obj_id = %d AND meta_addr = %d AND meta_seq = %d", fileSystemId, index, sequenceNumber));
        } catch (TskCoreException e) {
            return null;
        }
        
        // If files were returned from the SQL query, get the first one, add it to the cache and return it.
        if (!files.isEmpty()) {
            file = files.get(0);
            
            fileCache.put(cacheKey, file);
            
            return file;
        }
        
        return null;
    }

    public static AbstractFile getFileFromRecordInformation(long fileSystemId, long recordInformation) {
        // Gets an AbstractFIle from its file system ID and record informaton (index and sequence number combined).
        
        return getFileFromIndexSequenceNumber(fileSystemId, getRecordInformationIndex(recordInformation), getRecordInformationSequenceNumber(recordInformation));
    }
    
    public static Color getRandomColour() {
        // Gets a nice kooking random colour.
        // https://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
        
        hue += GOLDEN_RATIO_CONJUGATE;
        hue %= 1.;
        
        return Color.getHSBColor((float)hue, (float)0.5, (float)0.95);
    }
}
