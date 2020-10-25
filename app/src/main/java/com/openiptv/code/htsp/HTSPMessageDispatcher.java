package com.openiptv.code.htsp;

import android.util.Log;
import android.util.LongSparseArray;

import java.util.HashMap;
import java.util.Map;
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

    // Part of synchronous implementation
    private static final LongSparseArray<String> messageResponseMethodsBySequence = new LongSparseArray<>();
    private final LongSparseArray<Object> sequenceLocks = new LongSparseArray<>();
    private final LongSparseArray<HTSPMessage> sequenceResponses = new LongSparseArray<>();
    private static final int SYNC_SEQ = 101010;

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

        if (message.containsKey("seq")) {
            long seq = message.getLong("seq");

            // Reply messages don't include a method, only the sequence supplied in the request, so
            // if we have this sequence in our lookup table, go ahead and add the method into the
            // message.
            if (messageResponseMethodsBySequence.indexOfKey(seq) >= 0) {
                if (!message.containsKey("method")) {
                    message.put("method", messageResponseMethodsBySequence.get(seq));
                }

                // Clear the sequence from our lookup table, it's no longer needed.
                messageResponseMethodsBySequence.remove(seq);
            }

            // If we have a SequenceLock for this seq, the message is part of a blocking request/
            // reply, so stash it in place of lock, notify the lock and don't pass the message onto
            // the other listeners.
            if (sequenceLocks.indexOfKey(seq) >= 0) {
                Log.v(TAG, "Found " + seq + " in mSequenceLocks, synchronous response");
                Object lock = sequenceLocks.get(seq);
                sequenceResponses.put(seq, message);
                synchronized (lock) {
                    lock.notify();
                }
                sequenceLocks.remove(seq);
                return;
            }
        }

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

            throw new HTSPException("MessageDispatcher has no open Connection!");
        }
    }

    // Implement synchronous request for message.
    public HTSPMessage sendMessage(HTSPMessage message, int responseTimeout) throws HTSPException {

        long seq;
        if (!message.containsKey("seq")) {
            seq = (long) SYNC_SEQ;
            message.put("seq", seq);
        } else {
            seq = message.getLong("seq");
        }

        if(responseTimeout > 0)
        {
            Object lock = new Object();
            try {
                Log.v(TAG, "Putting " + seq + " into mSequenceLocks");
                sequenceLocks.put(seq, lock);

                sendMessage(message);

                synchronized (lock) {
                    try {
                        lock.wait(responseTimeout);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }

                return sequenceResponses.get(seq);
            } finally {
                sequenceLocks.remove(seq);
                sequenceResponses.remove(seq);
            }
        }

        return null;
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
