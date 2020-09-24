package com.openiptv.code.htsp;

import androidx.annotation.NonNull;

public interface MessageDispatcher {
    void addMessageListener(MessageListener listener);
    void sendMessage(@NonNull HTSPMessage message) throws HTSPException;
}
