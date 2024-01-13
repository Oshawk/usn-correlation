package uk.oshawk.UsnCorrelation;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;

public class UsnCorrelationTimelineAction extends AbstractAction {
    // The graphical timeline action.
    
    private final AbstractFile file;
    private final boolean child;
    
    public UsnCorrelationTimelineAction(AbstractFile file, boolean child) {
        // Called when a supported file is right-clicked.
        
        this.file = file;
        this.child = child;
        
        // Use a different message depending on if exporting a parent or a child.
        if (this.child) {
            putValue(Action.NAME, UsnUtil.TIMELINE_ACTION_CHILD_NAME);
        } else {
            putValue(Action.NAME, UsnUtil.TIMELINE_ACTION_PARENT_NAME);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Called when the right-click option is selected.
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F15] Right click graphical timeline action triggered for file (index=%d, sequence_number=%d).", file.getMetaAddr(), file.getMetaSeq()));
        }
        
        // Uses invokeLater to avoid freezing the program whilst the graphical timeline is generated.
        // Creates UsnCorrelationTimelineTopComponent, opens it, brings it to the front and sets it as active.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                UsnCorrelationTimelineTopComponent component = new UsnCorrelationTimelineTopComponent(file, child);
                
                component.open();
                component.toFront();
                component.requestActive();
            }
        });
    }
}
