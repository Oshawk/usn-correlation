package uk.oshawk.UsnCorrelation;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class UsnCorrelationTimelineFiller extends JPanel {
    // A filler entry UI component used in UsnCorrelationTimelineTopComponent (the graphical timeline outer grid).
    
    public UsnCorrelationTimelineFiller(boolean lastEntry) {
        // Creates the filler entry UI component.
        
        super();
        
        setBorder(createBorder(lastEntry));
    }
    
    private Border createBorder(boolean lastEntry) {
        // Creates a border based on if the entry is the last in a column.
        // If the entry is the last, it will add a border to the bottum as well as the left and right.
        
        return BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(
                0, 
                UsnUtil.TIMELINE_TOP_COMPONENET_FOLDER_PADDING, 
                lastEntry ? UsnUtil.TIMELINE_TOP_COMPONENET_FOLDER_PADDING : 0, 
                UsnUtil.TIMELINE_TOP_COMPONENET_FOLDER_PADDING
            ),
            BorderFactory.createMatteBorder(
                0, 
                1, 
                lastEntry ? 1 : 0,
                1, 
                UsnUtil.TIMELINE_TOP_COMPONENT_FOLDER_COLOUR
            )
        );
    }
}
