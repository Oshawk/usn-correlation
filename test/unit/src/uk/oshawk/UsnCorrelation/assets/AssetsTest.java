package uk.oshawk.UsnCorrelation.assets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

public class AssetsTest {
    public static RandomAccessFile getJ() {
        try {
            return new RandomAccessFile(new File(AssetsTest.class.getResource("$J").toURI()), "r");
        } catch (URISyntaxException | FileNotFoundException e) {
            fail("Failed to open $J.");
            return null;
        }
    }
    
    public static AbstractFile getJMock() {
        final RandomAccessFile j = AssetsTest.getJ();
        
        // Avoid the need for an Autopsy database by proxying AbstractFile reads to RandomAccessFile reads.
        AbstractFile jMock = mock(AbstractFile.class);
        try {
            when(jMock.read(any(byte[].class), any(long.class), any(long.class))).thenAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock iom) throws Throwable {
                    j.seek((int) ((long) iom.getArgument(1)));
                    j.read((byte[]) iom.getArgument(0), 0, (int) ((long) iom.getArgument(2)));
                    return null;
                }
            });
            
            when(jMock.getSize()).thenReturn(j.length());
        } catch (TskCoreException | IOException e) {
            fail();
            return null;
        }
        
        return jMock;
    }
}
