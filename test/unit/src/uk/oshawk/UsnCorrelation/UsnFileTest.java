package uk.oshawk.UsnCorrelation;

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.sleuthkit.datamodel.AbstractFile;
import uk.oshawk.UsnCorrelation.assets.AssetsTest;


public class UsnFileTest {
    UsnFile usnFile;
    
    public UsnFileTest() {
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
        
        usnFile = new UsnFile(jMock, true);
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testFields() {  // F23
        // Sizes from MFTEcmd.
        assertEquals(usnFile.entries.size(), 82085);
        assertEquals(usnFile.entriesByFileIndex.size(), 5577);
        assertEquals(usnFile.entriesByFileParentIndex.size(), 2967);
    }

    @Test
    public void testGetChildEntries_long_long() {  // F20
        System.out.println("getChildEntries");
        
        // USNs from MFTEcmd. Detailed entry content testing in UsnEntryTest.
        List<UsnEntry> entries = usnFile.getChildEntries(87102, 2);
        assertEquals(entries.size(), 9);
        assertEquals(entries.get(0).updateSequenceNumber, 9028176);
        assertEquals(entries.get(1).updateSequenceNumber, 9028256);
        assertEquals(entries.get(2).updateSequenceNumber, 9028336);
        assertEquals(entries.get(3).updateSequenceNumber, 9028416);
        assertEquals(entries.get(4).updateSequenceNumber, 9028576);
        assertEquals(entries.get(5).updateSequenceNumber, 9029184);
        assertEquals(entries.get(6).updateSequenceNumber, 9029264);
        assertEquals(entries.get(7).updateSequenceNumber, 9029456);
        assertEquals(entries.get(8).updateSequenceNumber, 9035408);
    }

    @Test
    public void testGetChildEntries_AbstractFile() {  // F20
        System.out.println("getChildEntries");
        
        AbstractFile file = mock(AbstractFile.class);
        when(file.getMetaAddr()).thenReturn(87102L);
        when(file.getMetaSeq()).thenReturn(2L);
        
        // USNs from MFTEcmd. Detailed entry content testing in UsnEntryTest.
        List<UsnEntry> entries = usnFile.getChildEntries(file);
        assertEquals(entries.size(), 9);
        assertEquals(entries.get(0).updateSequenceNumber, 9028176);
        assertEquals(entries.get(1).updateSequenceNumber, 9028256);
        assertEquals(entries.get(2).updateSequenceNumber, 9028336);
        assertEquals(entries.get(3).updateSequenceNumber, 9028416);
        assertEquals(entries.get(4).updateSequenceNumber, 9028576);
        assertEquals(entries.get(5).updateSequenceNumber, 9029184);
        assertEquals(entries.get(6).updateSequenceNumber, 9029264);
        assertEquals(entries.get(7).updateSequenceNumber, 9029456);
        assertEquals(entries.get(8).updateSequenceNumber, 9035408);
    }

    @Test
    public void testGetParentEntries_long_long() {  // F20
        System.out.println("getParentEntries");
        
        // USNs from MFTEcmd. Detailed entry content testing in UsnEntryTest.
        List<UsnEntry> entries = usnFile.getParentEntries(86966, 9);
        assertEquals(entries.size(), 69);
        assertEquals(entries.get(0).updateSequenceNumber, 8433400);
        assertEquals(entries.get(1).updateSequenceNumber, 8433504);
        assertEquals(entries.get(2).updateSequenceNumber, 8433664);
        assertEquals(entries.get(33).updateSequenceNumber, 8488232);
        assertEquals(entries.get(34).updateSequenceNumber, 8488328);
        assertEquals(entries.get(35).updateSequenceNumber, 8488432);
        assertEquals(entries.get(66).updateSequenceNumber, 9039504);
        assertEquals(entries.get(67).updateSequenceNumber, 9046384);
        assertEquals(entries.get(68).updateSequenceNumber, 9046480);
    }

    @Test
    public void testGetParentEntries_AbstractFile() {  // F20
        System.out.println("getParentEntries");
        
        AbstractFile file = mock(AbstractFile.class);
        when(file.getMetaAddr()).thenReturn(86966L);
        when(file.getMetaSeq()).thenReturn(9L);
        
        // USNs from MFTEcmd. Detailed entry content testing in UsnEntryTest.
        List<UsnEntry> entries = usnFile.getParentEntries(file);
        assertEquals(entries.size(), 69);
        assertEquals(entries.get(0).updateSequenceNumber, 8433400);
        assertEquals(entries.get(1).updateSequenceNumber, 8433504);
        assertEquals(entries.get(2).updateSequenceNumber, 8433664);
        assertEquals(entries.get(33).updateSequenceNumber, 8488232);
        assertEquals(entries.get(34).updateSequenceNumber, 8488328);
        assertEquals(entries.get(35).updateSequenceNumber, 8488432);
        assertEquals(entries.get(66).updateSequenceNumber, 9039504);
        assertEquals(entries.get(67).updateSequenceNumber, 9046384);
        assertEquals(entries.get(68).updateSequenceNumber, 9046480);
    }
}
