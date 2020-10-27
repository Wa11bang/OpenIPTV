package com.openiptv.code.htsp;

import android.util.Log;

public class BaseConnection {
    private static final String TAG = BaseConnection.class.getSimpleName();

    private final ConnectionInfo connectionInfo;
    private final SocketIOHandler socketIOHandler;
    private final HTSPMessageDispatcher htspMessageDispatcher;
    private final HTSPSerializer htspSerializer;
    private final Connection connection;
    private Thread connectionThread;
    private Authenticator authenticator;

    /**
     * Constructor for a BaseConnection Object
     * @param connectionInfo account details for TVH server
     */
    public BaseConnection(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;

        htspSerializer = new HTSPSerializer();
        htspMessageDispatcher = new HTSPMessageDispatcher();
        socketIOHandler = new SocketIOHandler(htspSerializer, htspMessageDispatcher);

        authenticator = new Authenticator(htspMessageDispatcher, connectionInfo);

        connection = new Connection(connectionInfo, socketIOHandler);
        connection.addConnectionListener(authenticator);

        htspMessageDispatcher.setConnection(connection);
        htspMessageDispatcher.addMessageListener(authenticator);
    }

    /**
     * Starts the current BaseConnection
     */
    public void start() {
        if (connectionThread != null) {
            Log.w(TAG, "BaseConnection has already started");
            return;
        }

        connectionThread = new Thread(connection);
        connectionThread.start();

        Log.w(TAG, "BaseConnection has started");
    }

    /**
     * Nicely stops the current BaseConnection
     */
    public void stop() {
        if (connectionThread == null) {
            Log.w(TAG, "BaseConnection has not started");
            return;
        }

        connection.closeConnection();
        connectionThread.interrupt();
    }

    /**
     * Returns the current HTSPMessageDispatcher instance
     * @return dispatcher reference
     */
    public HTSPMessageDispatcher getHTSPMessageDispatcher()
    {
        return htspMessageDispatcher;
    }

    /**
     * Returns the current Authenticator instance
     * @return authenticator reference
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Public link to the dispatcher method.
     * @param listener to add
     */
    public void addMessageListener(MessageListener listener)
    {
        htspMessageDispatcher.addMessageListener(listener);
    }
}
