package uk.oshawk.UsnCorrelation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TimeUtilities;
import org.sleuthkit.datamodel.TskCoreException;
import uk.oshawk.UsnCorrelation.assets.AssetsTest;

public class UsnEntryTest {
    List<UsnEntry> entries;
    
    public UsnEntryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        final AbstractFile jMock = AssetsTest.getJMock();
        
        entries = new ArrayList<>();
        
        ByteBuffer buffer = ByteBuffer.allocate(0x250);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        try {
            // Positions from MFTEcmd.
            entries.add(spy(new UsnEntry(jMock, 9029456, buffer, 0L)));
            entries.add(spy(new UsnEntry(jMock, 9035408, buffer, 0L)));
            entries.add(spy(new UsnEntry(jMock, 9036664, buffer, 0L)));
            entries.add(spy(new UsnEntry(jMock, 9036872, buffer, 0L)));
            entries.add(spy(new UsnEntry(jMock, 9036976, buffer, 0L)));
        } catch (TskCoreException e) {
            fail();
        }
        
        // Avoid the need for an Autopsy database by intercepting UsnUtil.getFileFromRecordInformation calls.
        for (UsnEntry entry: entries) {
            doReturn(null).when(entry).getFileFromRecordInformation(any(long.class), any(long.class));
        }
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testFields() {
        System.out.println("fields");
        
        assertEquals(entries.get(0).name, "Secret.zip");
        assertEquals(entries.get(1).name, "Secret.zip");
        assertEquals(entries.get(2).name, "New Text Document.txt");
        assertEquals(entries.get(3).name, "New Text Document.txt");
        assertEquals(entries.get(4).name, "Beth_Secret.txt");
        
        assertEquals(entries.get(0).updateSequenceNumber, 9029456);
        assertEquals(entries.get(1).updateSequenceNumber, 9035408);
        assertEquals(entries.get(2).updateSequenceNumber, 9036664);
        assertEquals(entries.get(3).updateSequenceNumber, 9036872);
        assertEquals(entries.get(4).updateSequenceNumber, 9036976);
    }
    
    @Test
    public void testGetFileIndex() {
        System.out.println("getFileIndex");
        
        // Indexes from MFTEcmd.
        assertEquals(entries.get(0).getFileIndex(), 87102);
        assertEquals(entries.get(1).getFileIndex(), 87102);
        assertEquals(entries.get(2).getFileIndex(), 87111);
        assertEquals(entries.get(3).getFileIndex(), 87111);
        assertEquals(entries.get(4).getFileIndex(), 87111);   
    }

    @Test
    public void testGetFileSequenceNumber() {
        System.out.println("getFileSequenceNumber");
        
        // Sequence numbers from MFTEcmd.
        assertEquals(entries.get(0).getFileSequenceNumber(), 2);
        assertEquals(entries.get(1).getFileSequenceNumber(), 2);
        assertEquals(entries.get(2).getFileSequenceNumber(), 2);
        assertEquals(entries.get(3).getFileSequenceNumber(), 2);
        assertEquals(entries.get(4).getFileSequenceNumber(), 2);   
    }

    @Test
    public void testGetParentFileIndex() {
        System.out.println("getParentFileIndex");
        
        // Parent indexes from MFTEcmd.
        assertEquals(entries.get(0).getParentFileIndex(), 86963);
        assertEquals(entries.get(1).getParentFileIndex(), 86963);
        assertEquals(entries.get(2).getParentFileIndex(), 86966);
        assertEquals(entries.get(3).getParentFileIndex(), 86966);
        assertEquals(entries.get(4).getParentFileIndex(), 86966);  
    }

    @Test
    public void testGetParentFileSequenceNumber() {
        System.out.println("getParentFileSequenceNumber");
        
        // Parent sequence numbers from MFTEcmd.
        assertEquals(entries.get(0).getParentFileSequenceNumber(), 7);
        assertEquals(entries.get(1).getParentFileSequenceNumber(), 7);
        assertEquals(entries.get(2).getParentFileSequenceNumber(), 9);
        assertEquals(entries.get(3).getParentFileSequenceNumber(), 9);
        assertEquals(entries.get(4).getParentFileSequenceNumber(), 9); 
    }

    @Test
    public void testGetFile() {
        System.out.println("getFile");
        
        entries.get(0).getFile();
        entries.get(1).getFile();
        entries.get(2).getFile();
        entries.get(3).getFile();
        entries.get(4).getFile();
        
        // Record information from manual hex inspection.
        verify(entries.get(0)).getFileFromRecordInformation(0, 562949953508414L);
        verify(entries.get(1)).getFileFromRecordInformation(0, 562949953508414L);
        verify(entries.get(2)).getFileFromRecordInformation(0, 562949953508423L);
        verify(entries.get(3)).getFileFromRecordInformation(0, 562949953508423L);
        verify(entries.get(4)).getFileFromRecordInformation(0, 562949953508423L);
    }

    @Test
    public void testGetParentFile() {
        System.out.println("getParentFile");
        
        entries.get(0).getParentFile();
        entries.get(1).getParentFile();
        entries.get(2).getParentFile();
        entries.get(3).getParentFile();
        entries.get(4).getParentFile();
        
        // Parent record information from manual hex inspection.
        verify(entries.get(0)).getFileFromRecordInformation(0, 1970324837061555L);
        verify(entries.get(1)).getFileFromRecordInformation(0, 1970324837061555L);
        verify(entries.get(2)).getFileFromRecordInformation(0, 2533274790482870L);
        verify(entries.get(3)).getFileFromRecordInformation(0, 2533274790482870L);
        verify(entries.get(4)).getFileFromRecordInformation(0, 2533274790482870L);
    }

    @Test
    public void testGetUpdateTimestampString() {
        System.out.println("getUpdateTimestampString");
        
        // Timestamps from MFTEcmd.
        assertEquals(entries.get(0).getUpdateTimestampString(),  TimeUtilities.epochToTime(1600486359));
        assertEquals(entries.get(1).getUpdateTimestampString(),  TimeUtilities.epochToTime(1600486458));
        assertEquals(entries.get(2).getUpdateTimestampString(),  TimeUtilities.epochToTime(1600486496));
        assertEquals(entries.get(3).getUpdateTimestampString(),  TimeUtilities.epochToTime(1600486506));
        assertEquals(entries.get(4).getUpdateTimestampString(),  TimeUtilities.epochToTime(1600486506));
    }

    @Test
    public void testGetUpdateReasonFlagsString() {
        System.out.println("getUpdateReasonFlagsString");
        
        // Reason flags from MFTEcmd.
        assertEquals(entries.get(0).getUpdateReasonFlagsString(), "SECURITY_CHANGE | RENAME_NEW_NAME | CLOSE");
        assertEquals(entries.get(1).getUpdateReasonFlagsString(), "FILE_DELETE | CLOSE");
        assertEquals(entries.get(2).getUpdateReasonFlagsString(), "FILE_CREATE");
        assertEquals(entries.get(3).getUpdateReasonFlagsString(), "RENAME_OLD_NAME");
        assertEquals(entries.get(4).getUpdateReasonFlagsString(), "RENAME_NEW_NAME");
    }

    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        
        for (int i = 0; i < entries.size(); i++) {
            for (int j = 0; j < entries.size(); j++) {
                if (i < j) {
                    assertTrue(entries.get(i).compareTo(entries.get(j)) < 0);
                } else if (i > j) {
                    assertTrue(entries.get(i).compareTo(entries.get(j)) > 0);
                } else {
                    assertEquals(entries.get(i).compareTo(entries.get(j)), 0);
                }
            }
        }
    }
}
