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

import com.google.android.media.tv.companionlibrary.model.Channel;
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

import static android.provider.CalendarContract.Instances.EVENT_ID;
import static com.openiptv.code.Constants.CHANNEL_ID;
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

    private final SparseArray<Uri> mChannelUriMap;

    private boolean fullSync;
    private Context context;
    private BaseConnection connection;
    private ConnectionInfo connectionInfo;
    private Set<Listener> syncListeners;
    private List<Channel> channels;

    public interface Listener
    {
        void onSyncComplete();
    }

    public EPGCaptureTask(Context context, boolean fullSync)
    {
        this(context);
        this.fullSync = fullSync;
    }

    public EPGCaptureTask(Context context)
    {
        if(Constants.DEBUG) {
            Log.d(TAG, "Started EPGCaptureTask");
        }

        connection = new BaseConnection(new ConnectionInfo("10.0.0.57", 9982, "development", "development", "MetaCapture", "23"));
        connection.fullSync();

        // Link ourselves to the BaseConnection to listen for when we have received HTSP Messages.
        // The base connection has an instance of a HTSPMessageDispatcher


        // Start HTSP Connection
        connection.start();
        connection.addMessageListener(this);

        this.context = context;
        syncListeners = new ArraySet<>();

        mChannelUriMap = buildChannelUriMap(context);

    }

    public void addSyncListener(Listener listener)
    {
        Log.d(TAG, "Added sync listener");
        syncListeners.add(listener);
    }

    public void captureChannels(HTSPMessage channelMessage)
    {
        ContentValues channelContent = getChannelContentValues(channelMessage);

        Uri channelUri = getChannelUri(context, channelMessage.getInteger("channelId"));

        if (channelUri == null) {
            Log.d(TAG, "Channel ID: " + context.getContentResolver().insert(TvContract.Channels.CONTENT_URI, channelContent));
        }
        else {
            Log.d(TAG, "Channel already exists");
            Log.d(TAG, "Channel ID: " + context.getContentResolver().update(TvContract.Channels.CONTENT_URI, channelContent, null , null));
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

    private ContentValues getChannelContentValues(HTSPMessage channelMessage)
    {
        int channelId = channelMessage.getInteger(CHANNEL_ID);
        int channelNumber = channelMessage.getInteger(Constants.CHANNEL_NUMBER);
        String channelName = channelMessage.getString(Constants.CHANNEL_NAME);

        ContentValues contentValues = new ContentValues();

        contentValues.put(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID, channelId);
        contentValues.put(TvContract.Channels.COLUMN_TYPE, TvContract.Channels.TYPE_OTHER);
        contentValues.put(TvContract.Channels.COLUMN_SEARCHABLE, 1);
        contentValues.put(TvContract.Channels.COLUMN_APP_LINK_TEXT, "Change this");
        contentValues.put(TvContract.Channels.COLUMN_APP_LINK_COLOR, Color.CYAN);
        contentValues.put(TvContract.Channels.COLUMN_INPUT_ID, TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));
        contentValues.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, channelNumber);
        contentValues.put(TvContract.Channels.COLUMN_DISPLAY_NAME, channelName);

        if(Constants.DEBUG)
        {
            Log.d(TAG, "Adding channel: " + channelName);
        }

        return contentValues;
    }

    public List<Channel> getChannels()
    {
        return channels;
    }

    public void capturePrograms(HTSPMessage programMessage)
    {
        ContentValues values = new ContentValues();

        Uri eventUri = getProgramUri(context, programMessage.getInteger(CHANNEL_ID), programMessage.getInteger(PROGRAM_ID));



        values.put(TvContract.Programs.COLUMN_CHANNEL_ID, getChannelId(context, programMessage.getInteger(CHANNEL_ID)));
        values.put(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA, String.valueOf(programMessage.getInteger(PROGRAM_ID)));

        // COLUMN_TITLE, COLUMN_EPISODE_TITLE, and COLUMN_SHORT_DESCRIPTION are used in the
        // Live Channels app EPG Grid. COLUMN_LONG_DESCRIPTION appears unused.
        // On Sony TVs, COLUMN_LONG_DESCRIPTION is used for the "more info" display.

        if (programMessage.containsKey(PROGRAM_TITLE)) {
            // The title of this TV program.
            values.put(TvContract.Programs.COLUMN_TITLE, programMessage.getString(PROGRAM_TITLE));
        }

        if (programMessage.containsKey(PROGRAM_SUMMARY) && programMessage.containsKey(PROGRAM_DESCRIPTION)) {
            // If we have both summary and description... use them both
            values.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, programMessage.getString(PROGRAM_SUMMARY));
            values.put(TvContract.Programs.COLUMN_LONG_DESCRIPTION, programMessage.getString(PROGRAM_DESCRIPTION));

        } else if (programMessage.containsKey(PROGRAM_SUMMARY) && !programMessage.containsKey(PROGRAM_DESCRIPTION)) {
            // If we have only summary, use it.
            values.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, programMessage.getString(PROGRAM_SUMMARY));

        } else if (!programMessage.containsKey(PROGRAM_SUMMARY) && programMessage.containsKey(PROGRAM_DESCRIPTION)) {
            // If we have only description, use it.
            values.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, programMessage.getString(PROGRAM_DESCRIPTION));
        }

        if (programMessage.containsKey(PROGRAM_AGE_RATING)) {
            final int ageRating = programMessage.getInteger(PROGRAM_AGE_RATING);
            if (ageRating >= 4 && ageRating <= 18) {
                TvContentRating rating = TvContentRating.createRating("com.android.tv", "DVB", "DVB_" + ageRating);
                values.put(TvContract.Programs.COLUMN_CONTENT_RATING, rating.flattenToString());
            }
        }

        if (programMessage.containsKey(PROGRAM_START_TIME)) {
            values.put(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, programMessage.getLong(PROGRAM_START_TIME) * 1000);
        }

        if (programMessage.containsKey(PROGRAM_FINISH_TIME)) {
            values.put(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, programMessage.getLong(PROGRAM_FINISH_TIME) * 1000);
        }

        if (eventUri == null) {
            context.getContentResolver().insert(TvContract.Programs.CONTENT_URI, values);
        } else {
            Log.d(TAG, "Updating Program - " + programMessage.getString("title"));
            context.getContentResolver().update(TvContract.Programs.CONTENT_URI, values, null, null);
        }



        /*ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        operations.add(ContentProviderOperation.newInsert(TvContract.Programs.CONTENT_URI)
                .withValues(values).build());
        try {
            context.getContentResolver().applyBatch(TvContract.AUTHORITY, operations);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }*/

        if(Constants.DEBUG)
        {
            Log.d(TAG, "Adding program: " +programMessage.getString(PROGRAM_TITLE));
        }
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

    public static long getChannelId(Context context, int channelId) {
        ContentResolver resolver = context.getContentResolver();

        Uri channelsUri = TvContract.buildChannelsUriForInput(TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));

        String[] projection = {TvContract.Channels._ID, TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID};

        try (Cursor cursor = resolver.query(channelsUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                if (cursor.getInt(1) == channelId) {
                    return cursor.getLong(0);
                }
            }
        }

        return -1;
    }

    public static Uri getChannelUri(Context context, int channelId) {
        long androidChannelId = getChannelId(context, channelId);

        if (androidChannelId != -1) {
            return TvContract.buildChannelUri(androidChannelId);
        }

        return null;
    }

    public static Uri getProgramUri(Context context, int channelId, int eventId) {
        ContentResolver resolver = context.getContentResolver();

        long androidChannelId = getChannelId(context, channelId);

        if (androidChannelId == -1) {
            Log.w(TAG, "Failed to fetch programUri, unknown channel");
            return null;
        }

        Uri programsUri = TvContract.buildProgramsUriForChannel(androidChannelId);

        String[] projection = {TvContract.Programs._ID, TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA};

        String strEventId = String.valueOf(eventId);

        try (Cursor cursor = resolver.query(programsUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                if (strEventId.equals(cursor.getString(1))) {
                    Log.d(TAG, "Found existing Program URI");
                    return TvContract.buildProgramUri(cursor.getLong(0));
                }
            }
        }

        return null;
    }

    public static SparseArray<Uri> buildChannelUriMap(Context context) {
        ContentResolver resolver = context.getContentResolver();

        // Create a map from original network ID to channel row ID for existing channels.
        SparseArray<Uri> channelMap = new SparseArray<>();
        Uri channelsUri = TvContract.buildChannelsUriForInput(TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));
        String[] projection = {TvContract.Channels._ID, TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID};

        try (Cursor cursor = resolver.query(channelsUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                int originalNetworkId = cursor.getInt(1);
                channelMap.put(originalNetworkId, TvContract.buildChannelUri(rowId));
            }
        }

        return channelMap;
    }
}