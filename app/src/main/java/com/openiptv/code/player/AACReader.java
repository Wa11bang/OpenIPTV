package com.openiptv.code.player;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.CodecSpecificDataUtil;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.openiptv.code.htsp.HTSPMessage;

import java.util.Collections;

public class AACReader extends AudioReader
{
    private static final int ADTS_HEADER_SIZE = 7;
    private static final int ADTS_CRC_SIZE = 2;

    public AACReader(Context context) {
        super(context, MimeTypes.AUDIO_AAC);
    }

    @Override
    protected void buildInitializationData(HTSPMessage message) {
        int rate = Format.NO_VALUE;
        if (message.containsKey("rate")) {
            rate = getSampleRate(message.getInteger("rate"));
        }

        if (message.containsKey("meta")) {
            initializationData = Collections.singletonList(message.getByteArray("meta"));
        } else {
            initializationData = Collections.singletonList(CodecSpecificDataUtil.buildAacLcAudioSpecificConfig(rate, message.getInteger("channels", Format.NO_VALUE)));
        }
    }

    @Override
    public boolean extract (@NonNull HTSPMessage message){
        byte[] payload = message.getByteArray("payload");

        if(payload == null || payload.length == 0)
        {
            // Error extracting stream from HTSPMessage
            return false;
        }

        long pts = message.getLong("pts");
        int flags = C.BUFFER_FLAG_KEY_FRAME;

        ParsableByteArray pba = new ParsableByteArray(payload);

        int skipLength;

        if (hasCRC(payload[1])) {
            // AAC has ADTS CRC Header
            skipLength = ADTS_HEADER_SIZE + ADTS_CRC_SIZE;
        } else {
            // AAC has no ADTS CRC Header
            skipLength = ADTS_HEADER_SIZE;
        }

        pba.skipBytes(skipLength);
        int frameLength = payload.length - skipLength;

        trackOutput.sampleData(pba, frameLength);
        trackOutput.sampleMetadata(pts, flags, frameLength, 0, null);

        return true;
    }
}