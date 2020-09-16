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

import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.NULL_CHANNEL;

public class RecordedProgram {
    private static final String TAG = Program.class.getSimpleName();

    private final int recordingId;
    private final int eventId;
    private final int channelId;
    private final long start;
    private final long end;
    private String title;
    private String summary;
    private String desc;
    private ContentValues contentValues;

    public RecordedProgram(Context context, int recordingId,int eventId, int channelId, long start, long end, String title, String summary, String desc) {
        this.recordingId = recordingId;
        this.eventId = eventId;
        this.channelId = channelId;
        this.start = start;
        this.end = end;
        this.title = title;
        this.summary = summary;
        this.desc = desc;

        generateContentValues(context);
    }

    public RecordedProgram(Context context, int recordingId,int eventId, int channelId, long start, long end) {
        this.recordingId = recordingId;
        this.eventId = eventId;
        this.channelId = channelId;
        this.start = start;
        this.end = end;

        generateContentValues(context);
    }


    public RecordedProgram(Context context, HTSPMessage message)
    {
        this.recordingId = message.getInteger(Constants.RECORDED_PROGRAM_ID);
        this.eventId = message.getInteger(Constants.PROGRAM_ID);
        this.channelId = message.getInteger(Constants.RECORDED_PROGRAM_CHANNEL);
        this.start = message.getLong(Constants.PROGRAM_START_TIME);
        this.end = message.getLong(Constants.PROGRAM_FINISH_TIME);
        this.title = message.getString(Constants.PROGRAM_TITLE);
        this.summary = message.getString(Constants.PROGRAM_SUMMARY);
        this.desc = message.getString(Constants.PROGRAM_DESCRIPTION);

        if(DEBUG) {
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

    private void generateContentValues(Context context)
    {
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

        if(Constants.DEBUG)
        {
            Log.d(TAG, "Generated ContentValues for Program: " + this.eventId);
        }
    }

    public int getRecordingId()
    {
        return recordingId;
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

    public ContentValues getContentValues() {
        return contentValues;
    }

    /**
     * Returns URI in TvProvider database, else NULL
     * @return Program URI
     */
    public static Uri getUri(Context context, int channelId, int recordingId)
    {
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
                    if(DEBUG) {
                        Log.d(TAG, "Found existing Recording URI");
                    }
                    return TvContract.buildProgramUri(cursor.getLong(0));
                }
            }
        }
        return null;
    }

    public static Uri getUri(Context context, RecordedProgram program)
    {
        return getUri(context, program.getChannelId(), program.getRecordingId());
    }

    public static Integer getRecordingIdFromRecordingUri(Context context, Uri recordingUri) {
        ContentResolver resolver = context.getContentResolver();

        String[] projection = {TvContract.RecordedPrograms._ID, TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA};
        List<Integer> channelIds = new ArrayList<>();

        try (Cursor cursor = resolver.query(recordingUri, projection, null,null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                channelIds.add(cursor.getInt(1));
            }
        }

        if(channelIds.size() == 1)
        {
            return channelIds.get(0);
        }

        return null;
    }
}
