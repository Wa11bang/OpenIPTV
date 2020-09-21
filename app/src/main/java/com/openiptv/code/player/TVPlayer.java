package com.openiptv.code.player;

import android.content.Context;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.openiptv.code.epg.RecordedProgram;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.HTSPMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TVPlayer implements Player.EventListener {
    private SimpleExoPlayer player;
    private Context context;
    private Surface surface;
    private MediaSource mediaSource;
    private BaseConnection connection;
    private HTSPMessage streams[];
    private HTSPDataSource.Factory mHtspSubscriptionDataSourceFactory;
    private HTSPDataSource mDataSource;
    private ExtractorsFactory mExtractorsFactory;
    private boolean recording;
    private PlaybackParams playbackParams;
    private SeekableRunnable seekableRunnable;
    private Handler handler;

    private static final String URL = "http://tv.theron.co.nz:9981/dvrfile/c27bb93d8be4b0946e0f1cf840863e0e";
    private static final String TAG = TVPlayer.class.getSimpleName();


    public TVPlayer(Context context, SimpleExoPlayer player, BaseConnection connection)
    {
        Log.d("TVPlayer", "Created!");
        this.context = context;
        this.player = player;

        this.connection = connection;

        mHtspSubscriptionDataSourceFactory = new HTSPSubscriptionDataSource.Factory(context, connection, "htsp");

        // Produces Extractor instances for parsing the media data.
        mExtractorsFactory = new ExtendedExtractorsFactory(context);

        handler = new Handler();
    }

    public boolean setSurface(Surface surface)
    {
        this.surface = surface;
        player.setVideoSurface(surface);

        return true;
    }

    public void prepare(Uri channelUri, boolean recording)
    {
        this.recording = recording;

        if(!recording) {

            mediaSource = new ProgressiveMediaSource.Factory(mHtspSubscriptionDataSourceFactory, mExtractorsFactory).createMediaSource(channelUri);

            player.prepare(mediaSource);
        }
        else
        {
            Log.d("TVPlayer", "captured recording ID" + RecordedProgram.getRecordingIdFromRecordingUri(context, channelUri));

            byte[] toEncrypt = ("development" + ":" + "development").getBytes();
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "OpenIPTV").replace("ExoPlayerLib", "Blah"));

            dataSourceFactory.getDefaultRequestProperties().set("Authorization","Basic "+Base64.encodeToString(toEncrypt, Base64.DEFAULT));
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory).createMediaSource(Uri.parse(URL));

            player.prepare(videoSource);
        }
    }

    public void start()
    {
        player.setPlayWhenReady(true);
    }

    public void stop()
    {
        Log.d("TVPlayer", "Released Subscription");
        player.release();
        connection.stop();
        surface.release();
        mediaSource.releaseSource(null);
    }

    public void resume() {

        if(seekableRunnable != null)
        {
            seekableRunnable.stopRewind();
            seekableRunnable = null;
        }

        player.setPlayWhenReady(true);
        player.setPlaybackParameters(new PlaybackParameters(1));

        mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (mDataSource != null) {
            Log.d("TVPlayer", "Resuming HtspDataSource");
            ((HTSPSubscriptionDataSource)mDataSource).resume();
        } else {
            Log.w("TVPlayer", "Unable to resume, no HtspDataSource available");
        }
    }

    public void pause() {
        player.setPlayWhenReady(false);

        mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (mDataSource != null) {
            ((HTSPSubscriptionDataSource)mDataSource).pause();
        }
    }

    public void setPlaybackParams(PlaybackParams playbackParams)
    {
        player.setPlayWhenReady(false);
        if(seekableRunnable != null)
        {
            seekableRunnable.stopRewind();
            seekableRunnable = null;
        }

        this.playbackParams = playbackParams;
        mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (mDataSource != null) {
            Log.d("TVPlayer", "Resuming HtspDataSource");

            if(playbackParams.getSpeed() < 1)
            {
                Log.d(TAG, "REWINDING! - NOT SUPPORTED");
                //((HTSPSubscriptionDataSource) mDataSource).setSpeed(AndroidTVSpeedToTVH(playbackParams.getSpeed()));
                //seekableRunnable = new SeekableRunnable(player, (int) playbackParams.getSpeed(), (HTSPSubscriptionDataSource) mDataSource, (HTSPSubscriptionDataSource.Factory) mHtspSubscriptionDataSourceFactory);
                //seekableRunnable.startRewind();
                //rewindRunnable = new RewindRunnable(player, playbackParams, (HTSPSubscriptionDataSource) mDataSource);
                //handler.postAtFrontOfQueue(rewindRunnable);

                Toast.makeText(context, "Fast Rewind not Supported!", Toast.LENGTH_SHORT).show();
            }
            else {
                ((HTSPSubscriptionDataSource) mDataSource).setSpeed(AndroidTVSpeedToTVH(playbackParams.getSpeed()));
                player.setPlaybackParameters(new PlaybackParameters(playbackParams.getSpeed()));
                player.setPlayWhenReady(true);
            }
        }
    }

    public long getTimeshiftStartPosition() {
        mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (mDataSource != null) {
            long startTime = ((HTSPSubscriptionDataSource)mDataSource).getTimeshiftStartTime();
            if (startTime != -1) {
                // For live content
                return (startTime / 1000);
            } else {
                // For recorded content
                return 0;
            }
        } else {
            Log.w(TAG, "Unable to getTimeshiftStartPosition, no HtspDataSource available");
        }

        return -1;
    }

    public long getTimeshiftCurrentPosition() {
        mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (mDataSource != null) {
            long offset = ((HTSPSubscriptionDataSource)mDataSource).getTimeshiftOffsetPts();

            return Math.max((System.currentTimeMillis() + (offset / 1000)), getTimeshiftStartPosition());
                // For live content

        } else {
            Log.w(TAG, "Unable to getTimeshiftCurrentPosition, no HtspDataSource available");
        }

        return -1;
    }

    public void seek(long timeMs)
    {
        pause();

        if (mDataSource != null) {
            Log.d(TAG, "Seeking to time: " + timeMs);

            long seekPts = (timeMs * 1000) - ((HTSPSubscriptionDataSource)mDataSource).getTimeshiftStartTime();
            seekPts = Math.max(seekPts, ((HTSPSubscriptionDataSource)mDataSource).getTimeshiftStartPts()) / 1000;
            Log.d(TAG, "Seeking to PTS: " + seekPts);

            player.seekTo(seekPts);
            ((HTSPSubscriptionDataSource)mDataSource).seek(seekPts);


            //mediaSource.releaseSource(null);

            //player.prepare(mediaSource, false, false);
        } else {
            Log.w(TAG, "Unable to seek, no HtspDataSource available");
        }

        resume();
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (isLoading && !recording) {
            mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        }
    }

    public static int AndroidTVSpeedToTVH(float speed)
    {
        switch ((int) speed)
        {
            case 0:
            {
                return 100; // 1X
            }
            case 2: {
                return 200; // 2X
            }
            case 8:
            {
                return 300; // 3X
            }
            case 32:
            {
                return 400; // 4X
            }
            case 128:
            {
                return 500; // 5X
            }
            case -2: {
                return -200;
            }
            case -8:
            {
                return -300;
            }
            case -32:
            {
                return -400;
            }
            case -128:
            {
                return -500;
            }
        }

        return 100; // 1X
    }

    private static class SeekableRunnable
    {
        private Handler repeatUpdateHandler = new Handler();
        public int mValue;           //increment
        private boolean mAutoIncrement = false;          //for fast foward in real time
        private boolean mAutoDecrement = false;         // for rewind in real time
        private SimpleExoPlayer player;
        private HTSPSubscriptionDataSource dataSource;
        private HTSPSubscriptionDataSource.Factory htspDataSourceFactory;

        public SeekableRunnable(SimpleExoPlayer player, int speed, HTSPSubscriptionDataSource dataSource, HTSPSubscriptionDataSource.Factory htspDataSourceFactory)
        {
            this.player = player;
            this.mValue = speed;
            this.dataSource = dataSource;
            this.htspDataSourceFactory = htspDataSourceFactory;
        }

        private class Updater implements Runnable {
            public void run() {
                if( mAutoDecrement ){

                    dataSource = (HTSPSubscriptionDataSource) htspDataSourceFactory.getCurrentDataSource();
                    long seekPts = (mValue * 1000) + dataSource.getTimeshiftStartTime();
                    seekPts = Math.max(seekPts, dataSource.getTimeshiftStartPts()) / 1000;
                    Log.d(TAG, "Seeking to PTS DataSource: " + seekPts+1000);

                    dataSource.seek(seekPts);

                    long seekPtsPlayer = player.getCurrentPosition() + mValue;
                    Log.d(TAG, "Seeking to PTS Player: " + seekPtsPlayer+1000);

                    //dataSource.seek(seekPts);
                    player.seekTo(seekPtsPlayer);

                    repeatUpdateHandler.postDelayed( new Updater(), 50 );
                }
            }
        }

        public void startRewind()
        {
            mAutoDecrement = true;
            repeatUpdateHandler.post( new Updater() );
        }

        public void stopRewind()
        {
            mAutoDecrement = false;
        }
    }
}
