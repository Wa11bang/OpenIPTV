package com.openiptv.code.timeshift;

import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.HTSPMessage;

public class TimeShift {
    // try to use HTSPMessage return type
    public static void rewind(BaseConnection connection, int subId)
    {
        HTSPMessage message = new HTSPMessage();
        message.put("method", "subscriptionSkip");
        message.put("subscriptionId", subId);


    }
}
