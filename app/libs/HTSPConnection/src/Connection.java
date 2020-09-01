/*
    HTSP Message Library
    Author: Waldo Theron
    Version: 0.1
 */

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection implements Runnable {
    private ConnectionInfo connectionInfo;
    private ConnectionState currentState;
    private SocketChannel socketChannel;
    private SocketIOHandler socketIOHandler;
    private Selector channelSelector;
    private final Lock ccLock = new ReentrantLock();

    public interface IOHandler {
        boolean hasWriteableData();
        boolean write(SocketChannel socketChannel);
        boolean read(SocketChannel socketChannel);
    }

    public Connection(ConnectionInfo connectionInfo, SocketIOHandler socketIOHandler)
    {
        this.connectionInfo = connectionInfo;
        this.socketIOHandler = socketIOHandler;
    }

    @Override
    public void run() {
        // Do the initial connection
        openConnection();

        while(currentState == ConnectionState.CONNECTING || currentState == ConnectionState.CONNECTED)
        {

            if(channelSelector == null || !channelSelector.isOpen())
            {
                // Exit and Close Connection
                currentState = ConnectionState.FAILED;
                break;
            }

            manageChannel();
        }

        System.out.println("Exited loop!");

        if (currentState == ConnectionState.CLOSED || currentState == ConnectionState.FAILED) {
            System.out.println("HTSP Connection thread wrapping up without already being closed");
            currentState = ConnectionState.FAILED;
        }

    }

    public boolean openConnection()
    {
        ccLock.lock();
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(connectionInfo.getHostname(), connectionInfo.getPort()));
            channelSelector = Selector.open();
            int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ;
            socketChannel.register(channelSelector, operations);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            ccLock.unlock();
        }

        currentState = ConnectionState.CONNECTING;
        return true;
    }

    public void manageChannel()
    {
        try {
            channelSelector.select();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<SelectionKey> selectionKeySet = channelSelector.selectedKeys();
        Iterator<SelectionKey> keyIterator = selectionKeySet.iterator();

        while (keyIterator.hasNext()) {
            System.out.println("Has keys");
            SelectionKey selectionKey = keyIterator.next();
            keyIterator.remove();

            if (!selectionKey.isValid()) {
                break;
            }

            if (selectionKey.isConnectable()) {
                System.out.println("Connectable");
                handleConnect(selectionKey);
            }

            if (selectionKey.isReadable()) {
                System.out.println("Readable");
                handleRead(selectionKey);
            }

            if (selectionKey.isWritable()) {
                System.out.println("Writable");
                handleWrite(selectionKey);
            }

            if (currentState == ConnectionState.CLOSED || currentState == ConnectionState.FAILED) {
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
                e.printStackTrace();
            } finally {
                ccLock.unlock();
            }
        }
    }

    public void handleConnect(SelectionKey selectionKey)
    {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        try {
            socketChannel.finishConnect();
        } catch (IOException e) {
            currentState = ConnectionState.FAILED;
            return;
        }

        System.out.println("HTSP Connected");
        currentState = ConnectionState.CONNECTED;
    }

    public void handleRead(SelectionKey selectionKey)
    {
        System.out.println("processReadableSelectionKey()");
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        if (currentState != ConnectionState.CLOSED || currentState != ConnectionState.FAILED) {
            if (!socketIOHandler.read(socketChannel)) {
                System.out.println("Failed to process readable selection key");
                currentState = ConnectionState.FAILED;
            }
        }
    }

    public void handleWrite(SelectionKey selectionKey)
    {
        System.out.println("processWritableSelectionKey()");

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        if (currentState != ConnectionState.CLOSED || currentState != ConnectionState.FAILED) {
            if (!socketIOHandler.write(socketChannel)) {
                System.out.println("Failed to process writeable selection key");
                currentState = ConnectionState.FAILED;
            }
        }
    }

    public void setWritePending() {
            if (currentState == ConnectionState.CLOSED || currentState == ConnectionState.FAILED) {
                System.out.println("Attempting to write while closed, closing or failed - discarding");
                return;
            }

            ccLock.lock();
            try {
                if (socketChannel != null && socketChannel.isConnected() && !socketChannel.isConnectionPending()) {
                    try {
                        System.out.println("Write has been triggered!");
                        socketChannel.register(channelSelector, SelectionKey.OP_WRITE);
                        channelSelector.wakeup();
                    } catch (ClosedChannelException e) {
                        currentState = ConnectionState.FAILED;
                    }
                }
            } finally {
                ccLock.unlock();
            }
    }
}
