package com.openiptv.code.epg;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;

import com.openiptv.code.Constants;
import com.openiptv.code.htsp.HTSPMessage;

import static com.openiptv.code.Constants.NULL_CHANNEL;

public class Channel {
    private static final String TAG = Channel.class.getSimpleName();

    private final int channelId;
    private final int channelNumber;
    private final int channelMinorNumber;
    private final String channelName;
    private ContentValues contentValues;

    public Channel(int channelId, int channelNumber, int channelMinorNumber, String channelName) {
        this.channelId = channelId;
        this.channelNumber = channelNumber;
        this.channelMinorNumber = channelMinorNumber;
        this.channelName = channelName;

        generateContentValues();
    }

    public Channel(HTSPMessage message)
    {
        this.channelId = message.getInteger(Constants.CHANNEL_ID);
        this.channelNumber = message.getInteger(Constants.CHANNEL_NUMBER);
        this.channelMinorNumber = message.getInteger(Constants.CHANNEL_NUMBER_MINOR);
        this.channelName = message.getString(Constants.CHANNEL_NAME);
    }

    public int getChannelId() {
        return channelId;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public int getChannelMinorNumber() {
        return channelMinorNumber;
    }

    public String getChannelName() {
        return channelName;
    }

    public ContentValues getContentValues()
    {
        return contentValues;
    }

    private void generateContentValues()
    {
        contentValues = new ContentValues();

        contentValues.put(TvContract.Channels.COLUMN_SERVICE_ID, channelId);
        contentValues.put(TvContract.Channels.COLUMN_TYPE, TvContract.Channels.TYPE_OTHER);
        contentValues.put(TvContract.Channels.COLUMN_SEARCHABLE, 1);
        contentValues.put(TvContract.Channels.COLUMN_INPUT_ID, TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));
        contentValues.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, channelNumber);
        contentValues.put(TvContract.Channels.COLUMN_DISPLAY_NAME, channelName);

        if(Constants.DEBUG)
        {
            Log.d(TAG, "Generated ContentValues for Channel: " + this.channelName);
        }
    }

    /**
     * Returns URI in TvProvider database, else NULL
     * @return Channel URI
     */
    public static Uri getUri(Context context, int channelId)
    {
        long tvProviderChannelId = getTvProviderId(channelId, context);
        if (tvProviderChannelId != NULL_CHANNEL) {
            return TvContract.buildChannelUri(tvProviderChannelId);
        }
        return null;
    }

    public static long getTvProviderId(int channelId, Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri channelsUri = buildChannelsUri();

        String[] projection = {TvContract.Channels._ID, TvContract.Channels.COLUMN_SERVICE_ID};

        try (Cursor cursor = resolver.query(channelsUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                if (cursor.getInt(1) == channelId) {
                    return cursor.getLong(0);
                }
            }
        }

        return NULL_CHANNEL;
    }

    public static long getTvProviderId(Channel channel, Context context) {
        return getTvProviderId(channel.getChannelId(), context);
    }

    public static long getTvProviderId(HTSPMessage message, Context context) {
        return getTvProviderId(message.getInteger(Constants.CHANNEL_ID), context);
    }

    public static Uri buildChannelsUri()
    {
        return TvContract.buildChannelsUriForInput(TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));
    }
}
