package com.openiptv.code.htsp;

public interface MessageListener {
    /**
     * This method is called by the MessageDispatcher whenever a new message has been received
     * from the TVHeadEnd server.
     * @param message incoming message
     */
    void onMessage(HTSPMessage message);
}
