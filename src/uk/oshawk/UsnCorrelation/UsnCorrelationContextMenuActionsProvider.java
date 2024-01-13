package uk.oshawk.UsnCorrelation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.sleuthkit.autopsy.corecomponentinterfaces.ContextMenuActionsProvider;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;

@ServiceProvider(service = ContextMenuActionsProvider.class)
public class UsnCorrelationContextMenuActionsProvider implements ContextMenuActionsProvider {
    // Used to add right-click actions.
    
    @Override
    public List<Action> getActions() {
        // Triggered when a file or group of items (generally files) are right-clicked.
        // Adds the graphical timeline and CSV export options.
        
        ArrayList<Action> actionsList = new ArrayList<>();
        
        // Extract the files from the selection.
        Collection<? extends AbstractFile> selectedFiles = Utilities.actionsGlobalContext().lookupAll(AbstractFile.class);
        
        // The options only make sense if a single file is selected.
        if (selectedFiles.size() != 1) {
            return actionsList;
        }
        
        // Conveniant way of getting the single selected file from the list. Will not loop.
        for (AbstractFile selectedFile : selectedFiles) {
            AbstractFile usn = UsnProcessor.getUsnForFile(selectedFile);
            
            // The selected file's file system has no $UsnJrnl file.
            if (usn == null) {
                continue;
            }

            UsnFile usnFile = UsnProcessor.INSTANCE.getUsnFile(usn);
            
            // Only add the child actions if the selected file has child entries.
            if (!usnFile.getChildEntries(selectedFile).isEmpty()) {
                actionsList.add(new UsnCorrelationTimelineAction(selectedFile, true));
                actionsList.add(new UsnCorrelationExportAction(selectedFile, true));
            }
            
            // Only add the parent actions if the selected file has parent entries.
            if (!usnFile.getParentEntries(selectedFile).isEmpty()) {
                actionsList.add(new UsnCorrelationTimelineAction(selectedFile, false));
                actionsList.add(new UsnCorrelationExportAction(selectedFile, false));
            }
            
            if (UsnUtil.TEST_LOGGING) {
                Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F15] Right click actions added for file (index=%d, sequence_number=%d).", selectedFile.getMetaAddr(), selectedFile.getMetaSeq()));
            }
        }
        
        return actionsList;
    }
}
