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
    private JProgressBar   progressBar1;
    private JTextField     filesUploadingProgressTextField;
    private JTextArea      textArea;
    private JScrollPane scroll;

    public EmbeddedSFTP() {
        chooseFilesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                EmbeddedSFTP.this.selectFiles();
            }
        });

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (EmbeddedSFTP.this.getFile() == null) {
                    return;
                }

                EmbeddedSFTP.this.buildUserInformation(EmbeddedSFTP.this.textField3.getText(), EmbeddedSFTP.this.passwordField1.getPassword());
                EmbeddedSFTP.this.switchViewportContents();
                EmbeddedSFTP.this.uploadFiles(EmbeddedSFTP.this.connectionInformation, "/landing_zone", EmbeddedSFTP.this.selected_file);
            }
        });
    }

    @Override
    public void init() {
        this.content = this.getContentPane();
        this.content.add(this.panel);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }

        catch (Exception ex) {
        }

        // Security properties
        java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");

        this.host = this.getDocumentBase().getHost();
        if (this.host.contentEquals("")) {
            this.append("No host information found. Using localhost");
            this.host = "localhost";
        }

        this.filesUploadingProgressTextField.setVisible(false);
        this.scroll.setVisible(false);
        this.textArea.setVisible(false);
        this.progressBar1.setVisible(false);
        this.content.setSize(this.panel.getSize());
    }

    public File getFile() {
        return this.selected_file;
    }

    public void setFile(File file) {
        this.selected_file = file;
        this.selectedFilesTextField.setText(file.getAbsolutePath());
        this.chooseFilesButton.setEnabled(false);
    }

    public void append(String string) {
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

    private void switchViewportContents() {
        this.selectedFilesTextField.setVisible(false);
        this.chooseFilesButton.setVisible(false);
        this.siteUsernameTextField.setVisible(false);
        this.sitePasswordTextField.setVisible(false);
        this.textField3.setText("");
        this.textField3.setVisible(false);
        this.passwordField1.setText("");
        this.passwordField1.setVisible(false);
        this.filesUploadingProgressTextField.setVisible(true);
        this.scroll.setVisible(true);
        this.textArea.setVisible(true);
        this.progressBar1.setVisible(true);
    }

    private boolean uploadFiles(final ConnectionInformation connectionInformation, final String dest, final File files) {
        // Don't execute unless we have initialised the relevant properties
        if (this.host == null) {
            return false;
        }

        final boolean[] deferredReturn = {false};
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                deferredReturn[0] = uploadFilesDeffered(EmbeddedSFTP.this.host, connectionInformation, dest, files);
            }
        });

        return deferredReturn[0];
    }

    private boolean uploadFilesDeffered(String host, ConnectionInformation connectionInformation, String dest, File files) {
        if (this.sftpUploadTask == null || !this.sftpUploadTask.isAlive()) {
            this.sftpUploadTask = new SFTPUploadTask(EmbeddedSFTP.this, host, connectionInformation, dest, files);
            this.sftpUploadTask.start();
            return true;
        }

        return false;
    }

    public void init(int op, String src, String dest, long max) {
        this.max = max;
        this.count = 0;
        this.progressBar1.setMaximum((int) max);
        this.progressBar1.setMinimum(0);
    }

    public boolean count(long count) {
        this.count += count;
        this.progressBar1.setValue((int) this.count);
        return true;
    }

    public void end() {
        this.progressBar1.setValue((int) this.max);
    }
}
