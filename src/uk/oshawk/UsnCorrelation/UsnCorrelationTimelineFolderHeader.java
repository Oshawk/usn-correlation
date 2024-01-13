/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uk.oshawk.UsnCorrelation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class UsnCorrelationTimelineFolderHeader extends JPanel {
    // A folder header UI component used in UsnCorrelationTimelineTopComponent (the graphical timeline outer grid).
    
    private static final Border BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(
            UsnUtil.TIMELINE_TOP_COMPONENET_FOLDER_PADDING, 
            UsnUtil.TIMELINE_TOP_COMPONENET_FOLDER_PADDING, 
            0, 
            UsnUtil.TIMELINE_TOP_COMPONENET_FOLDER_PADDING
        ),
        BorderFactory.createMatteBorder(
            1, 
            1, 
            0, 
            1, 
            UsnUtil.TIMELINE_TOP_COMPONENT_FOLDER_COLOUR
        )
    );
    private static final GridBagConstraints NAME_CONSTRAINTS = new GridBagConstraints(
            0,
            0,
            1,
            1,
            0,
            0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            UsnUtil.TIMELINE_FOLDER_HEADER_INSETS,
            0,
            0 
    );
    
    public UsnCorrelationTimelineFolderHeader(UsnEntry entry) {
        // Creates the folder header.
        
        super(new GridBagLayout());
        
        // All folder headers have the same border.
        setBorder(BORDER);
        
        // Names the folder based on the first entry's parent's name.
        add(new JLabel(entry.getParentFile().getName()), NAME_CONSTRAINTS);
        
        // Adds click actions.
        addMouseListener(new UsnCorrelationTimelineMouseListener(entry, true));
    }
    
}
