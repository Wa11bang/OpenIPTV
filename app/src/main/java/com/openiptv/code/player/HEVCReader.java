package com.openiptv.code.player;

import android.content.Context;
import android.util.Log;

import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.video.HevcConfig;
import com.openiptv.code.htsp.HTSPMessage;

import static com.openiptv.code.Constants.DEBUG;

/**
 * Creates a new HEVC (H.265) Video Stream Reader.
 */
public class HEVCReader extends VideoReader {
    public HEVCReader(Context context) {
        super(context, MimeTypes.VIDEO_H265);
    }

    /**
     * Extracts initialization data from an htsp message.
     *
     * @param message htsp Message to extract the data from.
     */
    @Override
    protected void buildInitializationData(HTSPMessage message) {
        // Build H265 Metadata
        if (message.containsKey("meta")) {
            try {
                HevcConfig hevcConfig = HevcConfig.parse(new ParsableByteArray(message.getByteArray("meta")));
                this.initializationData = hevcConfig.initializationData;

            } catch (ParserException e) {
                if (DEBUG) {
                    Log.d(TAG, "Cannot create Track Metadata!");
                }
            }
        }
    }
}
