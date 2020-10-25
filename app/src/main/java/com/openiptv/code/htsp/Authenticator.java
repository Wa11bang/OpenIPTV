package com.openiptv.code.htsp;

import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import static com.openiptv.code.Constants.UNIQUE_AUTH_SEQ_ID;

public class Authenticator implements MessageListener, Connection.Listener {
    private static final String TAG = Authenticator.class.getSimpleName();

    private final MessageDispatcher messageDispatcher;
    private final ConnectionInfo connectionInfo;
    private State state;
    private Set<Listener> listeners = new ArraySet<>();

    private boolean enableAsync = true;

    private static final int SEQ = UNIQUE_AUTH_SEQ_ID;

    /**
     * Enumeration of all the possible states an Authenticate object can be in.
     */
    public enum State {
        AUTHENTICATED,
        UNAUTHORISED,
        FAILED
    }

    /**
     * Interface used to communicate with other Classes and update the status of the authenticator.
     * Updates the state as above.
     */
    public interface Listener {
        void onAuthenticated(State state);
    }

    /**
     * Constructor for Authenticator Object
     * @param dispatcher     messageDispatcher, used for sending and receiving messages
     * @param connectionInfo used for the data when sending messages.
     */
    public Authenticator(MessageDispatcher dispatcher, ConnectionInfo connectionInfo) {
        this.messageDispatcher = dispatcher;
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void setConnection(@NonNull Connection connection) {

    }

    @Override
    public void onConnectionStateChange(@NonNull Connection.State state) {
        Log.d(TAG, "Received State - " + state.name());
        if (state == Connection.State.CONNECTED) {
            authenticate();
        }

        if (state == Connection.State.FAILED) {
            setState(State.FAILED);
        }
    }

    @Override
    public void onMessage(HTSPMessage message) {
        handleResponse(message);
    }

    /**
     * Internal method used to start the authentication process.
     */
    private void authenticate() {
        sendHelloRequest();
    }

    /**
     * Internal helper method used to handle any messages associated with the Authenticator.
     *
     * @param message auth messages
     */
    public void handleResponse(HTSPMessage message) {
        // Make sure that the incoming message has the sequence set.
        if (message.containsKey("seq") && message.getInteger("seq") == SEQ) {
            if (message.containsKey("noaccess") && message.getInteger("noaccess") == 1) {
                setState(State.UNAUTHORISED);
            } else {
                setState(State.AUTHENTICATED);
                sendEnableAsyncMessage();
            }
            return;
        }

        if (message.containsKey("error")) {
            setState(State.FAILED);
            return;
        }

        if (message.containsKey("challenge")) {
            sendAuthenticationRequest(message);
        }
    }

    /**
     * Internal method used to dispatch a HTSPMessage with the hello method.
     */
    public void sendHelloRequest() {
        HTSPMessage message = new HTSPMessage();

        message.put("method", "hello");
        message.put("htspversion", 23);
        message.put("clientname", connectionInfo.getClientName());
        message.put("clientversion", connectionInfo.getClientVersion());

        try {
            Log.d(TAG, "Sending Hello Message");
            messageDispatcher.sendMessage(message);
        } catch (HTSPException e) {
            Log.d(TAG, "Received HTSPException");
        }
    }

    /**
     * Internal method used to dispatch a HTSPMessage with the authenticate method. Requires a
     * challenge as a byte array, which is used for calculating the password digest.
     *
     * @param message hello response message
     */
    public void sendAuthenticationRequest(HTSPMessage message) {
        if (message.containsKey("challenge")) {
            byte[] challenge = message.getByteArray("challenge");

            HTSPMessage authMessage = new HTSPMessage();

            authMessage.put("method", "authenticate");
            authMessage.put("username", connectionInfo.getUsername());
            authMessage.put("digest", calculateDigest(connectionInfo.getPassword(), challenge));
            authMessage.put("seq", SEQ);

            try {
                Log.d(TAG, "Sending Authentication Message");
                messageDispatcher.sendMessage(authMessage);
            } catch (HTSPException e) {
                Log.d(TAG, "Received HTSPException");
                setState(State.FAILED);
            }
        }
    }

    /**
     * Internal method, used to send the enableAsyncMetaData method to the TVHeadEnd server.
     */
    public void sendEnableAsyncMessage() {
        boolean quickSync = true;
        long epgMaxTime = 0L;
        HTSPMessage enableAsyncMetadataRequest = new HTSPMessage();

        enableAsyncMetadataRequest.put("method", "enableAsyncMetadata");
        enableAsyncMetadataRequest.put("epg", 1);

        if (quickSync) {
            epgMaxTime = 7200 + (System.currentTimeMillis() / 1000L);
        } else {
            epgMaxTime = 691200 + (System.currentTimeMillis() / 1000L);
        }

        enableAsyncMetadataRequest.put("epgMaxTime", epgMaxTime);
        try {
            Log.d(TAG, "Sending EnableAsync Message");
            messageDispatcher.sendMessage(enableAsyncMetadataRequest);
        } catch (HTSPException e) {
            Log.d(TAG, "Received HTSPNotConnectedException");
            setState(State.FAILED);
        }
    }

    /**
     * Helper method used to calculate the SHA-1 hash digest of a password. Used when wanting to
     * authenticate with TVHeadEnd over HTSP.
     *
     * @param password  users password
     * @param challenge the given byte array acting as a challenge for the hash
     *                  (retrieved from the server)
     * @return the SHA-1 digest of the password
     */
    private byte[] calculateDigest(String password, byte[] challenge) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            setState(State.FAILED);
            throw new RuntimeException("Your platform doesn't support SHA-1");
        }

        md.update(password.getBytes(StandardCharsets.UTF_8));
        md.update(challenge);

        return md.digest();
    }

    /**
     * Add a new listener to the internal Listener list
     *
     * @param listener to be added
     * @return whether the add operation was successful
     */
    public boolean addListener(Listener listener) {
        return listeners.add(listener);
    }

    /**
     * Remove a listener from the internal Listener list,
     *
     * @param listener to be removed
     * @return whether the remove operation was successful
     */
    public boolean removeListener(Listener listener) {
        return listeners.remove(listener);
    }

    /**
     * Sets the current state of the Authenticator AND notifies each listener that the state has
     * also changed.
     *
     * @param state new state
     */
    public void setState(State state) {
        this.state = state;
        for (Listener listener : listeners) {
            listener.onAuthenticated(state);
        }
    }

    /**
     * Returns the current state of the Authenticator
     *
     * @return current state
     */
    public State getState() {
        return state;
    }
}
