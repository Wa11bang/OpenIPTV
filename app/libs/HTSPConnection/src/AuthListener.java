/*
    HTSP Message Library
    Author: Waldo Theron
    Version: 0.1
 */

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AuthListener implements MessageListener
{
    private byte[] challenge;
    private HTSPMessageHandler htspMessageHandler;
    private ConnectionInfo connectionInfo;

    public AuthListener(HTSPMessageHandler messageHandler, ConnectionInfo connectionInfo)
    {
        this.htspMessageHandler = messageHandler;
        this.connectionInfo = connectionInfo;
    }

    @Override
    public void onMessage(HTSPMessage message) {
        if(message.containsKey("challenge"))
        {
            this.challenge = message.getByteArray("challenge");

            HTSPMessage authMessage = new HTSPMessage();

            authMessage.put("method", "authenticate");
            authMessage.put("username", connectionInfo.getUsername());
            authMessage.put("digest", calculateDigest(connectionInfo.getPassword(), this.challenge));

            htspMessageHandler.sendMessage(authMessage);
        }
    }

    private byte[] calculateDigest(String password, byte[] challenge) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Your platform doesn't support SHA-1");
        }

        try {
            md.update(password.getBytes("utf8"));
            md.update(challenge);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Your platform doesn't support UTF-8");
        }

        return md.digest();
    }
}