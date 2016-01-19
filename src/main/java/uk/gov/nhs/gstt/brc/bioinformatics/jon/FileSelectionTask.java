package uk.gov.nhs.gstt.brc.bioinformatics.jon;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.io.File;

/**
 * Created by Jonathan Charles Smith on 06/01/16.
 */
public class FileSelectionTask extends Thread {

    private final EmbeddedSFTP parent;
    private final Container content_pane;

    public FileSelectionTask(EmbeddedSFTP parent, Container contentPane) {
        this.parent = parent;
        this.content_pane = contentPane;
    }

    @Override
    public void run() {
        File fileList = (File) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
            public Object run() {
                JFileChooser chooser = new JFileChooser();

                // Only allow single file compressed - hopefully obtain optimal transfer
                FileNameExtensionFilter zipFilter = new FileNameExtensionFilter("Compressed Files", "zip", "gz");
                chooser.setFileFilter(zipFilter);

                int returnValue = chooser.showOpenDialog(FileSelectionTask.this.content_pane);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    return chooser.getSelectedFile();
                }

                return null;
            }
        });

        this.parent.setFile(fileList);
    }
}
