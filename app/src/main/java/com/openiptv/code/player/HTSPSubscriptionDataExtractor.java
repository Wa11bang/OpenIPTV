package com.openiptv.code.player;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.extractor.SeekPoint;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.player.SourceReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import static com.openiptv.code.Constants.DEBUG;

class HTSPSubscriptionDataExtractor implements Extractor {
    private static final String TAG = HTSPSubscriptionDataExtractor.class.getName();

    private class HtspSeekMap implements SeekMap {
        @Override
        public boolean isSeekable() {
            return true;
        }

        @Override
        public long getDurationUs() {
            return C.TIME_UNSET;
        }

        @Override
        public SeekPoints getSeekPoints(long timeUs) {
            return new SeekPoints(new SeekPoint(timeUs, timeUs));
        }

    }

    private final Context mContext;
    private ExtractorOutput mOutput;
    private final SparseArray<SourceReader> streamReaders = new SparseArray<>();

    private final byte[] mRawBytes = new byte[1024 * 1024 * 5];

    public HTSPSubscriptionDataExtractor(Context context) {
        mContext = context;
        Log.d(TAG, "New HtspExtractor instantiated");
    }

    // Extractor Methods
    @Override
    public boolean sniff(ExtractorInput input) throws IOException, InterruptedException {
        ParsableByteArray scratch = new ParsableByteArray(HTSPSubscriptionDataSource.HEADER.length);

        // Find 8 bytes equal to HEADER at the start of the input.
        input.peekFully(scratch.data, 0, HTSPSubscriptionDataSource.HEADER.length);

        return Arrays.equals(scratch.data, HTSPSubscriptionDataSource.HEADER);

    }

    @Override
    public void init(ExtractorOutput output) {
        Log.i(TAG, "Initializing HTSP Extractor");
        mOutput = output;
        mOutput.seekMap(new HtspSeekMap());
    }

    @Override
    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        int bytesRead = input.read(mRawBytes, 0, mRawBytes.length);
        if (DEBUG)
            Log.v(TAG, "Read " + bytesRead + " bytes");

        ObjectInputStream objectInput = null;

        try (
                // N.B. Don't add the objectInput to this bit, it breaks stuff
                ByteArrayInputStream inputStream = new ByteArrayInputStream(mRawBytes, 0, bytesRead)
        ) {
            while (inputStream.available() > 0) {
                objectInput = new ObjectInputStream(inputStream);
                handleMessage((HTSPMessage) objectInput.readUnshared());
            }
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "Class Not Found");
        } finally {
            try {
                if (objectInput != null) {
                    objectInput.close();
                }
            } catch (IOException ex) {
                // Ignore
            }
        }

        return RESULT_CONTINUE;
    }

    @Override
    public void seek(long position, long timeUs) {
        Log.d(TAG, "Seeking HTSP Extractor to position:" + position + " and timeUs:" + timeUs);
    }

    @Override
    public void release() {
        Log.i(TAG, "Releasing HTSP Extractor");
        streamReaders.clear();
    }

    // Internal Methods
    private void handleMessage(@NonNull final HTSPMessage message) {
        final String method = message.getString("method");

        if (method.equals("subscriptionStart")) {
            handleSubscriptionStart(message);
        } else if (method.equals("muxpkt")) {
            handleMuxpkt(message);
        }
    }

    private void handleSubscriptionStart(@NonNull final HTSPMessage message) {
        Log.i(TAG, "Handling Subscription Start");

        for (HTSPMessage stream : message.getHtspMessageArray("streams")) {
            int streamIndex = stream.getInteger("index");
            String streamType = stream.getString("type");
            String mimeType = "";

            if (stream.containsKey("audio_version")) {
                switch (stream.getInteger("audio_version")) {
                    case 1: // MP1 Audio - V.Unlikely these days
                        mimeType = MimeTypes.AUDIO_MPEG_L1;
                        break;
                    case 2: // MP2 Audio - Pretty common in DVB streams
                        mimeType = MimeTypes.AUDIO_MPEG_L2;
                        break;
                    case 3: // MP3 Audio - Pretty common in IPTV streams
                        mimeType = MimeTypes.AUDIO_MPEG;
                        break;
                    default:
                        throw new RuntimeException("Unknown MPEG Audio Version: " + stream.getInteger("audio_version"));
                }
            }

            SourceReader streamReader = new SourceReader.Factory(mContext).build(streamType, mimeType);
            if (streamReader != null) {
                Log.d(TAG, "Creating StreamReader for " + streamType + " stream at index " + streamIndex);
                streamReader.buildTrackOutput(mOutput, stream);
                streamReaders.put(streamIndex, streamReader);
            } else {
                Log.d(TAG, "Discarding stream at index " + streamIndex + ", no suitable StreamReader");
            }

        }

        Log.d(TAG, "All streams have now been handled");
        mOutput.endTracks();
    }

    private void handleMuxpkt(@NonNull final HTSPMessage message) {
        int streamIndex = message.getInteger("stream");
        SourceReader streamReader = streamReaders.get(streamIndex);

        if (streamReader == null) {
            return;
        }

        streamReader.extract(message);
    }
}
