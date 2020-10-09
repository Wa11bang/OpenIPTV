package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;

/**
 * Creates a new MPEG2 Video Stream Reader.
 */
public class MPEG2VideoReader extends VideoReader {
    public MPEG2VideoReader(Context context) {
        super(context, MimeTypes.VIDEO_MPEG2);
    }

    /**
     * Extracts initialization data from an htsp message.
     *
     * @param message htsp Message to extract the data from.
     */
    @Override
    protected void buildInitializationData(HTSPMessage message) {
        // Ignore
    }
}
