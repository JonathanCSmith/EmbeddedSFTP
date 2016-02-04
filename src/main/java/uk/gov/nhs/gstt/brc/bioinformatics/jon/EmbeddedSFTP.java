package uk.gov.nhs.gstt.brc.bioinformatics.jon;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import com.jcraft.jsch.SftpProgressMonitor;

/**
 * Created by Jonathan Charles Smith on 06/01/16.
 */
public class EmbeddedSFTP extends JApplet implements SftpProgressMonitor {

    private SFTPUploadTask        sftpUploadTask        = null;
    private FileSelectionTask     fileSelectionTask     = null;
    private String                host                  = null;
    private ConnectionInformation connectionInformation = null;
    private File                  selected_file         = null;
    private long                  count                 = 0;
    private long                  max                   = 0;

    private Container      content;
    private JPanel         panel;
    private JTextField     selectedFilesTextField;
    private JButton        chooseFilesButton;
    private JTextField     siteUsernameTextField;
    private JTextField     sitePasswordTextField;
    private JTextField     textField3;
    private JPasswordField passwordField1;
    private JButton        submitButton;
    private JTextField     filesUploadingProgressTextField;
    private JTextArea      textArea;
    private JScrollPane    scroll;

    public EmbeddedSFTP() {
        this.chooseFilesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EmbeddedSFTP.this.append("Selecting files.");
                EmbeddedSFTP.this.selectFiles();
            }
        });
        this.submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // NOOP out if we don't have a file here
                if (EmbeddedSFTP.this.getFile() == null) {
                    return;
                }

                EmbeddedSFTP.this.append("Attempting File Upload.");

                // Generate the user information, followed by task initiation and display of relevant information
                EmbeddedSFTP.this.buildUserInformation(EmbeddedSFTP.this.textField3.getText(), EmbeddedSFTP.this.passwordField1.getPassword());
                EmbeddedSFTP.this.uploadFiles(EmbeddedSFTP.this.connectionInformation, EmbeddedSFTP.this.selected_file);
            }
        });
    }

    @Override
    public void init() {
        this.content = this.getContentPane();
        this.content.add(this.panel);
        this.content.setSize(this.panel.getSize());

        // Change the look - TODO: can we improve upon this wrt to generic embedding?
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        catch (Exception ex) {
        }

        // Security properties
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

        // Build host details based on the embedding document.
        this.host = this.getDocumentBase().getHost();
        if (this.host.contentEquals("")) {
            this.append("No host information found. Using localhost");
            this.host = "localhost";
        }
    }

    public File getFile() {
        return this.selected_file;
    }

    public void setFile(File file) {
        if (file == null) {
            return;
        }

        this.append("File selected: " + file.getName());

        this.selected_file = file;
        this.selectedFilesTextField.setText(file.getAbsolutePath());
    }

    public void append(String string) {
        System.out.println(string);
        this.textArea.append("\n" + string);
    }

    private boolean selectFiles() {
        if (this.fileSelectionTask == null || !this.fileSelectionTask.isAlive()) {
            this.fileSelectionTask = new FileSelectionTask(this, this.content);
            this.fileSelectionTask.start();
            return true;
        }

        return false;
    }

    private void buildUserInformation(String username, char[] password) {
        this.connectionInformation = new ConnectionInformation("biocompute-DM_user_" + username, password);
    }

    private boolean uploadFiles(final ConnectionInformation connectionInformation, final File files) {
        // Don't execute unless we have initialised the relevant properties
        if (this.host == null) {
            return false;
        }

        final boolean[] deferredReturn = {false};
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                deferredReturn[0] = uploadFilesDeferred(EmbeddedSFTP.this.host, connectionInformation, files);
            }
        });

        return deferredReturn[0];
    }

    private boolean uploadFilesDeferred(String host, ConnectionInformation connectionInformation, File files) {
        if (this.sftpUploadTask == null || !this.sftpUploadTask.isAlive()) {
            this.sftpUploadTask = new SFTPUploadTask(EmbeddedSFTP.this, host, connectionInformation, files);
            this.sftpUploadTask.start();
            return true;
        }

        return false;
    }

    public void init(int op, String src, String dest, long max) {
        this.max = max;
        this.count = 0;
        this.append("Upload Progress: 0%");
    }

    public boolean count(long count) {
        this.count += count;
        int amount = ((int) Math.floor((this.count * 100.0f) / (float) this.max));
        if (amount % 5 == 0) {
            this.append("Upload Progress: " + amount + "%");
        }

        return true;
    }

    public void end() {
        this.append("Upload Progress: 100%");
    }

    // Function to restore original based on an upload complete notification
    public void reset() {
        this.append("Upload complete.");
        this.selectedFilesTextField.setText("");
        this.fileSelectionTask = null;
        this.sftpUploadTask = null;
        this.connectionInformation = null;
        this.selected_file = null;
    }
}
