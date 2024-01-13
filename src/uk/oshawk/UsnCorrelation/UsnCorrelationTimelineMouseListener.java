package uk.oshawk.UsnCorrelation;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import org.sleuthkit.autopsy.directorytree.ViewContextAction;
import org.sleuthkit.datamodel.AbstractFile;

public class UsnCorrelationTimelineMouseListener extends MouseAdapter {
    // Enables click actions for the graphical timeline.
    
    private final UsnEntry entry;
    private final boolean folder;
    
    public UsnCorrelationTimelineMouseListener(UsnEntry entry, boolean folder) {
        // Created when the timeline is created.
        // Actions vary based on the entry being clicked and if a folder is being clicked.
        
        this.entry = entry;
        this.folder = folder;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        // Called when a graphical timeline component is clicked.
        
        // If it is not a double click, return.
        if (e.getClickCount() != 2) return;

        // Get the file (or parent file if a folder is clicked) for the entry.
        AbstractFile file;
        if (folder) {
            file = entry.getParentFile();
        } else {
            file = entry.getFile();
        }
        
        // If failed to get the file, return.
        if (file == null) return;
        
        // Get the file's associated $UsnJrnl.
        AbstractFile usn = UsnProcessor.getUsnForFile(file);

        // If the file has no associated $UsnJrnl, return.
        if (usn == null) return;
        
        // Convert the AbstractFile to a UsnFile.
        UsnFile usnFile = UsnProcessor.INSTANCE.getUsnFile(usn);
        
        Action action = null;
        
        // Left click, view the child timeline for the file that was clicked (if it has one).
        if (e.getButton() == MouseEvent.BUTTON1 && !usnFile.getChildEntries(file).isEmpty()) {
            action = new UsnCorrelationTimelineAction(file, true);
        }
        
        // Middle click, view the file that was clicked in the result viewer.
        if (e.getButton() == MouseEvent.BUTTON2) {
            action = new ViewContextAction(null, file);
        }
        
        // Right click, view the parent timeline for the file that was clicked (if it has one).
        if (e.getButton() == MouseEvent.BUTTON3 && !usnFile.getParentEntries(file).isEmpty()) {
            action = new UsnCorrelationTimelineAction(file, false);
        }
        
        // If no action, return.
        if (action == null) return;
        
        // Perform the action.
        action.actionPerformed(null);
    }
    
}
