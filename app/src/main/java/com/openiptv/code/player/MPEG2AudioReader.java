package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;

public class MPEG2AudioReader extends AudioReader {
    public MPEG2AudioReader(Context context, String audioType) {
        super(context, audioType);
    }

    @Override
    protected void buildInitializationData(HTSPMessage message) {
        // Ignore
    }
}
