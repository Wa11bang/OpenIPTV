package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;

/**
 * Creates a new AC3 Audio Stream Reader.
 */
public class AC3Reader extends AudioReader {
    public AC3Reader(Context context) {
        super(context, MimeTypes.AUDIO_AC3);
    }

    /**
     * Extracts initialization data from an htsp message.
     * @param message htsp Message to extract the data from.
     */
    @Override
    protected void buildInitializationData(HTSPMessage message) {
        // Ignore
    }
}
