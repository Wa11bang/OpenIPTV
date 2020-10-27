package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;


public abstract class CaptionReader extends SourceReader {
    /**
     * Set the track type
     *
     * @param context
     * @param trackType
     */
    public CaptionReader(Context context, int trackType) {
        super(context, trackType);
    }
    // TODO

    protected Format buildTrackFormat(HTSPMessage message, int index) {
        Format format = Format.createTextSampleFormat(Integer.toString(index), MimeTypes.APPLICATION_SUBRIP, C.SELECTION_FLAG_AUTOSELECT, message.getString("language", "undefined"), null);

        return format;
    }
}
