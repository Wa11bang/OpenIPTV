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

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.NULL_CHANNEL;

public class Program {
    private static final String TAG = Program.class.getSimpleName();

    private int eventId;
    private int channelId;
    private long start;
    private long end;
    private String title;
    private String summary;
    private String desc;
    private int ageRating; // Minimum-age rating
    private String programImage;
    private ContentValues contentValues;
    private Context context;
    private String contentType;

    /**
     * Constructor for a Program object
     * @param context application context
     */
    public Program(Context context)
    {
        this.context = context;
    }

    /**
     * Sets the eventId
     * @param eventId of object
     * @return this instance
     */
    public Program setEventId(int eventId)
    {
        this.eventId = eventId;
        return this;
    }

    /**
     * Sets the channelId
     * @param channelId of object
     * @return this instance
     */
    public Program setChannelId(int channelId)
    {
        this.channelId = channelId;
        return this;
    }

    /**
     * Sets the start value (nanosecs)
     * @param start of the recording
     * @return this instance
     */
    public Program setStart(long start)
    {
        this.start = start;
        return this;
    }

    /**
     * Sets the end value (nanosecs)
     * @param end of the recording
     * @return this instance
     */
    public Program setEnd(long end)
    {
        this.end = end;
        return this;
    }

    /**
     * Sets the title
     * @param title of the recording
     * @return this instance
     */
    public Program setTitle(String title)
    {
        this.title = title;
        return this;
    }

    /**
     * Sets the summary
     * @param summary of the recording
     * @return this instance
     */
    public Program setSummary(String summary)
    {
        this.summary = summary;
        return this;
    }

    /**
     * Sets the description
     * @param desc of the recording
     * @return this instance
     */
    public Program setDescription(String desc)
    {
        this.desc = desc;
        return this;
    }

    /**
     * Sets the description
     * @param ageRating of the recording
     * @return this instance
     */
    public Program setAgeRating(int ageRating)
    {
        this.ageRating = ageRating;
        return this;
    }

    /**
     * Sets the description
     * @param programImage of the recording
     * @return this instance
     */
    public Program setProgramImage(String programImage)
    {
        this.programImage = programImage;
        return this;
    }

    /**
     * Builds the RecordedProgram Object. Runs an internal method to build ContextValues.
     * @return this instance
     */
    public Program build() {
        generateContentValues(context);
        return this;
    }

    /**
     * Constructor for a Program object, takes in a HTSPMessage which is then parsed
     * into program data.
     * @param context application context
     * @param message program parsable HTSPMessage object
     */
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
        this.contentType = new DvbContentType().getType(message.getInteger(Constants.PROGRAM_CONTENT_TYPE));

        generateContentValues(context);
    }

    /**
     * Internal Method to generate ContentValues bundle
     * @param context of the application
     */
    private void generateContentValues(Context context)
    {
        contentValues = new ContentValues();
        contentValues.put(TvContract.Programs.COLUMN_CANONICAL_GENRE,contentType);
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

    /**
     * Returns the eventId
     * @return eventId
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Returns the channelId
     * @return channelId
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * Returns the start value
     * @return start
     */
    public long getStart() {
        return start;
    }

    /**
     * Returns the end value
     * @return end
     */
    public long getEnd() {
        return end;
    }

    /**
     * Returns the title
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the summary
     * @return summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Returns the description
     * @return desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Returns the Age Rating for the Program (min age in years)
     * @return ageRating
     */
    public int getAgeRating() {
        return ageRating;
    }

    /**
     * Returns the Program Image
     * @return programImage
     */
    public String getProgramImage() {
        return programImage;
    }

    /**
     * Returns the ContentValues
     * @return contentValues
     */
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

    /**
     * Gets the Internal TvProvider URI for a given Program
     * @param context application context
     * @param program Program object to locate in TvProvider database
     * @return Program URI
     */
    public static Uri getUri(Context context, Program program)
    {
        return getUri(context, program.getChannelId(), program.getEventId());
    }

    /**
     * Returns the TvHeadEnd eventId from a given Program Uri. The method searches
     * the TvProvider database for the stored eventId.
     * @param context application context
     * @param programUri uri used to locate eventId in TvProvider database
     * @return TvHeadEnd eventId
     */
    public static Integer getProgramIdFromProgramUri(Context context, Uri programUri) {
        ContentResolver resolver = context.getContentResolver();

        String[] projection = {TvContract.Programs._ID, TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA};
        List<Integer> programIds = new ArrayList<>();

        try (Cursor cursor = resolver.query(programUri, projection, null,null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                programIds.add(cursor.getInt(1));
            }
        }

        if(programIds.size() == 1)
        {
            return programIds.get(0);
        }

        return null;
    }

    /**
     * Returns the TvHeadEnd start time from a given Program Uri. The method searches
     * the TvProvider database for the stored start time.
     * @param context application context
     * @param programUri uri used to locate start time in TvProvider database
     * @return TvHeadEnd start time
     */
    public static Integer getProgramStartFromProgramUri(Context context, Uri programUri) {
        ContentResolver resolver = context.getContentResolver();

        String[] projection = {TvContract.Programs._ID, TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS};
        List<Integer> programIds = new ArrayList<>();

        try (Cursor cursor = resolver.query(programUri, projection, null,null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                programIds.add(cursor.getInt(1));
            }
        }

        if(programIds.size() == 1)
        {
            return programIds.get(0);
        }

        return null;
    }

    /**
     * Returns the TvHeadEnd end time from a given Program Uri. The method searches
     * the TvProvider database for the stored end time.
     * @param context application context
     * @param programUri uri used to locate end time in TvProvider database
     * @return TvHeadEnd end time
     */
    public static Integer getProgramEndFromProgramUri(Context context, Uri programUri) {
        ContentResolver resolver = context.getContentResolver();

        String[] projection = {TvContract.Programs._ID, TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS};
        List<Integer> programIds = new ArrayList<>();

        try (Cursor cursor = resolver.query(programUri, projection, null,null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                programIds.add(cursor.getInt(1));
            }
        }

        if(programIds.size() == 1)
        {
            return programIds.get(0);
        }

        return null;
    }
}