package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.openiptv.code.htsp.HTSPMessage;

/**
 * This is class is used as a factory to create new Video and Audio streams of various types.
 */
public abstract class SourceReader {

    /**
     * This factory returns the Source Reader of the type requested o null, if the type is invalid.
     */
    public static class Factory {
        private Context context;

        public Factory(Context context)
        {
            this.context = context;
        }

        /**
         *
         * @param type The format that the source is in.
         * @param mpegVersion If the format is an MPEG2 Audio stream, the version of of MPEG2 is required, otherwise just pass it null.
         * @return Returns a reader for the format specified in the arguments.
         */
        public SourceReader build(String type, String mpegVersion) {
            switch (type)
            {
                case "HEVC":
                {
                    return new HEVCReader(context);
                }
                case "H264":
                {
                    return new AVCReader(context);
                }
                case "MPEG2VIDEO":
                {
                    return new MPEG2VideoReader(context);
                }
                case "AC3":
                {
                    return new AC3Reader(context);
                }
                case "EAC3":
                {
                    return new EAC3Reader(context);
                }
                case "AAC":
                {
                    return new AACReader(context);
                }
                case "VORBIS":
                {
                    return new VorbisReader(context);
                }
                case "MPEG2AUDIO":
                {
                    return new MPEG2AudioReader(context, mpegVersion);
                }
                default:
                {
                    return null;
                }
            }
        }
    }

    private int trackType;
    private String sourceType;
    protected TrackOutput trackOutput;
    private Context context;


    /**
     * Set the track type
     * @param context
     * @param trackType
     */
    public SourceReader(Context context, int trackType)
    {
        this.context = context;
        this.trackType = trackType;
    }

    /**
     * Set the format type from a htsp message an extractor output
     * @param extractorOutput
     * @param streamMessage
     */
    public void buildTrackOutput(ExtractorOutput extractorOutput, HTSPMessage streamMessage)
    {
        int index = streamMessage.getInteger("index");

        sourceType = streamMessage.getString("type");
        trackOutput = extractorOutput.track(index, trackType);
        trackOutput.format(buildTrackFormat(streamMessage, index));
    }

    protected abstract Format buildTrackFormat(HTSPMessage message, int index);

    /**
     * extract stream data from a htsp message
     * @param streamMessage the message that has been received from the TVHeadend Server
     * @return If the message is successfully extracted, return true.
     */
    public boolean extract(HTSPMessage streamMessage)
    {
        byte[] payload = streamMessage.getByteArray("payload");

        if(payload == null || payload.length == 0)
        {
            // Error extracting stream from HTSPMessage
            return false;
        }

        long pts = streamMessage.getLong("pts");
        int frameType = streamMessage.getInteger("frametype", -1);
        int flags = 0;

        /**
         * If the track is a video track and of frameType "I" Or -1
         */
        if(trackType == C.TRACK_TYPE_VIDEO)
        {
            if(frameType == 'I' || frameType == -1)
            {
                flags |= C.BUFFER_FLAG_KEY_FRAME;
            }
        }
        else
        {
            flags |= C.BUFFER_FLAG_KEY_FRAME;
        }
        trackOutput.sampleData(new ParsableByteArray(payload), payload.length);
        trackOutput.sampleMetadata(pts, flags, payload.length, 0, null);

        return true;
    }

    /**
     * Converts Presentation timestamp to framerate
     * @param frameDuration
     * @return
     */
    protected static float PTSToFrameRate(int frameDuration) {
        float frameRate = Format.NO_VALUE;
        if (frameDuration != Format.NO_VALUE) {
            frameRate = 1000000 / (float) frameDuration;
        }
        return frameRate;
    }

    /**
     * Check if the byte has a CRC
     * @param b byte to check
     * @return true if it has a CRC
     */
    protected boolean hasCRC (byte b) {
        int data = b & 0xFF;
        return (data & 0x1) == 0;
    }
}
