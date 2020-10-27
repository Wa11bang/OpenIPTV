package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;

/**
 * Creates a new MPEG2 Audio Stream Reader.
 */
public class MPEG2AudioReader extends AudioReader {
    public MPEG2AudioReader(Context context, String audioType) {
        super(context, audioType);
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
