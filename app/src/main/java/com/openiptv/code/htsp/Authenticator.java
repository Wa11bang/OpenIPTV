package com.openiptv.code.htsp;

import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;



public class Authenticator implements MessageListener, Connection.ConnectionListener {
    private static final String TAG = Authenticator.class.getSimpleName();
    private final MessageDispatcher messageDispatcher;
    private final ConnectionInfo connectionInfo;
    private Connection connection;
    private boolean fullSync = false;
    private State state;
    private Set<Listener> listeners = new ArraySet<>();
    private static final int SEQ = 10;

    public enum State {
        AUTHENTICATED,
        UNAUTHORISED,
        FAILED
    }

    public interface Listener {
        void onAuthenticated(State state);
    }

    public Authenticator(MessageDispatcher dispatcher, ConnectionInfo connectionInfo)
    {
        this.messageDispatcher = dispatcher;
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void setConnection(@NonNull Connection connection) {
        this.connection = connection;
    }

    @Override
    public void onConnectionStateChange(@NonNull ConnectionState state) {
        Log.d(TAG, "Received State - " + state.name());
        if (state == ConnectionState.CONNECTED) {
            authenticate();
        }

        if(state == ConnectionState.FAILED)
        {
            setState(State.FAILED);
        }
    }

    @Override
    public void onMessage(HTSPMessage message) {
            handleResponse(message);
    }

    private void authenticate()
    {
        sendHelloRequest();
    }

    public void handleResponse(HTSPMessage message)
    {
        if(message.containsKey("seq") && message.getInteger("seq") == SEQ)
        {
            if(message.containsKey("noaccess") && message.getInteger("noaccess") == 1)
            {
                setState(State.UNAUTHORISED);
            }
            else
            {
                setState(State.AUTHENTICATED);
                sendEnableAsyncMessage();
            }
            return;
        }

        if(message.containsKey("error"))
        {
            setState(State.FAILED);
            return;
        }

        if(message.containsKey("challenge"))
        {
            sendAuthenticationRequest(message);
            return;
        }
    }

    public void sendHelloRequest()
    {
        HTSPMessage message = new HTSPMessage();

        message.put("method", "hello");
        message.put("htspversion", 23);
        message.put("clientname", connectionInfo.getClientName());
        message.put("clientversion", connectionInfo.getClientVersion());

        try {
            Log.d(TAG, "Sending Hello Message");
            messageDispatcher.sendMessage(message);
        } catch (HTSPNotConnectedException e) {
            Log.d(TAG, "Received HTSPNotConnectedException");
        }
    }

    public void sendAuthenticationRequest(HTSPMessage message)
    {
        if(message.containsKey("challenge"))
        {
            byte[] challenge = message.getByteArray("challenge");

            HTSPMessage authMessage = new HTSPMessage();

            authMessage.put("method", "authenticate");
            authMessage.put("username", connectionInfo.getUsername());
            authMessage.put("digest", calculateDigest(connectionInfo.getPassword(), challenge));
            authMessage.put("seq", SEQ);

            try {
                Log.d(TAG, "Sending Authentication Message");
                messageDispatcher.sendMessage(authMessage);
            } catch (HTSPNotConnectedException e) {
                Log.d(TAG, "Received HTSPNotConnectedException");
                setState(State.FAILED);
            }
        }
    }

    public void sendEnableAsyncMessage()
    {
        fullSync = false;
        long epgMaxTime = 0L;
        HTSPMessage enableAsyncMetadataRequest = new HTSPMessage();

        enableAsyncMetadataRequest.put("method", "enableAsyncMetadata");
        enableAsyncMetadataRequest.put("epg", 1);

        if(fullSync) {
            Log.d(TAG, "Full sync enabled");
            epgMaxTime = 691200 + (System.currentTimeMillis() / 1000L);
        }
        else {
            epgMaxTime = 7200 + (System.currentTimeMillis() / 1000L);
        }
        Log.d(TAG, "MAX TIME: "+epgMaxTime);

        enableAsyncMetadataRequest.put("epgMaxTime", epgMaxTime);

        try {
            Log.d(TAG, "Sending EnableAsync Message");
            messageDispatcher.sendMessage(enableAsyncMetadataRequest);
        } catch (HTSPNotConnectedException e) {
            Log.d(TAG, "Received HTSPNotConnectedException");
            setState(State.FAILED);
        }
    }

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

    public boolean addListener(Listener listener)
    {
        return listeners.add(listener);
    }

    public boolean removeListener(Listener listener)
    {
        return listeners.remove(listener);
    }

    public void setState(State state) {
        this.state = state;
        for (Listener listener : listeners) {
            listener.onAuthenticated(state);
        }
    }

    public State getState()
    {
        return state;
    }
}