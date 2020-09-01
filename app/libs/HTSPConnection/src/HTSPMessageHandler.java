/*
    HTSP Message Library
    Author: Waldo Theron
    Version: 0.1
 */

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

public class HTSPMessageHandler {
    private final Set<MessageListener> listeners = new CopyOnWriteArraySet<>(); // High Performance thread-safe implementation
    private final Queue<HTSPMessage> pendingMessages = new ConcurrentLinkedQueue<>();
    private Connection connection;

    public HTSPMessageHandler() {
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void addMessageListener(MessageListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            return;
        }
        System.out.println("Listener already exists!");
    }

    public void removeMessageListener(MessageListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
            return;
        }
        System.out.println("Listener to remove does not exist!");
    }

    public void displayMessageKeys(HTSPMessage message)
    {
        for(String key : message.keySet())
        {
            System.out.println(key + ": " + message.get(key));
        }
    }

    public void onMessage(final HTSPMessage message) {
        //displayMessageKeys(message);

        if(message.containsKey("method") && message.get("method").equals("muxpkt"))
        {
            final long pts = message.getLong("pts");
            final int frameType = message.getInteger("frametype", -1);
            final byte[] payload = message.getByteArray("payload");

        }

        if(message.containsKey("method") && message.get("method").equals("subscriptionStart"))
        {
            if(message.containsKey("streams")) {
                for (HTSPMessage stream : message.getHtspMessageArray("streams")) {
                    final int streamIndex = stream.getInteger("index");
                    final String streamType = stream.getString("type");

                    System.out.println("Index: " + streamIndex + " Type: " + streamType);
                }
            }
        }

        for (final MessageListener listener : listeners) {
            listener.onMessage(message);
        }
    }

    public void sendMessage(HTSPMessage message) {
        if(!pendingMessages.contains(message))
        {
            if (connection != null) {
                connection.setWritePending();
            }
            pendingMessages.add(message);
            System.out.println("Added message to queue");
        }
    }

    public boolean hasPendingMessages() {
        return pendingMessages.size() > 0;
    }

    public HTSPMessage getMessage() {
        System.out.println("Dequeueing message for sending");
        return pendingMessages.remove();
    }
}
