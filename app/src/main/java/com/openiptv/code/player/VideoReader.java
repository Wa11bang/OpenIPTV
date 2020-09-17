package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.openiptv.code.htsp.HTSPMessage;

import java.util.List;

public abstract class VideoReader extends SourceReader {
    protected static final String TAG = VideoReader.class.getSimpleName();
    protected List<byte[]> initializationData;

    private String videoType;

    public VideoReader(Context context, String videoType) {
        super(context, C.TRACK_TYPE_VIDEO);
        this.videoType = videoType;
        this.initializationData = null;
    }

    @Override
    protected Format buildTrackFormat(HTSPMessage message, int index) {

        buildInitializationData(message);

        return Format.createVideoSampleFormat(
                Integer.toString(index),
                videoType,
                null,
                Format.NO_VALUE,
                Format.NO_VALUE,
                message.getInteger("width"),
                message.getInteger("height"),
                PTSToFrameRate(message.getInteger("duration", Format.NO_VALUE)),
                initializationData,
                null);
    }

    protected abstract void buildInitializationData(HTSPMessage message);
}
