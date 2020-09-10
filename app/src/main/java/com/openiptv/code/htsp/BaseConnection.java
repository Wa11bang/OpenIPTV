package com.openiptv.code.htsp;

import android.util.Log;

public class BaseConnection {
    private final ConnectionInfo connectionInfo;
    private final SocketIOHandler socketIOHandler;
    private final HTSPMessageDispatcher htspMessageDispatcher;
    private final HTSPSerializer htspSerializer;
    private final Connection connection;
    private Thread connectionThread;
    private Authenticator authenticator;
    private static final String TAG = BaseConnection.class.getSimpleName();

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

    public void fullSync()
    {
        authenticator.fullSync();
    }

    public void start() {
        if (connectionThread != null) {
            Log.w(TAG, "BaseConnection has already started");
            return;
        }

        connectionThread = new Thread(connection);
        connectionThread.start();

        Log.w(TAG, "BaseConnection has started");
    }

    public void stop() {
        if (connectionThread == null) {
            Log.w(TAG, "BaseConnection has not started");
            return;
        }

        connection.closeConnection();
        connectionThread.interrupt();
    }

    public boolean addMessageListener(MessageListener listener)
    {
        htspMessageDispatcher.addMessageListener(listener);
        return true;
    }
}
