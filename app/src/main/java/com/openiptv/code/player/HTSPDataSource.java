package com.openiptv.code.player;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.openiptv.code.htsp.BaseConnection;

import java.io.Closeable;
import java.lang.ref.WeakReference;


public abstract class HTSPDataSource implements DataSource, Closeable {
    public static abstract class Factory implements DataSource.Factory {
        private static final String TAG = Factory.class.getName();

        private WeakReference<HTSPDataSource> currentDataSource;

        @Override
        public HTSPDataSource createDataSource() {
            releaseCurrentDataSource();

            currentDataSource = new WeakReference<>(createDataSourceInternal());
            return currentDataSource.get();
        }

        /**
         * Retrieves the current DataSource instance if it exists.
         * @return current DataSource instance
         */
        public HTSPDataSource getCurrentDataSource() {
            if (currentDataSource != null) {
                return currentDataSource.get();
            }
            return null;
        }

        /**
         * Destroys the current DataSource instance
         */
        public void releaseCurrentDataSource() {
            if (currentDataSource != null) {
                currentDataSource.get().release();
                currentDataSource.clear();
                currentDataSource = null;
            }
        }

        /**
         * Abstract method used to create the initial DataSource object.
         * @return single instance of HTSPDataSource
         */
        protected abstract HTSPDataSource createDataSourceInternal();
    }

    public final Context context;
    public BaseConnection connection;
    public DataSpec dataSpec;

    /**
     * Constructor for a HTSPDataSource object
     * @param context application context
     * @param connection BaseConnection used to subscribe to TV Channels
     */
    public HTSPDataSource(Context context, BaseConnection connection) {
        this.context = context;
        this.connection = connection;
    }

    /**
     * Abstract method to release the DataSource, part of implementation.
     */
    protected abstract void release();

    @Override
    public Uri getUri() {
        if (dataSpec != null) {
            return dataSpec.uri;
        }

        return null;
    }
}
