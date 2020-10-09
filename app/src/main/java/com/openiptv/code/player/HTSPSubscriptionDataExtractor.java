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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;

import static com.openiptv.code.Constants.DEBUG;

class HTSPSubscriptionDataExtractor implements Extractor {
    private static final String TAG = HTSPSubscriptionDataExtractor.class.getName();

    /**
     * HTSPSeekMap which fakes seeking ability. The seeking should only occur on the server
     * side.
     */
    private class HTSPSeekMap implements SeekMap {
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

    private final Context context;
    private ExtractorOutput output;
    private final SparseArray<SourceReader> streamReaders = new SparseArray<>();

    /*
        Currently the byte buffer is set to 5MB
     */
    private final byte[] rawBytes = new byte[1024 * 1024 * 5];

    /**
     * Constructor for HTSPSubscriptionDataExtractor
     *
     * @param context application context
     */
    public HTSPSubscriptionDataExtractor(Context context) {
        this.context = context;
        Log.d(TAG, "New HtspExtractor instantiated");
    }

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
        this.output = output;
        this.output.seekMap(new HTSPSeekMap());
    }

    @Override
    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        int bytesRead = input.read(rawBytes, 0, rawBytes.length);
        if (DEBUG)
            Log.v(TAG, "Read " + bytesRead + " bytes");

        ObjectInputStream objectInput = null;

        try (
                // N.B. Don't add the objectInput to this bit, it breaks stuff
                ByteArrayInputStream inputStream = new ByteArrayInputStream(rawBytes, 0, bytesRead)
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

    /**
     * Internal Wrapper method used to identify the type of incoming HTSPMessage.
     *
     * @param message incoming message
     */
    private void handleMessage(@NonNull final HTSPMessage message) {
        final String method = message.getString("method");

        if (method.equals("subscriptionStart")) {
            handleSubscriptionStart(message);
        } else if (method.equals("muxpkt")) {
            handleMuxpkt(message);
        }
    }

    /**
     * Internal method used to parse a subscriptionStart HTSPMessage. This message indicates to the
     * application all of the available streams and their metadata.
     *
     * @param message subscriptionStart message
     */
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

            SourceReader streamReader = new SourceReader.Factory(context).build(streamType, mimeType);
            if (streamReader != null) {
                Log.d(TAG, "Creating StreamReader for " + streamType + " stream at index " + streamIndex);
                streamReader.buildTrackOutput(output, stream);
                streamReaders.put(streamIndex, streamReader);
            } else {
                Log.d(TAG, "Discarding stream at index " + streamIndex + ", no suitable StreamReader");
            }

        }

        Log.d(TAG, "All streams have now been handled");
        output.endTracks();
    }

    /**
     * Internal method used to parse a given HTSPMessage that has stream data.
     *
     * @param message stream data message
     */
    private void handleMuxpkt(@NonNull final HTSPMessage message) {
        int streamIndex = message.getInteger("stream");
        SourceReader streamReader = streamReaders.get(streamIndex);

        if (streamReader == null) {
            return;
        }

        streamReader.extract(message);
    }
}