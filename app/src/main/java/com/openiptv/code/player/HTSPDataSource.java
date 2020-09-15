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

        public HTSPDataSource getCurrentDataSource() {
            if (currentDataSource != null) {
                return currentDataSource.get();
            }
            return null;
        }

        public void releaseCurrentDataSource() {
            if (currentDataSource != null) {
                currentDataSource.get().release();
                currentDataSource.clear();
                currentDataSource = null;
            }
        }

        protected abstract HTSPDataSource createDataSourceInternal();
    }

    public final Context context;
    public BaseConnection connection;
    public DataSpec dataSpec;

    public HTSPDataSource(Context context, BaseConnection connection) {
        this.context = context;
        this.connection = connection;
    }

    protected abstract void release();

    @Override
    public Uri getUri() {
        if (dataSpec != null) {
            return dataSpec.uri;
        }

        return null;
    }
}
