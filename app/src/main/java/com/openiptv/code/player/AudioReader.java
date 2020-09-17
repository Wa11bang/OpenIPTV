package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.openiptv.code.Constants;
import com.openiptv.code.htsp.HTSPMessage;

import java.util.List;

public abstract class AudioReader extends SourceReader {
    protected static final String TAG = AudioReader.class.getSimpleName();
    protected List<byte[]> initializationData;

    private String audioType;

    public AudioReader(Context context, String audioType) {
        super(context, C.TRACK_TYPE_AUDIO);
        this.audioType = audioType;
        this.initializationData = null;
    }

    @Override
    protected Format buildTrackFormat(HTSPMessage message, int index) {

        int rate = Format.NO_VALUE;
        if (message.containsKey("rate")) {
            rate = getSampleRate(message.getInteger("rate"));
        }

        buildInitializationData(message);

        return Format.createAudioSampleFormat(
                Integer.toString(index),
                audioType,
                null,
                Format.NO_VALUE,
                Format.NO_VALUE,
                message.getInteger("channels", Format.NO_VALUE),
                rate,
                C.ENCODING_PCM_16BIT,
                initializationData,
                null,
                C.SELECTION_FLAG_AUTOSELECT,
                message.getString("language", "und")
        );
    }

    protected abstract void buildInitializationData(HTSPMessage message);

    public static int getSampleRate(int sri)
    {
        return Constants.AUDIO_SAMPLE_RATES[sri & 0xF];
    }
}