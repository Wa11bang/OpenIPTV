package com.openiptv.code.epg;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.openiptv.code.Constants;
import com.openiptv.code.htsp.HTSPMessage;

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.NULL_CHANNEL;

public class RecordedProgram {
    private static final String TAG = Program.class.getSimpleName();

    private int recordingId;
    private int eventId;
    private int channelId;
    private long start;
    private long end;
    private String title;
    private String summary;
    private String desc;
    private ContentValues contentValues;
    private Context context;

    /**
     * Constructor for a RecordedProgram object
     *
     * @param context application context
     */
    public RecordedProgram(Context context) {
        this.context = context;
    }

    /**
     * Sets the recordingId
     *
     * @param recordingId of object
     * @return this instance
     */
    public RecordedProgram setRecordingId(int recordingId) {
        this.recordingId = recordingId;
        return this;
    }

    /**
     * Sets the channelId
     *
     * @param channelId of object
     * @return this instance
     */
    public RecordedProgram setChannelId(int channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Sets the eventId
     *
     * @param eventId of object
     * @return this instance
     */
    public RecordedProgram setEventId(int eventId) {
        this.eventId = eventId;
        return this;
    }

    /**
     * Sets the start value (nanosecs)
     *
     * @param start of the recording
     * @return this instance
     */
    public RecordedProgram setStart(long start) {
        this.start = start;
        return this;
    }

    /**
     * Sets the end value (nanosecs)
     *
     * @param end of the recording
     * @return this instance
     */
    public RecordedProgram setEnd(long end) {
        this.end = end;
        return this;
    }

    /**
     * Sets the title
     *
     * @param title of the recording
     * @return this instance
     */
    public RecordedProgram setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets the summary
     *
     * @param summary of the recording
     * @return this instance
     */
    public RecordedProgram setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    /**
     * Sets the description
     *
     * @param desc of the recording
     * @return this instance
     */
    public RecordedProgram setDescription(String desc) {
        this.desc = desc;
        return this;
    }

    /**
     * Builds the RecordedProgram Object. Runs an internal method to build ContextValues.
     *
     * @return this instance
     */
    public RecordedProgram build() {
        generateContentValues(context);
        return this;
    }

    /**
     * Constructor for a RecordedProgram object, takes in a HTSPMessage which is then parsed
     * into recording data.
     *
     * @param context application context
     * @param message recording parsable HTSPMessage object
     */
    public RecordedProgram(Context context, HTSPMessage message) {
        this.recordingId = message.getInteger(Constants.RECORDED_PROGRAM_ID);
        this.eventId = message.getInteger(Constants.PROGRAM_ID);
        this.channelId = message.getInteger(Constants.RECORDED_PROGRAM_CHANNEL);
        this.start = message.getLong(Constants.PROGRAM_START_TIME);
        this.end = message.getLong(Constants.PROGRAM_FINISH_TIME);
        this.title = message.getString(Constants.PROGRAM_TITLE);
        this.summary = message.getString(Constants.PROGRAM_SUMMARY);
        this.desc = message.getString(Constants.PROGRAM_DESCRIPTION);

        // If an subscription error is received, it will be show as a toast to the user.
        // If no subscription error is found it will skip the switch statement.
        if (message.containsKey(Constants.SUBSCRIPTION_ERROR)) {
            switch (message.getString(Constants.SUBSCRIPTION_ERROR)) {
                case Constants.NO_FREE_ADAPTOR:
                    Toast.makeText(context, "No free adaptor for this service", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.SCRAMBLED:
                    Toast.makeText(context, "Service is scrambled", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.BAD_SIGNAL:
                    Toast.makeText(context, "Bad signal status", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.TUNING_FAILED:
                    Toast.makeText(context, "Tuning of this service failed", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.SUBSCRIPTION_OVERRIDDEN:
                    Toast.makeText(context, "Subscription overridden by another one.", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MUX_NOT_ENABLED:
                    Toast.makeText(context, "No mux enabled for this service.", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.INVALID_TARGET:
                    Toast.makeText(context, "Recording/livestream cannot be saved to filesystem or recording/streaming configuration is incorrect.", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.USER_ACCESS:
                    Toast.makeText(context, "User does not have access rights for this service.", Toast.LENGTH_SHORT).show();
                    break;
                case Constants.USER_LIMIT:
                    Toast.makeText(context, "Error user limit reached", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        if (DEBUG) {
            if (message.getHtspMessageArray("files", null) != null) {

                HTSPMessage[] files = message.getHtspMessageArray("files", null);

                for (HTSPMessage file : files) {
                    for (String key : file.keySet()) {
                        Log.d(TAG, key + " - " + file.get(key));
                    }
                }
            }
        }

        generateContentValues(context);
    }

    /**
     * Internal Method to generate ContentValues bundle
     *
     * @param context of the application
     */
    private void generateContentValues(Context context) {
        contentValues = new ContentValues();

        contentValues.put(TvContract.RecordedPrograms.COLUMN_INPUT_ID, TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS)));
        contentValues.put(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA, String.valueOf(recordingId));

        contentValues.put(TvContract.RecordedPrograms.COLUMN_CHANNEL_ID, Channel.getTvProviderId(channelId, context));

        if (title != null) {
            contentValues.put(TvContract.RecordedPrograms.COLUMN_TITLE, title);
        }

        contentValues.put(TvContract.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS, start * 1000);
        contentValues.put(TvContract.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS, end * 1000);

        long duration = end - start;
        contentValues.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS, duration * 1000);

        contentValues.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DATA_URI, String.valueOf(eventId));

        if (Constants.DEBUG) {
            Log.d(TAG, "Generated ContentValues for Program: " + this.eventId);
        }
    }

    /**
     * Returns the recordingId
     *
     * @return recordingId
     */
    public int getRecordingId() {
        return recordingId;
    }

    /**
     * Returns the eventId
     *
     * @return eventId
     */
    public int getEventId() {
        return eventId;
    }

    /**
     * Returns the channelId
     *
     * @return channelId
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * Returns the start value
     *
     * @return start
     */
    public long getStart() {
        return start;
    }

    /**
     * Returns the end value
     *
     * @return end
     */
    public long getEnd() {
        return end;
    }

    /**
     * Returns the title
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the summary
     *
     * @return summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Returns the description
     *
     * @return desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Returns the ContentValues
     *
     * @return contentValues
     */
    public ContentValues getContentValues() {
        return contentValues;
    }

    /**
     * Returns URI in TvProvider database, else NULL
     *
     * @return Program URI
     */
    public static Uri getUri(Context context, int channelId, int recordingId) {
        ContentResolver resolver = context.getContentResolver();
        long tvProviderChannelId = Channel.getTvProviderId(channelId, context);

        if (tvProviderChannelId == NULL_CHANNEL) {
            Log.w(TAG, "Failed to fetch programUri, unknown channel");
            return null;
        }

        Uri programsUri = TvContract.RecordedPrograms.CONTENT_URI.buildUpon().appendQueryParameter("id", String.valueOf(recordingId)).build();

        String[] projection = {TvContract.RecordedPrograms._ID, TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA};
        String strRecordingId = String.valueOf(recordingId);

        try (Cursor cursor = resolver.query(programsUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                if (strRecordingId.equals(cursor.getString(1))) {
                    if (DEBUG) {
                        Log.d(TAG, "Found existing Recording URI");
                    }
                    return TvContract.buildProgramUri(cursor.getLong(0));
                }
            }
        }
        return null;
    }

    /**
     * Gets the Internal TvProvider URI for a given RecordedProgram
     *
     * @param context application context
     * @param program RecordedProgram object to locate in TvProvider database
     * @return RecordedProgram URI
     */
    public static Uri getUri(Context context, RecordedProgram program) {
        return getUri(context, program.getChannelId(), program.getRecordingId());
    }

    /**
     * Returns the TvHeadEnd recordingId from a given RecordedProgram Uri. The method searches
     * the TvProvider database for the stored recordingId.
     *
     * @param context      application context
     * @param recordingUri uri used to locate recordingId in TvProvider database
     * @return TvHeadEnd recordingId
     */
    public static Integer getRecordingIdFromRecordingUri(Context context, Uri recordingUri) {
        ContentResolver resolver = context.getContentResolver();

        String[] projection = {TvContract.RecordedPrograms._ID, TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA};
        List<Integer> recordingIds = new ArrayList<>();

        try (Cursor cursor = resolver.query(recordingUri, projection, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                recordingIds.add(cursor.getInt(1));
            }
        }

        if (recordingIds.size() == 1) {
            return recordingIds.get(0);
        }

        return null;
    }
}
