package com.openiptv.code.epg;

import android.content.Context;
import android.util.Log;

import com.openiptv.code.Constants;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.htsp.MessageListener;

import java.util.Arrays;
import java.util.HashSet;

public class EPGCaptureTask  implements MessageListener {
    private static final String TAG = EPGCaptureTask.class.getSimpleName();
    private static final HashSet<String> METHODS = new HashSet<>(Arrays.asList("channelAdd", "eventAdd", "channelUpdate", "eventUpdate", "\n" +
            "initialSyncCompleted"));

    private BaseConnection connection;
    private ConnectionInfo connectionInfo;

    public EPGCaptureTask(Context context)
    {
        if(Constants.DEBUG) {
            Log.d(TAG, "Started EPGCaptureTask");
        }

        // Get Account Details for TVHeadend
        connectionInfo = new ConnectionInfo("10.0.0.57", 9982, "development", "development", "MetaCapture", "23");

        // Create HTSP Connection
        connection = new BaseConnection(connectionInfo);

        // Link ourselves to the BaseConnection to listen for when we have received HTSP Messages.
        // The base connection has an instance of a HTSPMessageDispatcher
        connection.addMessageListener(this);

        // Start HTSP Connection
        connection.start();
    }

    public void captureChannels(HTSPMessage channelMessage)
    {

    }

    public void capturePrograms(HTSPMessage programMessage)
    {

    }

    public void initialCompleted(HTSPMessage message)
    {

    }

    @Override
    public void onMessage(HTSPMessage message) {
        if(message.getString("method") != null && METHODS.contains(message.getString("method")))
        {
            switch (message.getString("method"))
            {
                case "channelAdd":
                {
                    captureChannels(message);
                    break;
                }
                case "eventAdd":
                {
                    capturePrograms(message);
                    break;
                }
                case "initialSyncCompleted":
                {
                    initialCompleted(message);
                    break;
                }
            }
        }
    }
}
