package uk.gov.nhs.gstt.brc.bioinformatics.jon;

import com.jcraft.jsch.UserInfo;

/**
 * Created by Jonathan Charles Smith on 06/01/16.
 */
public class ConnectionInformation implements UserInfo {

    private final String username;
    private final String password;

    public ConnectionInformation(String username, char[] password) {
        this.username = username;
        this.password = new String(password);
    }


    public String getPassphrase() {
        return null;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean promptPassword(String message) {
        return false;
    }

    public boolean promptPassphrase(String message) {
        return false;
    }

    public boolean promptYesNo(String message) {
        return false;
    }

    public void showMessage(String message) {

    }
}
