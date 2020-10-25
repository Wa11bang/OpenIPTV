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

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.Constants.NULL_CHANNEL;

public class Channel {
    private static final String TAG = Channel.class.getSimpleName();

    private final int channelId;
    private final int channelNumber;
    private final int channelMinorNumber;
    private final String channelName;
    private ContentValues contentValues;

    /**
     * Constructor for a Channel Object
     *
     * @param channelId          the channelId associated with TVHeadend
     * @param channelNumber      the display channel number
     * @param channelMinorNumber the display minor-channel number
     * @param channelName        the channel name
     */
    public Channel(int channelId, int channelNumber, int channelMinorNumber, String channelName) {
        this.channelId = channelId;
        this.channelNumber = channelNumber;
        this.channelMinorNumber = channelMinorNumber;
        this.channelName = channelName;

        generateContentValues();
    }

    /**
     * Constructor for a Channel Object
     * @param message object which contains parsable channel data
     */
    public Channel(HTSPMessage message)
    {
        this.channelId = message.getInteger(Constants.CHANNEL_ID);
        this.channelNumber = message.getInteger(Constants.CHANNEL_NUMBER);
        this.channelMinorNumber = message.getInteger(Constants.CHANNEL_NUMBER_MINOR);
        this.channelName = message.getString(Constants.CHANNEL_NAME);

        generateContentValues();
    }

    /**
     * Returns the channelId
     * @return channelId
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * Returns the channelNumber
     * @return channelNumber
     */
    public int getChannelNumber() {
        return channelNumber;
    }

    /**
     * Returns the channelMinorNumber
     * @return channelMinorNumber
     */
    public int getChannelMinorNumber() {
        return channelMinorNumber;
    }

    /**
     * Returns the channelName
     * @return channelName
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Returns the contentValues bundle
     * @return contentValues
     */
    public ContentValues getContentValues()
    {
        return contentValues;
    }

    /**
     * Internal Method to generate ContentValues bundle
     */
    private void generateContentValues()
    {
        contentValues = new ContentValues();

        contentValues.put(TvContract.Channels.COLUMN_SERVICE_ID, channelId);
        contentValues.put(TvContract.Channels.COLUMN_TYPE, TvContract.Channels.TYPE_OTHER);
        contentValues.put(TvContract.Channels.COLUMN_SEARCHABLE, 1);
        contentValues.put(TvContract.Channels.COLUMN_INPUT_ID, TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));
        contentValues.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, channelNumber);
        contentValues.put(TvContract.Channels.COLUMN_DISPLAY_NAME, channelName);

        if (Constants.DEBUG) {
            Log.d(TAG, "Generated ContentValues for Channel: " + this.channelName);
        }
    }

    /**
     * Returns URI in TvProvider database, else NULL
     *
     * @return Channel URI
     */
    public static Uri getUri(Context context, int channelId) {
        long tvProviderChannelId = getTvProviderId(channelId, context);
        if (tvProviderChannelId != NULL_CHANNEL) {
            return TvContract.buildChannelUri(tvProviderChannelId);
        }
        return null;
    }

    /**
     * Gets the Internal TvProvider URI for a given Channel
     * @param context application context
     * @param channel Channel object to locate in TvProvider database
     * @return Channel URI
     */
    public static Uri getUri(Context context, Channel channel) {
        return getUri(context, channel.getChannelId());
    }

    /**
     * Returns the row ID for a given channelId stored in the TvProviders database.
     * @param channelId TvHeadEnd channelId
     * @param context application context
     * @return the internal channelId
     */
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

    /**
     * Returns the row ID for a given Channel Object stored in the TvProviders database.
     * @param channel Channel object
     * @param context application context
     * @return the internal channelId
     */
    public static long getTvProviderId(Channel channel, Context context) {
        return getTvProviderId(channel.getChannelId(), context);
    }

    /**
     * Returns the row ID for a given channelId that is parsed from a given HTSPMessage object.
     * The channelId is used to locate a Channel stored in the TvProviders database.
     * @param message message containing parsable channel data
     * @param context application context
     * @return the internal channelId
     */
    public static long getTvProviderId(HTSPMessage message, Context context) {
        return getTvProviderId(message.getInteger(Constants.CHANNEL_ID), context);
    }

    /**
     * Builds the Uri which identifies all of the channels stored in the TvProviders
     * database.
     * @return Single Uri for all Channels.
     */
    public static Uri buildChannelsUri()
    {
        return TvContract.buildChannelsUriForInput(TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));
    }

    /**
     * Returns the TvHeadEnd channelId from a given Channel Uri. The method searches the TvProvider
     * database for the stored channelId.
     * @param context application context
     * @param channelUri uri used to locate channelId in TvProvider database
     * @return TvHeadEnd channelId
     */
    public static Integer getChannelIdFromChannelUri(Context context, Uri channelUri) {
        ContentResolver resolver = context.getContentResolver();

        String[] projection = {TvContract.Channels._ID, TvContract.Channels.COLUMN_SERVICE_ID};
        List<Integer> channelIds = new ArrayList<>();

        try (Cursor cursor = resolver.query(channelUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                channelIds.add(cursor.getInt(1));
            }
        }

        if (channelIds.size() == 1) {
            return channelIds.get(0);
        }

        return null;
    }
}
