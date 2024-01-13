package uk.oshawk.UsnCorrelation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.directorytree.ViewContextAction;
import org.sleuthkit.datamodel.AbstractFile;

@ServiceProvider(service = DataContentViewer.class)
public class UsnCorrelationDataContentViewer extends javax.swing.JPanel implements DataContentViewer {
    // The content viewer UI used to display the tabular timeline.
    
    private final UsnTableModel childTableModel;
    private final UsnTableModel parentTableModel;
    private final JTable childTable;
    private final JTable parentTable;
    private final JLabel helpLabel;
    private final JPanel helpPanel;
    private final JScrollPane childScrollPane;
    private final JScrollPane parentScrollPane;
    private final JScrollPane helpScrollPane;
    private final JTabbedPane tabbedPane;

    private class UsnTableModel extends AbstractTableModel {
        // A custom table model for $UsnJrnl entries. More efficent than using the default model.
        
        private final List<UsnEntry> entries;
        private final Map<Long, String> cache;

        public UsnTableModel() {
            entries = new ArrayList<>();
            cache = new HashMap<>();
        }
        
        @Override
        public String getColumnName(int column) {
            // Gets the column name given its index. Uses the a constant from UsnUtil so that they can easily be changed.
            
            return UsnUtil.VIEWER_TABLE_COLUMNS[column];
        }
        
        @Override
        public int getRowCount() {
            // Gets the number of rows in the table.
            
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            // Gets the number of columns in the table.
            
            return UsnUtil.VIEWER_TABLE_COLUMNS.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            // Gets the element at a given row and column.
            
            long cacheKey = (((long)rowIndex) << 32) | ((long)columnIndex);
            
            // Uses caching to improve performance.
            String result = cache.get(cacheKey);
            if (result != null) {
                return result;
            }
            
            // Row determines the entry.
            UsnEntry entry = entries.get(rowIndex);
            
            // Coulumn determies the attribute displayed.
            switch (columnIndex) {
                case 0:  // Time.
                    result = entry.getUpdateTimestampString();
                    break;
                case 1:  // Action.
                    result = entry.getUpdateReasonFlagsString();
                    break;
                case 2:  // Child.
                    result = entry.name;
                    break;
                case 3:  // Parent.
                    AbstractFile parentFile = entry.getParentFile();
                    if (parentFile == null) {
                        result = "RUN INGEST";
                    } else {
                        result = parentFile.getName();
                    }
                    break;
            }
            
            // Add the result to the cache.
            if (result != null) {
                cache.put(cacheKey, result);
            }
            
            return result;
        }
        
        public void add(UsnEntry entry) {
            // Adds an entry to the table. 
            
            entries.add(entry);
            
            // Causes the GUI to be updated if required.
            fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
        }
        
        public void clear() {
            // Clears the table.
            
            int size = entries.size();
            
            entries.clear();
            cache.clear();
            
            if (size != 0) {
                // Causes the GUI to be updated if required.
                fireTableRowsDeleted(0, size - 1);
            }
        } 
    }
    
