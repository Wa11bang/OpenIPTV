package com.openiptv.code.epg;

import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;

import com.openiptv.code.Constants;
import com.openiptv.code.R;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.htsp.MessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.openiptv.code.Constants.CHANNEL_ID;
import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.PROGRAM_AGE_RATING;
import static com.openiptv.code.Constants.PROGRAM_DESCRIPTION;
import static com.openiptv.code.Constants.PROGRAM_FINISH_TIME;
import static com.openiptv.code.Constants.PROGRAM_ID;
import static com.openiptv.code.Constants.PROGRAM_START_TIME;
import static com.openiptv.code.Constants.PROGRAM_SUMMARY;
import static com.openiptv.code.Constants.PROGRAM_TITLE;

public class EPGCaptureTask implements MessageListener {
    private static final String TAG = EPGCaptureTask.class.getSimpleName();
    private static final HashSet<String> METHODS = new HashSet<>(Arrays.asList("channelAdd", "eventAdd", "channelUpdate", "eventUpdate", "initialSyncCompleted"));

    private boolean fullSync;
    private Context context;
    private BaseConnection connection;
    private ConnectionInfo connectionInfo;
    private Set<Listener> syncListeners;

    public interface Listener
    {
        void onSyncComplete();
    }

    public EPGCaptureTask(Context context, boolean fullSync)
    {
        this(context);
        this.fullSync = fullSync;
        connection.fullSync();
    }

    public EPGCaptureTask(Context context)
    {
        if(Constants.DEBUG) {
            Log.d(TAG, "Started EPGCaptureTask");
        }

        connection = new BaseConnection(new ConnectionInfo("10.0.0.57", 9982, "development", "development", "MetaCapture", "23"));
        //connection.fullSync();

        // Link ourselves to the BaseConnection to listen for when we have received HTSP Messages.
        // The base connection has an instance of a HTSPMessageDispatcher


        // Start HTSP Connection
        connection.start();
        connection.addMessageListener(this);

        this.context = context;
        syncListeners = new ArraySet<>();

    }

    public void addSyncListener(Listener listener)
    {
        Log.d(TAG, "Added sync listener");
        syncListeners.add(listener);
    }

    public void captureChannels(HTSPMessage channelMessage)
    {
        Channel channel = new Channel(channelMessage);

        Uri channelUri = Channel.getUri(context, channel);

        if (channelUri == null) {
            Log.d(TAG, "Channel ID: " + context.getContentResolver().insert(TvContract.Channels.CONTENT_URI, channel.getContentValues()));
        }
        else {
            Log.d(TAG, "Channel already exists");
        }

        /*ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(TvContract.Channels.CONTENT_URI)
                .withValues(channelContent).build());

        try {
            context.getContentResolver().applyBatch(TvContract.AUTHORITY, operations);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }*/
    }

    public void capturePrograms(HTSPMessage programMessage)
    {
        Program program = new Program(context, programMessage);
        Uri eventUri = Program.getUri(context, program);

        if (eventUri == null) {
            context.getContentResolver().insert(TvContract.Programs.CONTENT_URI, program.getContentValues());
            if(DEBUG)
            {
                Log.d(TAG, "Adding program: " +program.getTitle());
            }
        } else {
            if(DEBUG)
            {
                Log.d(TAG, "Updating Program - " + program.getTitle());
            }
            context.getContentResolver().update(TvContract.Programs.CONTENT_URI, program.getContentValues(), null, null);
        }

        /*ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(TvContract.Programs.CONTENT_URI)
                .withValues(values).build());
        try {
            context.getContentResolver().applyBatch(TvContract.AUTHORITY, operations);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }*/
    }

    public void initialCompleted(HTSPMessage message)
    {
        Log.d(TAG, "Initial Sync Complete");
        for(Listener l : syncListeners)
        {
            l.onSyncComplete();
        }
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
                case "eventUpdate":
                {
                    Log.d(TAG, "Updating Event" + message.getString("title"));
                    capturePrograms(message);
                    break;
                }
                case "initialSyncCompleted":
                {
                    Log.d(TAG, "got initial compeleted");
                    initialCompleted(message);
                    break;
                }
            }
        }
    }
}