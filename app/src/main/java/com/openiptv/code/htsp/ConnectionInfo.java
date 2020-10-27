package com.openiptv.code.htsp;

public class ConnectionInfo {
    private final String Ihostname;
    private final int Iport;
    private final String Iusername;
    private final String Ipassword;
    private final String IclientName;
    private final String IclientVersion;

    /**
     * Constructor for a ConnectionInfo Object (Wrapper for TVHeadEnd user details)
     * @param hostname for TVHeadEnd server
     * @param port for TVHeadEnd server
     * @param username registered on TVHeadEnd server
     * @param password assigned on TVHeadEnd server
     * @param clientName user-defined
     * @param clientVersion Android SDK Version (Default)
     */
    public ConnectionInfo(String hostname, int port, String username, String password, String clientName, String clientVersion) {
        Ihostname = hostname;
        Iport = port;
        Iusername = username;
        Ipassword = password;
        IclientName = clientName;
        IclientVersion = clientVersion;
    }

    /**
     * Returns the IP or Hostname of the TVHeadEnd server, set on instantiation.
     * @return server hostname/ip
     */
    public String getHostname() {
        return Ihostname;
    }

    /**
     * Returns the TVHeadEnd server port set on instantiation.
     * @return server port
     */
    public int getPort() {
        return Iport;
    }

    /**
     * Returns the client username set on instantiation.
     * @return username
     */
    public String getUsername() {
        return Iusername;
    }

    /**
     * Returns the client password set on instantiation.
     * @return password
     */
    public String getPassword() {
        return Ipassword;
    }

    /**
     * Returns the client name set on instantiation.
     * @return client name
     */
    public String getClientName() {
        return IclientName;
    }

    /**
     * Returns the client version set on instantiation.
     * @return client version
     */
    public String getClientVersion() {
        return IclientVersion;
    }
}
