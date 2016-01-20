package uk.gov.nhs.gstt.brc.bioinformatics.jon;

import java.io.File;
import java.security.PrivilegedActionException;

import com.jcraft.jsch.*;

/**
 * Created by Jonathan Charles Smith on 06/01/16.
 */
public class SFTPUploadTask extends Thread {

    private final EmbeddedSFTP          client;
    private final String                host;
    private final ConnectionInformation connection_information;
    private final String                destination;
    private final File                  file;

    public SFTPUploadTask(EmbeddedSFTP client, String host, ConnectionInformation connectionInformation, String dest, File file) {
        this.client = client;
        this.host = host;
        this.connection_information = connectionInformation;
        this.destination = dest;
        this.file = file;
    }

    @Override
    public void run() {
        try {
            java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    Session session;
                    ChannelSftp sftpChannel;
                    Channel channel;

                    try {
                        JSch jSch = new JSch();
                        session = jSch.getSession(SFTPUploadTask.this.connection_information.getUsername(), SFTPUploadTask.this.host, 22);
                        session.setUserInfo(SFTPUploadTask.this.connection_information);
                        session.setConfig("compression.s2c", "zlib@openssh.com,zlib,none");
                        session.setConfig("compression.c2s", "zlib@openssh.com,zlib,none");
                        session.setConfig("compression_level", "9");
                        session.connect();
                    }

                    catch(JSchException ex) {
                        SFTPUploadTask.this.client.append(ex.getLocalizedMessage());
                        return false;
                    }

                    try {
                        channel = session.openChannel("sftp");
                        channel.connect();
                        sftpChannel = (ChannelSftp) channel;
                    }

                    catch (JSchException ex) {
                        SFTPUploadTask.this.client.append(ex.getLocalizedMessage());
                        return false;
                    }

                    try {
                        sftpChannel.put(SFTPUploadTask.this.file.getAbsolutePath(), SFTPUploadTask.this.destination, SFTPUploadTask.this.client, ChannelSftp.RESUME);
                    }

                    catch (SftpException ex) {
                        SFTPUploadTask.this.client.append(ex.getLocalizedMessage());
                        return false;
                    }

                    return true;
                }
            });
        }

        catch (PrivilegedActionException ex) {
            this.client.append(ex.getLocalizedMessage());
        }
    }
}
