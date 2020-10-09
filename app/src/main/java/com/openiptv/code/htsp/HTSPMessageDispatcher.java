package com.openiptv.code.htsp;

import android.util.Log;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.openiptv.code.Constants.DEBUG;

public class HTSPMessageDispatcher implements MessageDispatcher {
    private static final String TAG = HTSPMessageDispatcher.class.getSimpleName();

    private final Set<MessageListener> listeners = new CopyOnWriteArraySet<>(); // High Performance thread-safe implementation
    private final Queue<HTSPMessage> pendingMessages = new ConcurrentLinkedQueue<>();
    private Connection connection;

    public HTSPMessageDispatcher() {
    }

    /**
     *
     * @param connection
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     * @param listener
     */
    public void addMessageListener(MessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            return;
        }
        if(DEBUG) {
            System.out.println("Listener already exists!");
        }
    }

    /**
     *
     * @param listener
     */
    public void removeMessageListener(MessageListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
            return;
        }
        if(DEBUG) {
            System.out.println("Listener to remove does not exist!");
        }
    }

    /**
     *
     * @param message
     */
    public void onMessage(final HTSPMessage message) {
        for (final MessageListener listener : listeners) {
            listener.onMessage(message);
        }
    }

    @Override
    public void sendMessage(HTSPMessage message) throws HTSPException {
        if(!pendingMessages.contains(message))
        {
            if (connection != null) {
                connection.setWritePending();
                pendingMessages.add(message);
                if(DEBUG) {
                    Log.d(TAG, "Added message to queue");
                }
                return;
            }

            throw new HTSPException("MessageDispatcher has no oper Connection!");
        }
    }

    /**
     *
     * @return
     */
    public boolean hasPendingMessages() {
        return pendingMessages.size() > 0;
    }

    /**
     *
     * @return
     */
    public HTSPMessage getMessage() {
        if(DEBUG) {
            System.out.println("Dequeueing message for sending");
        }
        if(pendingMessages.size() != 0) {
            return pendingMessages.remove();
        }
        return null;
    }
}
