package com.openiptv.code.epg;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;

import com.openiptv.code.Constants;
import com.openiptv.code.htsp.HTSPMessage;

import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.NULL_CHANNEL;

public class Program {
    private static final String TAG = Program.class.getSimpleName();

    private final int eventId;
    private final int channelId;
    private final long start;
    private final long end;
    private String title;
    private String summary;
    private String desc;
    private int ageRating;
    private String programImage;
    private ContentValues contentValues;

    public Program(Context context, int eventId, int channelId, long start, long end, String title, String summary, String desc, int ageRating, String programImage) {
        this.eventId = eventId;
        this.channelId = channelId;
        this.start = start;
        this.end = end;
        this.title = title;
        this.summary = summary;
        this.desc = desc;
        this.ageRating = ageRating;
        this.programImage = programImage;

        generateContentValues(context);
    }

    public Program(Context context, int eventId, int channelId, long start, long end) {
        this.eventId = eventId;
        this.channelId = channelId;
        this.start = start;
        this.end = end;

        generateContentValues(context);
    }


    public Program(Context context, HTSPMessage message)
    {
        this.eventId = message.getInteger(Constants.PROGRAM_ID);
        this.channelId = message.getInteger(Constants.CHANNEL_ID);
        this.start = message.getLong(Constants.PROGRAM_START_TIME);
        this.end = message.getLong(Constants.PROGRAM_FINISH_TIME);
        this.title = message.getString(Constants.PROGRAM_TITLE);
        this.summary = message.getString(Constants.PROGRAM_SUMMARY);
        this.desc = message.getString(Constants.PROGRAM_DESCRIPTION);
        this.ageRating = message.getInteger(Constants.PROGRAM_AGE_RATING);
        this.programImage = message.getString(Constants.PROGRAM_IMAGE);

        generateContentValues(context);
    }

    private void generateContentValues(Context context)
    {
        contentValues = new ContentValues();

        contentValues.put(TvContract.Programs.COLUMN_CHANNEL_ID, Channel.getTvProviderId(channelId, context));
        contentValues.put(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA, eventId);

        if (title != null) {
            contentValues.put(TvContract.Programs.COLUMN_TITLE, title);
        }

        if (summary != null && desc != null) {
            // If we have both summary and desc, use it.
            contentValues.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, summary);
            contentValues.put(TvContract.Programs.COLUMN_LONG_DESCRIPTION, desc);

        } else if (summary != null) {
            // If we have only summary, use it.
            contentValues.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, summary);

        } else if (desc != null) {
            // If we have only description, use it.
            contentValues.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, desc);
        }

        if (ageRating != 0) {
            if (ageRating >= 4 && ageRating <= 18) {
                TvContentRating rating = TvContentRating.createRating("com.android.tv", "DVB", "DVB_" + ageRating);
                contentValues.put(TvContract.Programs.COLUMN_CONTENT_RATING, rating.flattenToString());
            }
        }

        if(programImage != null) {
            contentValues.put(TvContract.Programs.COLUMN_POSTER_ART_URI, programImage);
            if (Constants.DEBUG) {
                Log.d(TAG, "Program image uri: " + programImage);
            }
        }

        contentValues.put(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, start * 1000);
        contentValues.put(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, end * 1000);

        if(Constants.DEBUG)
        {
            Log.d(TAG, "Generated ContentValues for Program: " + this.eventId);
        }
    }

    public int getEventId() {
        return eventId;
    }

    public int getChannelId() {
        return channelId;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getDesc() {
        return desc;
    }

    public int getAgeRating() {
        return ageRating;
    }

    public String getProgramImage() {
        return programImage;
    }

    public ContentValues getContentValues() {
        return contentValues;
    }

    /**
     * Returns URI in TvProvider database, else NULL
     * @return Program URI
     */
    public static Uri getUri(Context context, int channelId, int eventId)
    {
        ContentResolver resolver = context.getContentResolver();
        long tvProviderChannelId = Channel.getTvProviderId(channelId, context);

        if (tvProviderChannelId == NULL_CHANNEL) {
            Log.w(TAG, "Failed to fetch programUri, unknown channel");
            return null;
        }

        Uri programsUri = TvContract.buildProgramsUriForChannel(tvProviderChannelId);
        String[] projection = {TvContract.Programs._ID, TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA};
        String strEventId = String.valueOf(eventId);

        try (Cursor cursor = resolver.query(programsUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                if (strEventId.equals(cursor.getString(1))) {
                    if(DEBUG) {
                        Log.d(TAG, "Found existing Program URI");
                    }
                    return TvContract.buildProgramUri(cursor.getLong(0));
                }
            }
        }
        return null;
    }

    public static Uri getUri(Context context, Program program)
    {
        return getUri(context, program.getChannelId(), program.getEventId());
    }
}
