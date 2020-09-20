package com.openiptv.code.player;

import android.content.Context;
import android.media.PlaybackParams;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

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
        this.playbackParams = playbackParams;
        mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (mDataSource != null) {
            Log.d("TVPlayer", "Resuming HtspDataSource");
            ((HTSPSubscriptionDataSource) mDataSource).setSpeed(AndroidTVSpeedToTVH(playbackParams.getSpeed()));
            player.setPlaybackParameters(new PlaybackParameters(playbackParams.getSpeed()));
        }
    }

    public long getTimeshiftStartPosition() {
        mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (mDataSource != null) {
            long startTime = ((HTSPSubscriptionDataSource)mDataSource).getTimeshiftStartTime();
            if (startTime != -1) {
                // For live content
                return startTime / 1000;
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

                // For live content
                return System.currentTimeMillis() + (offset / 1000) + 2000;

        } else {
            Log.w(TAG, "Unable to getTimeshiftCurrentPosition, no HtspDataSource available");
        }

        return -1;
    }

    public void seek(long timeMs)
    {
        player.setPlayWhenReady(false);

        if (mDataSource != null) {
            Log.d(TAG, "Seeking to time: " + timeMs);

            long seekPts = (timeMs * 1000) - ((HTSPSubscriptionDataSource)mDataSource).getTimeshiftStartTime();
            seekPts = Math.max(seekPts, ((HTSPSubscriptionDataSource)mDataSource).getTimeshiftStartPts()) / 1000;
            Log.d(TAG, "Seeking to PTS: " + seekPts+1000);

            ((HTSPSubscriptionDataSource)mDataSource).seek(seekPts);
            player.seekTo(seekPts);
            //mediaSource.releaseSource(null);

            //player.prepare(mediaSource, false, false);
        } else {
            Log.w(TAG, "Unable to seek, no HtspDataSource available");
        }

        player.setPlayWhenReady(true);
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
}