    public UsnCorrelationDataContentViewer() {
        // Builds the GUI from Swing components.
        
        childTableModel = new UsnTableModel();
        parentTableModel = new UsnTableModel();
        childTable = new JTable(childTableModel);
        parentTable = new JTable(parentTableModel);
        helpLabel = new JLabel(UsnUtil.HELP_HTML);
        helpPanel = new JPanel();
        childScrollPane = new JScrollPane(childTable);
        parentScrollPane = new JScrollPane(parentTable);
        helpScrollPane = new JScrollPane(helpPanel);
        
        // Allows the file names to be clicked and shown in the result viewer.
        childTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tableMouseClicked(event, childTableModel, childTable);
            }
        });
        parentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                tableMouseClicked(event, parentTableModel, parentTable);
            }
        });
        
        // Stops helpLabel from being centered.
        helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
        helpPanel.add(helpLabel);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(UsnUtil.VIEWER_TAB_CHILD, childScrollPane);
        tabbedPane.addTab(UsnUtil.VIEWER_TAB_PARENT, parentScrollPane);
        tabbedPane.addTab(UsnUtil.VIEWER_TAB_HELP, helpScrollPane);
        
        setLayout(new GridLayout(0, 1));
        setPreferredSize(new Dimension(0, 0));  // Needed or scroll will break.
        add(tabbedPane);
    }
    
    private void tableMouseClicked(MouseEvent event, UsnTableModel tableModel, JTable table) {
        // Allows the file names to be clicked and shown in the result viewer.
        
        if (event.getClickCount() == 2) {  // Only trigger on double clicks.
            UsnEntry entry = tableModel.entries.get(table.getSelectedRow());
            
            AbstractFile file = null;
            switch (table.getSelectedColumn()) {
                case 2:
                    file = entry.getFile();
                    break;
                case 3:
                    file = entry.getParentFile();
                    break;
            }
            
            if (file == null) {
                return;
            }
            
            // Feels wrong.
            ViewContextAction action = new ViewContextAction(null, file);
            action.actionPerformed(null);
        }
    }

    @Override
    public void setNode(Node node) {
        // Called when a new supported file is selected.
        
        AbstractFile file = node.getLookup().lookup(AbstractFile.class);
        
        // Gets the $UsnJrnl file associated with the selected file.
        AbstractFile usn = UsnProcessor.getUsnForFile(file);
        
        // The selected file has no asscoiated $UsnJrnl.
        if (usn == null) {
            return;
        }
        
        // Converts the AbstractFile into a UsnFile that contains the needed entries.
        UsnFile usnFile = UsnProcessor.INSTANCE.getUsnFile(usn);
        
        // Add the selected file's child entries to the child table.
        for (UsnEntry entry: usnFile.getChildEntries(file)) {
            childTableModel.add(entry);
        }
        
        // Adds the selected file's parent entries to the parent table.
        for (UsnEntry entry: usnFile.getParentEntries(file)) {
            parentTableModel.add(entry);
        }
    }

    @Override
    public String getTitle() {
        // Gets the title of the content viewer from a constant.
        
        return UsnUtil.VIEWER_TITLE;
    }

    @Override
    public String getToolTip() {
        // Gets the tooltip of the content viewer from a constant.
        
        return UsnUtil.VIEWER_TOOLTIP;
    }

    @Override
    public DataContentViewer createInstance() {
        // Creates a new instance of the content viewer.
        
        return new UsnCorrelationDataContentViewer();
    }

    @Override
    public Component getComponent() {
        // Gets the UI component associated with the content viewer.
        // In this case they are implemented in the same class.
        
        return this;
    }

    @Override
    public void resetComponent() {
        // Called when a new file is selected.
        
        // Clears the contents of both tables.
        childTableModel.clear();
        parentTableModel.clear();
        
        // Selects the first tab.
        tabbedPane.setSelectedIndex(0);
    }

    @Override
    public boolean isSupported(Node node) {
        // Returns whether the content viewer supports a selected item.
        
        AbstractFile file = node.getLookup().lookup(AbstractFile.class);
        
        // If the selected item is not a file, return false.
        if (file == null) {
            return false;
        }
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F11] File (index=%d, sequence_number=%d) selected.", file.getMetaAddr(), file.getMetaSeq()));
        }
        
        // Gets the $UsnJrnl file associated with the selected file.
        AbstractFile usn = UsnProcessor.getUsnForFile(file);
        
        // If the selected item has no associated $UsnJrnl file, rerurn false.
        if (usn == null) {
            return false;
        }
        
        // Converts the AbstractFile into a UsnFile that contains the needed entries.
        UsnFile usnFile = UsnProcessor.INSTANCE.getUsnFile(usn);
        
        // If the selected file has parent or child entries, return true, else return false. 
        return !usnFile.getChildEntries(file).isEmpty() || !usnFile.getParentEntries(file).isEmpty();
    }

    @Override
    public int isPreferred(Node node) {
        // Used to determine if a content viewer should be automatically selected.
        
        // Since the timelines are not specific to a type of file, return a low priority.
        return 1;
    }
}
