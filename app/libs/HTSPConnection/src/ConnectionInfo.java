/*
    HTSP Message Library
    Author: Waldo Theron
    Version: 0.1
 */

public class ConnectionInfo {
    private final String Ihostname;
    private final int Iport;
    private final String Iusername;
    private final String Ipassword;
    private final String IclientName;
    private final String IclientVersion;

    public ConnectionInfo(String hostname, int port, String username, String password, String clientName, String clientVersion) {
        Ihostname = hostname;
        Iport = port;
        Iusername = username;
        Ipassword = password;
        IclientName = clientName;
        IclientVersion = clientVersion;
    }

    public String getHostname() {
        return Ihostname;
    }

    public int getPort() {
        return Iport;
    }

    public String getUsername() {
        return Iusername;
    }

    public String getPassword() {
        return Ipassword;
    }

    public String getClientName() {
        return IclientName;
    }

    public String getClientVersion() {
        return IclientVersion;
    }
}
