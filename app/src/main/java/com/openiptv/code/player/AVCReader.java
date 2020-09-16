package com.openiptv.code.player;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.video.AvcConfig;
import com.openiptv.code.htsp.HTSPMessage;

import static com.openiptv.code.Constants.DEBUG;

public class AVCReader extends VideoReader {
    public AVCReader(Context context) {
        super(context, MimeTypes.VIDEO_H264);
    }

    @Override
    protected void buildInitializationData(@NonNull HTSPMessage message) {
        // Build H264 Metadata
        if(message.containsKey("meta")) {
            try {
                AvcConfig avcConfig = AvcConfig.parse(new ParsableByteArray(message.getByteArray("meta")));
                this.initializationData = avcConfig.initializationData;

            } catch (ParserException e) {
                if (DEBUG) {
                    Log.d(TAG, "Cannot create Track Metadata!");
                }
            }
        }
    }
}
