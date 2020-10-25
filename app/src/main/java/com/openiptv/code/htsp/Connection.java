package com.openiptv.code.htsp;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection implements Runnable {
    private ConnectionInfo connectionInfo;
    private Connection.State currentState;
    private SocketChannel socketChannel;
    private SocketIOHandler socketIOHandler;
    private Selector channelSelector;
    private final Lock ccLock = new ReentrantLock();
    private Set<Listener> listeners = new CopyOnWriteArraySet<>();
    private final static String TAG = Connection.class.getSimpleName();

    /**
     *
     */
    public interface Listener {
        /**
         * @param connection
         */
        void setConnection(@NonNull Connection connection);

        /**
         * @param state
         */
        void onConnectionStateChange(@NonNull State state);
    }

    /**
     *
     */
    public enum State {
        STARTED,
        CONNECTED,
        CONNECTING,
        CLOSED,
        FAILED
    }

    /**
     * @param connectionInfo
     * @param socketIOHandler
     */
    public Connection(ConnectionInfo connectionInfo, SocketIOHandler socketIOHandler) {
        this.connectionInfo = connectionInfo;
        this.socketIOHandler = socketIOHandler;
    }

    @Override
    public void run() {
        // Do the initial connection
        openConnection();

        while (currentState == State.CONNECTING || currentState == State.CONNECTED) {

            if (channelSelector == null || !channelSelector.isOpen()) {
                // Exit and Close Connection
                setState(State.FAILED);
                closeConnection();
                break;
            }

            manageChannel();
        }

        //System.out.println("Exited loop!");

        if (currentState == State.CLOSED || currentState == State.FAILED) {
            //System.out.println("HTSP Connection thread wrapping up without already being closed");
            setState(State.FAILED);
        }

    }

    /**
     *
     */
    public void openConnection() {
        setState(State.STARTED);
        ccLock.lock();
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(connectionInfo.getHostname(), connectionInfo.getPort()));
            channelSelector = Selector.open();
            int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ;
            socketChannel.register(channelSelector, operations);
        } catch (UnresolvedAddressException e) {
            setState(State.FAILED);
            return;
        } catch (IllegalArgumentException e) {
            setState(State.FAILED);
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            ccLock.unlock();
        }

        setState(State.CONNECTING);
    }

    /**
     *
     */
    public void closeConnection() {
        if (currentState == State.CLOSED || currentState == State.FAILED) {
            //Log.w(TAG, "Attempting to close while already closed, closing or failed");
            return;
        }

        //Log.i(TAG, "Closing HTSP Connection");

        ccLock.lock();
        try {
            if (socketChannel != null) {
                try {
                    socketChannel.socket().close();
                    socketChannel.close();
                } catch (IOException e) {
                    //Log.w(TAG, "Failed to close socket channel:", e);
                } finally {
                    socketChannel = null;
                }
            }

            if (channelSelector != null) {
                try {
                    channelSelector.close();
                } catch (IOException e) {
                    //Log.w(TAG, "Failed to close socket channel:", e);
                } finally {
                    channelSelector = null;
                }
            }

            setState(State.CLOSED);
        } finally {
            ccLock.unlock();
        }
    }

    /**
     *
     */
    public void manageChannel() {
        try {
            channelSelector.select();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<SelectionKey> keyIterator = null;

        try {
            if (currentState == State.CLOSED || currentState == State.FAILED) {
                //System.out.println("HTSP Connection thread wrapping up without already being closed");
                setState(State.FAILED);
                return;
            }
            Set<SelectionKey> selectionKeySet = channelSelector.selectedKeys();
            keyIterator = selectionKeySet.iterator();
        } catch (ClosedSelectorException e) {
            // Connection is basically closed
            closeConnection();
            return;
        }

        while (keyIterator.hasNext()) {
            //System.out.println("Has keys");
            SelectionKey selectionKey = keyIterator.next();
            keyIterator.remove();

            if (!selectionKey.isValid()) {
                setState(State.FAILED);
                break;
            }

            if (selectionKey.isValid() && selectionKey.isConnectable()) {
                //System.out.println("Connectable");
                handleConnect(selectionKey);
            }

            if (selectionKey.isValid() && selectionKey.isReadable()) {
                //System.out.println("Readable");
                handleRead(selectionKey);
            }

            if (selectionKey.isValid() && selectionKey.isWritable()) {
                //System.out.println("Writable");
                handleWrite(selectionKey);
            }

            if (currentState == State.CLOSED || currentState == State.FAILED) {
                break;
            }

            ccLock.lock();
            try {
                if (socketChannel != null && socketChannel.isConnected() && socketIOHandler.hasWriteableData()) {
                    socketChannel.register(channelSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else if (socketChannel != null && socketChannel.isConnected()) {
                    socketChannel.register(channelSelector, SelectionKey.OP_READ);
                }
            } catch (ClosedChannelException e) {
                // Expected when a user closes / exits the Live Channels Application
                e.printStackTrace();
            } finally {
                ccLock.unlock();
            }
        }
    }

    /**
     * @param selectionKey
     */
    public void handleConnect(SelectionKey selectionKey) {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            setState(State.FAILED);
            return;
        }

        //System.out.println("HTSP Connected");
        setState(State.CONNECTED);
    }

    /**
     * @param selectionKey
     */
    public void handleRead(SelectionKey selectionKey) {
        //System.out.println("processReadableSelectionKey()");
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        if (currentState != State.CLOSED || currentState != State.FAILED) {
            if (!socketIOHandler.read(socketChannel)) {
                //System.out.println("Failed to process readable selection key");
                setState(State.FAILED);
            }
        }
    }

    /**
     * @param selectionKey
     */
    public void handleWrite(SelectionKey selectionKey) {
        //System.out.println("processWritableSelectionKey()");

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        if (currentState != State.CLOSED || currentState != State.FAILED) {
            if (!socketIOHandler.write(socketChannel)) {
                System.out.println("Failed to process writeable selection key");
                setState(State.FAILED);
            }
        }
    }

    /**
     *
     */
    public void setWritePending() {
        if (currentState == State.CLOSED || currentState == State.FAILED) {
            System.out.println("Attempting to write while closed, closing or failed - discarding");
            return;
        }

        ccLock.lock();
        try {
            if (socketChannel != null && socketChannel.isConnected() && !socketChannel.isConnectionPending()) {
                try {
                    //System.out.println("Write has been triggered!");
                    socketChannel.register(channelSelector, SelectionKey.OP_WRITE);
                    channelSelector.wakeup();
                } catch (ClosedChannelException e) {
                    setState(State.FAILED);
                }
            }
        } finally {
            ccLock.unlock();
        }
    }

    /**
     * @param state
     */
    private void setState(final State state) {
        ccLock.lock();
        try {
            currentState = state;
        } finally {
            ccLock.unlock();
        }

        for (final Listener listener : listeners) {
            listener.onConnectionStateChange(state);
        }
    }

    /**
     * @param listener
     */
    public void addConnectionListener(Listener listener) {
        if (listeners.contains(listener)) {
            Log.w(TAG, "Attempted to add duplicate connection listener");
            return;
        }
        listener.setConnection(this);
        listeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeConnectionListener(Listener listener) {
        if (!listeners.contains(listener)) {
            Log.w(TAG, "Attempted to remove non existing connection listener");
            return;
        }
        listeners.remove(listener);
    }
}