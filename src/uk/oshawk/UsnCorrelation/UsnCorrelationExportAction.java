package uk.oshawk.UsnCorrelation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.NoCurrentCaseException;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.guiutils.JFileChooserFactory;
import org.sleuthkit.datamodel.AbstractFile;

public class UsnCorrelationExportAction extends AbstractAction {
    // The CSV export action.
    
    private final AbstractFile file;
    private final boolean child;
    private final JFileChooserFactory chooserFactory;
    
    public UsnCorrelationExportAction(AbstractFile file, boolean child) {
        // Called when a supported file is right-clicked.
        
        this.file = file;
        this.child = child;
        
        // Use a different message depending on if exporting a parent or a child.
        if (this.child) {
            putValue(Action.NAME, UsnUtil.EXPORT_ACTION_CHILD_NAME);
        } else {
            putValue(Action.NAME, UsnUtil.EXPORT_ACTION_PARENT_NAME);
        }
        
        chooserFactory = new JFileChooserFactory();
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        // Called when the right-click option is selected.
        
        if (UsnUtil.TEST_LOGGING) {
            Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F15] Right click CSV timeline action triggered for file (index=%d, sequence_number=%d).", file.getMetaAddr(), file.getMetaSeq()));
        }
        
        Case currentCase;
        try {
            currentCase = Case.getCurrentCaseThrows();
        } catch (NoCurrentCaseException e) {
            return;
        }
        
        // Creates a file save dialogue.
        JFileChooser fileChooser = chooserFactory.getChooser();
        
        // Sets the dialogue to save CSV files with the initial directory as Autopsy's export directory.
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setCurrentDirectory(new File(currentCase.getExportDirectory()));
        
        // Shows the file save dialogue. Calls the export method with the chosen file if the user clicked "save".
        if (fileChooser.showSaveDialog((Component) event.getSource()) == JFileChooser.APPROVE_OPTION) {
            export(fileChooser.getSelectedFile());
        }
    }
    
    public void export(File outFile) {
        // Exports the timeline to the specified CSV file.
        
        // Adds the CSV extension if it is not present.
        if (!outFile.getName().endsWith(".csv")) {
            outFile = new File(outFile.toString() + ".csv");
        }
        
        // Uses a buffered writer to efficiently write to the specified file.
        BufferedWriter br;
        try {
            br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            return;
        }
        
        // Gets the associated $UsnJrnl file.
        AbstractFile usn = UsnProcessor.getUsnForFile(file);
        
        // There is no associated $UsnJrnl file so it is impossible to create a timeline.
        if (usn == null) {
            return;
        }
        
        // Converts the AbstractFile to a UsnFile.
        UsnFile usnFile = UsnProcessor.INSTANCE.getUsnFile(usn);
        
        // Selects the correct list of entries based on if a parent or child export was selected.
        List<UsnEntry> entries;
        if (child) {
            entries = usnFile.getChildEntries(file);
        } else {
            entries = usnFile.getParentEntries(file);
        }
        
        try {
            // Writes the CSV file header.
            br.write("\"Time\",\"Action\",\"Child\",\"Parent\"\n");
            
            // Writes a line in the CSV file for each entry in the timeline.
            for (UsnEntry entry: entries) {
                br.write("\"");
                br.write(entry.getUpdateTimestampString());
                br.write("\",\"");
                br.write(entry.getUpdateReasonFlagsString());
                br.write("\",\"");
                br.write(entry.name);
                br.write("\",\"");
                
                // Attempts to get the parent file to write its name.
                AbstractFile parentFile = entry.getParentFile();
                if (file == null) {  // There is no parent file (ingest can be run to recover it).
                    br.write("RUN INGEST");
                } else {  // There is a parent file.
                    br.write(parentFile.getName());
                }
                
                br.write("\"\n");
            }
            
            if (UsnUtil.TEST_LOGGING) {
                Logger.getLogger("uk.oshawk.UsnCorrelation").info(String.format("[F16] File (index=%d, sequence_number=%d) entries exported to CSV (name=\"%s\").", file.getMetaAddr(), file.getMetaSeq(), outFile.getName()));
            }
        } catch (IOException e) {}
        
        try {
            br.close();
        } catch (IOException e) {}
    }
}
