package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.text.ssa.SsaDecoder;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.Util;
import com.openiptv.code.htsp.HTSPMessage;

import java.util.Arrays;
import java.util.Locale;

public class TeletextReader extends SourceReader {
    // TODO
    private static final String TAG = TeletextReader.class.getSimpleName();
    static byte[] SUBTITLE_PREFIX = new byte[]{49, 10, 48, 48, 58, 48, 48, 58, 48, 48, 44, 48, 48, 48, 32, 45, 45, 62, 32, 48, 48, 58, 48, 48, 58, 48, 48, 44, 48, 48, 48, 10}; //Length of 20
    static byte[] TIME_CODE_EMPTY = new byte[]{32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32}; //Length of 12

    private Context context;
    private TrackOutput trackOutput;

    /**
     * Set the track type
     *
     * @param context
     */
    public TeletextReader(Context context) {
        super(context, C.TRACK_TYPE_TEXT);
    }

    private static void setTeletextEndTimeCode(byte[] teletext, long timestampUs) {
        byte[] subtitleData;
        if (timestampUs == 0 || timestampUs == C.TIME_UNSET) {
            subtitleData = TIME_CODE_EMPTY;
        } else {
            int hours = (int) (timestampUs / (long) (3600 * 1000 * 1000));
            timestampUs -= hours * 3600 * 1000 * 1000;
            int minutes = (int) (timestampUs / (long) (600 * 1000 * 100));
            timestampUs -= minutes * 600 * 1000 * 100;
            int seconds = (int) (timestampUs / (long) (1000000));
            timestampUs -= seconds * 1000000;
            int milliseconds = (int) (timestampUs / (long) (1000));

            subtitleData = Util.getUtf8Bytes(String.format(Locale.ENGLISH, "%02d:%02d:%02d,%03d", hours, minutes, seconds, milliseconds));
        }
        System.arraycopy(subtitleData, 0, teletext, 19, 12);
    }

    @Override
    public boolean extract(HTSPMessage streamMessage) {
        long timestampSubtitleUs = streamMessage.getLong("pts");
        long subtitleDuration = streamMessage.getInteger("duration");
        byte[] subtitleData = Util.getUtf8Bytes(new String(streamMessage.getByteArray("payload")));

        int lengthOfSubtitle = SUBTITLE_PREFIX.length + subtitleData.length;

        byte[] subtitleSample = Arrays.copyOf(SUBTITLE_PREFIX, lengthOfSubtitle);
        System.arraycopy(subtitleData, 0, subtitleSample, SUBTITLE_PREFIX.length, subtitleData.length);

        setTeletextEndTimeCode(subtitleSample, subtitleDuration);

        trackOutput.sampleData(new ParsableByteArray(subtitleSample), lengthOfSubtitle);

        trackOutput.sampleMetadata(timestampSubtitleUs, C.BUFFER_FLAG_KEY_FRAME, lengthOfSubtitle, 0, null);

        return true;
    }

    @Override
    public void buildTrackOutput(ExtractorOutput extractorOutput, HTSPMessage streamMessage) {
        int index = streamMessage.getInteger("index");

        trackOutput = extractorOutput.track(index, C.TRACK_TYPE_TEXT);
        trackOutput.format(buildTrackFormat(streamMessage, index));
    }

    @Override
    protected Format buildTrackFormat(HTSPMessage message, int index) {
        return Format.createTextSampleFormat(Integer.toString(index), MimeTypes.APPLICATION_SUBRIP, C.SELECTION_FLAG_AUTOSELECT, message.getString("language", "undefined"), null);
    }
}
