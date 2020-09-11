package com.openiptv.code.epg;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class RecordedProgram {
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
