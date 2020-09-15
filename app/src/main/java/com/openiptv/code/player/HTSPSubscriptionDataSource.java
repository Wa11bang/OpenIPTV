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
import com.openiptv.code.htsp.HTSPNotConnectedException;
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
    private static final AtomicInteger sDataSourceCount = new AtomicInteger();
    private static final int BUFFER_SIZE = 10*1024*1024;
    public static final byte[] HEADER = new byte[] {0,1,0,1,0,1,0,1};

    public static class Factory extends HTSPDataSource.Factory {
        private static final String TAG = Factory.class.getName();

        private final Context mContext;
        private final BaseConnection mConnection;
        private final String mStreamProfile;

        public Factory(Context context, BaseConnection connection, String streamProfile) {
            mContext = context;
            mConnection = connection;
            mStreamProfile = streamProfile;
        }

        @Override
        public HTSPDataSource createDataSourceInternal() {
            return new HTSPSubscriptionDataSource(mContext, mConnection, mStreamProfile);
        }

    }

    private final String mStreamProfile;

    private final int mDataSourceNumber;
    private Subscriber mSubscriber;

    private ByteBuffer mBuffer;
    private final ReentrantLock mLock = new ReentrantLock();

    private boolean mIsOpen = false;
    private boolean mIsSubscribed = false;

    private HTSPSubscriptionDataSource(Context context, BaseConnection connection, String streamProfile) {
        super(context, connection);

        mStreamProfile = streamProfile;
        mDataSourceNumber = sDataSourceCount.incrementAndGet();

        Log.d(TAG, "New HtspSubscriptionDataSource instantiated ("+mDataSourceNumber+")");

        try {
            // Create the buffer, and place the HtspSubscriptionDataSource header in place.
            mBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            mBuffer.limit(HEADER.length);
            mBuffer.put(HEADER);
            mBuffer.position(0);
        } catch (OutOfMemoryError e) {
            // Since we're allocating a large buffer here, it's fairly safe to assume we'll have
            // enough memory to catch and throw this exception. We do this, as each OOM exception
            // message is unique (lots of #'s of bytes available/used/etc) and means crash reporting
            // doesn't group things nicely.
            throw new RuntimeException("OutOfMemoryError when allocating HtspSubscriptionDataSource buffer ("+mDataSourceNumber+")", e);
        }

        mSubscriber = new Subscriber(this.connection.getHTSPMessageDispatcher());
        mSubscriber.addSubscriptionListener(this);
    }

    @Override
    protected void finalize() throws Throwable {
        // This is a total hack, but there's not much else we can do?
        // https://github.com/google/ExoPlayer/issues/2662 - Luckily, i've not found it's actually
        // been used anywhere at this moment.
        if (mSubscriber != null || connection != null) {
            Log.e(TAG, "Datasource finalize relied upon to release the subscription");

            release();
        }

        super.finalize();
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {

    }

    // DataSource Methods
    @Override
    public long open(DataSpec dataSpec) throws IOException {
        Log.i(TAG, "Opening HtspSubscriptionDataSource ("+mDataSourceNumber+")");
        this.dataSpec = dataSpec;

        if (!mIsSubscribed) {
            try {
                Log.d(TAG + " -----", "" + Long.parseLong(Channel.getChannelIdFromChannelUri(context, dataSpec.uri).toString()));;
                long channelId = Long.parseLong(Channel.getChannelIdFromChannelUri(context, dataSpec.uri).toString());
                mSubscriber.subscribe(channelId, mStreamProfile);
                mIsSubscribed = true;
            } catch (HTSPNotConnectedException e) {
                throw new IOException("Failed to open HtspSubscriptionDataSource, HTSP not connected (" + mDataSourceNumber + ")", e);
            }
        }

        long seekPosition = this.dataSpec.position;
        if (seekPosition > 0) {
            Log.d(TAG, "Seek to time PTS: " + seekPosition);

            mSubscriber.skip(seekPosition);
            mBuffer.clear();
            mBuffer.limit(0);
        }

        mIsOpen = true;

        return C.LENGTH_UNSET;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        }

        // If the buffer is empty, block until we have at least 1 byte
        while (mIsOpen && mBuffer.remaining() == 0) {
            try {
                if (DEBUG)
                    Log.v(TAG, "Blocking for more data ("+mDataSourceNumber+")");
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // Ignore.
                return 0;
            }
        }

        if (!mIsOpen && mBuffer.remaining() == 0) {
            return C.RESULT_END_OF_INPUT;
        }

        int length;

        mLock.lock();
        try {
            int remaining = mBuffer.remaining();
            length = Math.min(remaining, readLength);

            mBuffer.get(buffer, offset, length);
            mBuffer.compact();
            mBuffer.flip();
        } finally {
            mLock.unlock();
        }

        return length;
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return new HashMap<>();
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "Closing HTSP DataSource ("+mDataSourceNumber+")");
        mIsOpen = false;
    }

    // Subscription.Listener Methods
    @Override
    public void onSubscriptionStart(@NonNull HTSPMessage message) {
        Log.d(TAG, "Received subscriptionStart ("+mDataSourceNumber+")");
        serializeMessageToBuffer(message);
    }

    @Override
    public void onSubscriptionStatus(@NonNull HTSPMessage message) {
        // Don't care about this event here
    }

    @Override
    public void onSubscriptionStop(@NonNull HTSPMessage message) {
        Log.d(TAG, "Received subscriptionStop ("+mDataSourceNumber+")");
        mIsOpen = false;
    }

    @Override
    public void onMuxpkt(@NonNull HTSPMessage message) {
        serializeMessageToBuffer(message);
    }

    // HtspDataSource Methods
    public void release() {
        if (connection != null) {
            connection = null;
        }

        if (mSubscriber != null) {
            mSubscriber.removeSubscriptionListener(this);
            mSubscriber.unsubscribe();
            mSubscriber = null;
        }
    }

    // Misc Internal Methods
    private void serializeMessageToBuffer(@NonNull HTSPMessage message) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutput = new ObjectOutputStream(outputStream)
        ) {
            mLock.lock();
            objectOutput.writeUnshared(message);
            objectOutput.flush();

            mBuffer.position(mBuffer.limit());
            mBuffer.limit(mBuffer.capacity());

            mBuffer.put(outputStream.toByteArray());

            mBuffer.flip();
        } catch (IOException e) {
            // Ignore?
        } catch (BufferOverflowException e) {
            // Ignore
        } finally {
            mLock.unlock();
            // Ignore
        }
    }
}
