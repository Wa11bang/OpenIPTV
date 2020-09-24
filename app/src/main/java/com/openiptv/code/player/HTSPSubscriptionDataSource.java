package com.openiptv.code.player;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.openiptv.code.epg.Channel;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.htsp.HTSPException;
import com.openiptv.code.htsp.Subscriber;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static com.openiptv.code.Constants.DEBUG;

public class HTSPSubscriptionDataSource extends HTSPDataSource implements Subscriber.Listener {
    private static final String TAG = HTSPSubscriptionDataSource.class.getName();
    private static final AtomicInteger dataSourceCount = new AtomicInteger();
    private static final int BUFFER_SIZE = 10*1024*1024;
    public static final byte[] HEADER = new byte[] {0,1,0,1,0,1,0,1};

    public static class Factory extends HTSPDataSource.Factory {
        private static final String TAG = Factory.class.getName();

        private final Context context;
        private final BaseConnection connection;
        private final String streamProfile;

        /**
         *
         * @param context
         * @param connection
         * @param streamProfile
         */
        public Factory(Context context, BaseConnection connection, String streamProfile) {
            this.context = context;
            this.connection = connection;
            this.streamProfile = streamProfile;
        }

        @Override
        public HTSPDataSource createDataSourceInternal() {
            return new HTSPSubscriptionDataSource(context, connection, streamProfile);
        }
    }

    private final String streamProfile;
    private final int dataSourceNumber;
    private Subscriber subscriber;
    private ByteBuffer buffer;
    private final ReentrantLock lock = new ReentrantLock();
    private boolean isOpen = false;
    private boolean isSubscribed = false;

    /**
     *
     * @param context
     * @param connection
     * @param streamProfile
     */
    private HTSPSubscriptionDataSource(Context context, BaseConnection connection, String streamProfile) {
        super(context, connection);

        this.streamProfile = streamProfile;
        dataSourceNumber = dataSourceCount.incrementAndGet();

        try {
            // Create the buffer, and place the HtspSubscriptionDataSource header in place.
            buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.limit(HEADER.length);
            buffer.put(HEADER);
            buffer.position(0);
        } catch (OutOfMemoryError e) {
            throw new RuntimeException("OutOfMemoryError when allocating HTSPSubscriptionDataSource buffer", e);
        }

        subscriber = new Subscriber(this.connection.getHTSPMessageDispatcher());
        subscriber.addSubscriptionListener(this);
    }

    @Override
    protected void finalize() throws Throwable {
        // This is a total hack, but there's not much else we can do?
        // https://github.com/google/ExoPlayer/issues/2662 - Luckily, i've not found it's actually
        // been used anywhere at this moment.
        if (subscriber != null || connection != null) {
            Log.e(TAG, "Datasource finalize relied upon to release the subscription");

            release();
        }

        super.finalize();
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {

    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        Log.i(TAG, "Opening HtspSubscriptionDataSource ("+ dataSourceNumber +")");
        this.dataSpec = dataSpec;

        if (!isSubscribed) {
            try {
                long channelId = Long.parseLong(Channel.getChannelIdFromChannelUri(context, dataSpec.uri).toString());
                subscriber.subscribe(channelId, streamProfile);
                isSubscribed = true;
            } catch (HTSPException e) {
                throw new IOException("Failed to open HtspSubscriptionDataSource, HTSP not connected (" + dataSourceNumber + ")", e);
            }
        }

        long seekPosition = this.dataSpec.position;
        if (seekPosition > 0) {
            Log.d(TAG, "Seek to time PTS: " + seekPosition);

            subscriber.seek(seekPosition);
            buffer.clear();
            buffer.limit(0);
        }

        isOpen = true;
        return dataSpec.length;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        }

        // IOException gets handled by calling method somehow. Catching the IOException leads to problems
        // that cause unnecessary handling.

        // If the buffer is empty, block until we have at least 1 byte of data
        while (isOpen && this.buffer.remaining() == 0) {
            try {
                if (DEBUG)
                    Log.v(TAG, "Blocking for more data ("+ dataSourceNumber +")");
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Ignore.
                return 0;
            }
        }

        if (!isOpen && this.buffer.remaining() == 0) {
            return C.RESULT_END_OF_INPUT;
        }

        int length;

        lock.lock();
        try {
            int remaining = this.buffer.remaining();
            length = Math.min(remaining, readLength);

            this.buffer.get(buffer, offset, length);
            this.buffer.compact();
            this.buffer.flip();
        } finally {
            lock.unlock();
        }

        return length;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return new HashMap<>();
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "Closing HTSP DataSource ("+ dataSourceNumber +")");
        isOpen = false;
    }

    // Subscription.Listener Methods
    @Override
    public void onSubscriptionStart(@NonNull HTSPMessage message) {
        Log.d(TAG, "Received subscriptionStart ("+ dataSourceNumber +")");
        serializeMessageToBuffer(message);
    }

    @Override
    public void onSubscriptionStatus(@NonNull HTSPMessage message) {
        // Don't care about this event here
    }

    @Override
    public void onSubscriptionStop(@NonNull HTSPMessage message) {
        Log.d(TAG, "Received subscriptionStop ("+ dataSourceNumber +")");
        isOpen = false;
    }

    /**
     *
     * @param timeMs
     */
    public void seek(long timeMs)
    {
        Log.d(TAG, "Wanting to see by " + timeMs);
        subscriber.seek(timeMs);
    }

    /**
     *
     * @return
     */
    public long getTimeshiftStartTime() {
        if (subscriber != null) {
            return subscriber.getTimeshiftStartTime();
        }

        return -1;
    }

    /**
     *
     * @return
     */
    public long getTimeshiftStartPts() {
        if (subscriber != null) {
            return subscriber.getTimeshiftStartPts();
        }

        return -1;
    }

    /**
     *
     * @return
     */
    public long getTimeshiftOffsetPts() {
        return subscriber.getTimeshiftOffsetPts();
    }

    /**
     *
     */
    public void pause() {
        if (subscriber != null) {
            subscriber.pause();
        }
    }

    /**
     *
     */
    public void resume() {
        if (subscriber != null) {
            subscriber.resume();
        }
    }

    /**
     *
     * @param speed
     */
    public void setSpeed(int speed)
    {
        subscriber.setSpeed(speed);
    }

    @Override
    public void onMuxpkt(@NonNull HTSPMessage message) {
        serializeMessageToBuffer(message);
    }


    public void release() {
        if (connection != null) {
            connection = null;
        }

        if (subscriber != null) {
            subscriber.removeSubscriptionListener(this);
            subscriber.unsubscribe();
            subscriber = null;
        }
    }

    /**
     *
     * @param message
     */
    private void serializeMessageToBuffer(@NonNull HTSPMessage message) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutput = new ObjectOutputStream(outputStream)
        ) {
            lock.lock();
            objectOutput.writeUnshared(message);
            objectOutput.flush();

            buffer.position(buffer.limit());
            buffer.limit(buffer.capacity());

            buffer.put(outputStream.toByteArray());

            buffer.flip();
        } catch (IOException | BufferOverflowException | IllegalArgumentException e) {
            // Ignore
        } finally {
            lock.unlock();
        }
    }
}