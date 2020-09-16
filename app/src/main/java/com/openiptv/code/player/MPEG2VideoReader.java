package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;

public class MPEG2VideoReader extends VideoReader {
    public MPEG2VideoReader(Context context) {
        super(context, MimeTypes.VIDEO_MPEG2);
    }

    @Override
    protected void buildInitializationData(HTSPMessage message) {
        // Ignore
    }
}
