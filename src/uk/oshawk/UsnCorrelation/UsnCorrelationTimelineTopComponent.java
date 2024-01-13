package uk.oshawk.UsnCorrelation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openide.windows.TopComponent;
import org.sleuthkit.datamodel.AbstractFile;

@TopComponent.Description(
    preferredID = "UsnCorrelationTimelineTopComponent",
    persistenceType = TopComponent.PERSISTENCE_NEVER
)
public class UsnCorrelationTimelineTopComponent extends TopComponent {
    // The main graphical timeline UI component.
    // The layout (outer grid) is documented in the report.
    
    private final Map<UsnEntry, UsnCorrelationTimelineEntry> timelineEntries;
    private final Map<AbstractFile, Color> fileColours;
    private final Map<AbstractFile, Integer> folderColumns;
    private final JPanel mainPanel;
    private final JScrollPane scrollPane;
    
    public UsnCorrelationTimelineTopComponent(AbstractFile file, boolean child) {
        // Creates the graphical timeline UI.
        
        timelineEntries = new HashMap<>();
        fileColours = new HashMap<>();
        folderColumns = new HashMap<>();
        mainPanel = new JPanel();
        scrollPane = new JScrollPane(mainPanel);
        
        // Sets the tab name from a constant.
        setName(UsnUtil.TIMELINE_TOP_COMPONENT_NAME);
        
        // Gets the $UsnJrnl file associated with the file.
        AbstractFile usn = UsnProcessor.getUsnForFile(file);
        
        if (usn == null) {
            return;
        }
        
        // Converts the AbstractFile to a UsnFile.
        UsnFile usnFile = UsnProcessor.INSTANCE.getUsnFile(usn);
        
        // Gets the entries for the timeline.
        List<UsnEntry> entries;
        if (child) {  // If it is a child timeline, just get the file's child entries.
            entries = usnFile.getChildEntries(file);
        } else {  // If it is a parent timeline, get all of files that the chosen file has been a parent to and combine their child entries.
            HashSet<AbstractFile> files = new HashSet<>();
            entries = new ArrayList<>();
            
            for (UsnEntry entry : usnFile.getParentEntries(file)) {                
                if (!files.contains(entry.getFile())) {
                    entries.addAll(usnFile.getChildEntries(entry.getFile()));
                    files.add(entry.getFile());
                }
            }
            
            // Sort the entries in USN order.
            Collections.sort(entries);
        }        
        
        // Create the UI for the entries.
        processEntries(entries);
        
        setLayout(new GridLayout(0, 1));
        setPreferredSize(new Dimension(0, 0));  // Needed or scroll will break.
        add(scrollPane);
    }
    
    private void processEntries(List<UsnEntry> entries) {
        // Create the UI for the list of entries.
        
        mainPanel.setLayout(new GridBagLayout());
        
        // Set up the folder columns.
        setupFolders(entries);
        
        // Populate the folder columns.
        populateFolders(entries);
    }

    private void setupFolders(List<UsnEntry> entries) {
        // Sets up the folder columns and their headers.
        
        // Iterates over all the entries.
        // Uses the first in the folder to create the folder's header.
        for (UsnEntry entry: entries) {
            Integer folderColumn = folderColumns.get(entry.getParentFile());
            
            if (folderColumn == null) {
                folderColumn = folderColumns.size();
                
                UsnCorrelationTimelineFolderHeader timelineFolderHeader = new UsnCorrelationTimelineFolderHeader(entry);
                
                GridBagConstraints timelineFolderHeaderConstraints = new GridBagConstraints();
                
                timelineFolderHeaderConstraints.gridx = folderColumn;
                timelineFolderHeaderConstraints.gridy = 0;
                timelineFolderHeaderConstraints.fill = GridBagConstraints.BOTH;
                
                mainPanel.add(timelineFolderHeader, timelineFolderHeaderConstraints);
                
                folderColumns.put(entry.getParentFile(), folderColumn);
            }
        }
    }
    
    private void populateFolders(List<UsnEntry> entries) {
        // Populates the folder columns with entries.
        
        int entryRow = 0;
        Integer oldNameColumn = null;
        
        // Iterates over all the entries.
        for (UsnEntry entry : entries) {
            // Is this entry the last in a column?
            boolean lastEntry = entry == entries.get(entries.size() - 1);
            
            // Gets the column index for the entry.
            int folderColumn = folderColumns.get(entry.getParentFile());
            
            // Gets the border colour for the entry.
            Color fileColor = fileColours.get(entry.getFile());
            
            // If there is no existing border colour, create one and insert it.
            if (fileColor == null) {
                fileColor = UsnUtil.getRandomColour();
                
                fileColours.put(entry.getFile(), fileColor);
            }
            
            // Renames are treated differently.
            if (oldNameColumn != null) {  // This is a RENAME_NEW_NAME.
                if (oldNameColumn == folderColumn) {  // The rename doesn't change folders, so act like a normal entry.
                    fillRow(entryRow, false, oldNameColumn, null);
                    entryRow++;
                }
            } else {  // This is not a RENAME_NEW_NAME, so proceed normally.
                entryRow++;
            }
            
            // Get the UI component for the entry.
            UsnCorrelationTimelineEntry timelineEntry = new UsnCorrelationTimelineEntry(entry, fileColor, lastEntry);
           
            GridBagConstraints timeLineEntryConstraints = new GridBagConstraints();
            
            timeLineEntryConstraints.gridx = folderColumn;
            timeLineEntryConstraints.gridy = entryRow;
            timeLineEntryConstraints.fill = GridBagConstraints.BOTH;
            
            // Add the entry's UI component to the graphical timeline.
            mainPanel.add(timelineEntry, timeLineEntryConstraints);
            
            timelineEntries.put(entry, timelineEntry);
            
            // Potentially fill the remaining columns with filler entries.
            // If RENAME_OLD_NAME and not last entry, leave filling for the next entry (RENAME_NEW_NAME).
            if ((entry.updateReasonFlags & UsnEntry.RENAME_OLD_NAME) == 0 || lastEntry) {
                fillRow(entryRow, lastEntry, folderColumn, oldNameColumn);
            }
            
            // If RENAME_OLD_NAME, set oldNameColumn to inform the next iteration.
            if ((entry.updateReasonFlags & UsnEntry.RENAME_OLD_NAME) != 0) {
                oldNameColumn = folderColumn;
            } else {
                oldNameColumn = null;
            }
        }
    }
    
    private void fillRow(int row, boolean lastEntry, Integer excluding1, Integer excluding2) {
        // Fills a row with filler elements excluding one (or two in the case of a rename) columns.
        
        // Iterates over all columns.
        for (int column = 0; column < folderColumns.size(); column++) {
            // Fills the column if it is not excluded.
            if (!((excluding1 != null && column == excluding1) || (excluding2 != null && column == excluding2))) {
                UsnCorrelationTimelineFiller timelineFiller = new UsnCorrelationTimelineFiller(lastEntry);

                GridBagConstraints timelineFillerConstraints = new GridBagConstraints();

                timelineFillerConstraints.gridx = column;
                timelineFillerConstraints.gridy = row;
                timelineFillerConstraints.fill = GridBagConstraints.BOTH;

                mainPanel.add(timelineFiller, timelineFillerConstraints);
            }
        }
    }
}
