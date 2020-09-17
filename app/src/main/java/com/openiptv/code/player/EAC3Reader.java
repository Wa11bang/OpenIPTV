package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;

public class EAC3Reader extends AudioReader {
    public EAC3Reader(Context context) {
        super(context, MimeTypes.AUDIO_E_AC3);
    }

    @Override
    protected void buildInitializationData(HTSPMessage message) {
        // Ignore
    }
}
