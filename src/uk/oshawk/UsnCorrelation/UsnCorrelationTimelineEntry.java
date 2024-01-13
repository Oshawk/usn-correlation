package uk.oshawk.UsnCorrelation;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import uk.oshawk.UsnCorrelation.assets.Assets;

public class UsnCorrelationTimelineEntry extends JPanel {
    // The UI component for a $UsnJrnl entry. Part of the graphical timeline.
    // The layout (inner grid) is documented in the report.
    
    private static final GridBagConstraints NAME_CONSTRAINTS = new GridBagConstraints(
            0,
            0,
            UsnUtil.TIMELINE_ENTRY_COLUMNS,
            1,
            0,
            0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            UsnUtil.TIMELINE_ENTRY_INSETS,
            0,
            0 
    );
    private static final GridBagConstraints TIMESTAMP_CONSTRAINTS = new GridBagConstraints(
            0,
            1,
            UsnUtil.TIMELINE_ENTRY_COLUMNS,
            1,
            0,
            0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            UsnUtil.TIMELINE_ENTRY_INSETS,
            0,
            0 
    );
    
    private int nextIconX = 0;
    private int nextIconY = 2;
    
    public UsnCorrelationTimelineEntry(UsnEntry entry, Color fileColour, boolean lastEntry) {
        // Creates the entry UI component.
        
        super(new GridBagLayout());
        
        setBorder(createBorder(fileColour, lastEntry));
        
        add(new JLabel(entry.name), NAME_CONSTRAINTS);
        
        add(new JLabel(entry.getUpdateTimestampString()), TIMESTAMP_CONSTRAINTS);
        
        setIcons(entry);
        
        // Adds click actions.
        addMouseListener(new UsnCorrelationTimelineMouseListener(entry, false));
    }
    
    private Border createBorder(Color fileColor, boolean lastEntry) {
        // Creates a border based on the file colour and if the entry is the last in a column.
        // If the entry is the last, it will add the coloured border to the bottum as well as the left and right.
        
        return BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
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
            ),
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(UsnUtil.TIMELINE_ENTRY_PADDING, 
                    UsnUtil.TIMELINE_ENTRY_PADDING, 
                    UsnUtil.TIMELINE_ENTRY_PADDING, 
                    UsnUtil.TIMELINE_ENTRY_PADDING
                ),
                BorderFactory.createLineBorder(fileColor)
            )
        );
    }
    
    private void setIcons(UsnEntry entry) {
        // Adds the action icons based on the entry's flags.
        
        if ((entry.updateReasonFlags & UsnEntry.DEFAULT_DATA_OVERWRITE) != 0) setIcon(Assets.DEFAULT_DATA_OVERWRITE, "DEFAULT_DATA_OVERWRITE");
        if ((entry.updateReasonFlags & UsnEntry.DEFAULT_DATA_EXTEND) != 0) setIcon(Assets.DEFAULT_DATA_EXTEND, "DEFAULT_DATA_EXTEND");
        if ((entry.updateReasonFlags & UsnEntry.DEFAULT_DATA_TRUNCATE) != 0) setIcon(Assets.DEFAULT_DATA_TRUNCATE, "DEFAULT_DATA_TRUNCATE");
        if ((entry.updateReasonFlags & UsnEntry.NAMED_DATA_OVERWRITE) != 0) setIcon(Assets.NAMED_DATA_OVERWRITE, "NAMED_DATA_OVERWRITE");
        if ((entry.updateReasonFlags & UsnEntry.NAMED_DATA_EXTEND) != 0) setIcon(Assets.NAMED_DATA_EXTEND, "NAMED_DATA_EXTEND");
        if ((entry.updateReasonFlags & UsnEntry.NAMED_DATA_TRUNCATE) != 0) setIcon(Assets.NAMED_DATA_TRUNCATE, "NAMED_DATA_TRUNCATE");
        if ((entry.updateReasonFlags & UsnEntry.FILE_CREATE) != 0) setIcon(Assets.FILE_CREATE, "FILE_CREATE");
        if ((entry.updateReasonFlags & UsnEntry.FILE_DELETE) != 0) setIcon(Assets.FILE_DELETE, "FILE_DELETE");
        if ((entry.updateReasonFlags & UsnEntry.EA_CHANGE) != 0) setIcon(Assets.EA_CHANGE, "EA_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.SECURITY_CHANGE) != 0) setIcon(Assets.SECURITY_CHANGE, "SECURITY_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.RENAME_OLD_NAME) != 0) setIcon(Assets.RENAME_OLD_NAME, "RENAME_OLD_NAME");
        if ((entry.updateReasonFlags & UsnEntry.RENAME_NEW_NAME) != 0) setIcon(Assets.RENAME_NEW_NAME, "RENAME_NEW_NAME");
        if ((entry.updateReasonFlags & UsnEntry.INDEXABLE_CHANGE) != 0) setIcon(Assets.INDEXABLE_CHANGE, "INDEXABLE_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.BASIC_INFORMATION_CHANGE) != 0) setIcon(Assets.BASIC_INFORMATION_CHANGE, "BASIC_INFORMATION_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.HARD_LINK_CHANGE) != 0) setIcon(Assets.HARD_LINK_CHANGE, "HARD_LINK_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.COMPRESSION_CHANGE) != 0) setIcon(Assets.COMPRESSION_CHANGE, "COMPRESSION_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.ENCRYPTION_CHANGE) != 0) setIcon(Assets.ENCRYPTION_CHANGE, "ENCRYPTION_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.OBJECT_ID_CHANGE) != 0) setIcon(Assets.OBJECT_ID_CHANGE, "OBJECT_ID_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.REPARSE_POINT_CHANGE) != 0) setIcon(Assets.REPARSE_POINT_CHANGE, "REPARSE_POINT_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.STREAM_CHANGE) != 0) setIcon(Assets.STREAM_CHANGE, "STREAM_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.TRANSACTED_CHANGE) != 0) setIcon(Assets.TRANSACTED_CHANGE, "TRANSACTED_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.INTEGRITY_CHANGE) != 0) setIcon(Assets.INTEGRITY_CHANGE, "INTEGRITY_CHANGE");
        if ((entry.updateReasonFlags & UsnEntry.CLOSE) != 0) setIcon(Assets.CLOSE, "CLOSE");
        
        // Adds filler to any unoccupied spaces in the row.
        while (nextIconY < 3) {
            setIcon(Assets.FILLER, null);
        }
    }
    
    private void setIcon(ImageIcon icon, String tooltip) {
        // Adds an action icon to the UI with the given tooltip.
        
        // Converts the ImageIcon to a JLabel so it can be displayed in the UI.
        JLabel iconLabel = new JLabel(icon);
        
        // Sets the tooltip if one is provided.
        if (tooltip != null) {
            iconLabel.setToolTipText(tooltip);
        }
        
        GridBagConstraints iconConstraints = new GridBagConstraints();
        
        iconConstraints.gridx = nextIconX;
        iconConstraints.gridy = nextIconY;
        iconConstraints.insets = UsnUtil.TIMELINE_ENTRY_INSETS;
        
        iconConstraints.weightx = 1;
        iconConstraints.weighty = 1;
        
        // Adds the icon.
        add(iconLabel, iconConstraints);
        
        // Moves to the next column.
        nextIconX += 1;
        
        // If this is past the last column. Set the column to 0 and move to the next row.
        if (nextIconX == UsnUtil.TIMELINE_ENTRY_COLUMNS) {
            nextIconX = 0;
            nextIconY += 1;
        }
    }
}
