package nz.co.theron.iptv;

import android.os.Bundle;

public class TVHeadendAccount {
    private String username;
    private String password;
    private String hostname;
    private String port;
    private String clientName;

    public TVHeadendAccount(String username, String password, String hostname, String port, String clientName){

        this.setUsername(username);
        this.setPassword(password);
        this.setHostname(hostname);
        this.setPort(port);
        this.setClientName(clientName);
    }

    public TVHeadendAccount(Bundle bundle){
        this.setUsername(bundle.getString("username"));
        this.setPassword(bundle.getString("password"));
        this.setHostname(bundle.getString("hostname"));
        this.setPort(bundle.getString("port"));
        this.setClientName(bundle.getString("clientName"));
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}
