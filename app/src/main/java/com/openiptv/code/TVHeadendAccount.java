package com.openiptv.code;
import android.os.Bundle;

/**
 * This class stores all the values required to make a connection to the TVHeadend server
 */
public class TVHeadendAccount {
    private String username;
    private String password;
    private String hostname;
    private String port;
    private String clientName;

    /**
     * Set the values for the TVHeadend account using a bundle with the values
     * @param bundle The bundle containing all the values for the TVHeadend account
     */
    public TVHeadendAccount(Bundle bundle){
        this.setUsername(bundle.getString("username"));
        this.setPassword(bundle.getString("password"));
        this.setHostname(bundle.getString("hostname"));
        this.setPort(bundle.getString("port"));
        this.setClientName(bundle.getString("clientName"));
    }

    /**
     * Returns the TVHeadend account's username
     * @return Username used to connect to TVHeadend server
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the TVHeadend username to something different
     * @param username The username to set it to
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the TVHeadend account's password
     * @return Password used to connect to TVHeadend server
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the TVHeadend password to something different
     * @param password The password to set it to
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the TVHeadend account's hostname
     * @return Hostname used to connect to TVHeadend server
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the TVHeadend Hostname to something different
     * @param hostname The hostname to set it to
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Returns the TVHeadend account's port
     * @return Port used to connect to TVHeadend server
     */
    public String getPort() {
        return port;
    }

    /**
     * Set the TVHeadend port to something different
     * @param port The port to set it to
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Returns the TVHeadend account's clientName
     * @return ClientName used to connect to TVHeadend server
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Set the TVHeadend ClientName to something different
     * @param clientName The password to set it to
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
