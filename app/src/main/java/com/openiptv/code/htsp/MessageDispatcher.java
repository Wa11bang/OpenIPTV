package com.openiptv.code.htsp;

import androidx.annotation.NonNull;

public interface MessageDispatcher {
    /**
     * @param listener
     */
    void addMessageListener(MessageListener listener);

    /**
     * @param message
     * @throws HTSPException
     */
    void sendMessage(@NonNull HTSPMessage message) throws HTSPException;
}
