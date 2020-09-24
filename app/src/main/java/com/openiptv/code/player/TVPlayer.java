package com.openiptv.code.player;

import android.content.Context;
import android.media.PlaybackParams;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.openiptv.code.epg.RecordedProgram;
import com.openiptv.code.htsp.BaseConnection;

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.Constants.DEBUG;

public class TVPlayer implements Player.EventListener {
    private SimpleExoPlayer player;
    private Context context;
    private Surface surface;
    private MediaSource mediaSource;
    private BaseConnection connection;
    private HTSPDataSource.Factory HTSPSubscriptionDataSourceFactory;
    private HTSPDataSource dataSource;
    private ExtractorsFactory extractorsFactory;

    private boolean recording;
    private List<Listener> listeners;
    private DefaultTrackSelector trackSelector;
    private float currentVolume;
    private PlaybackParams playbackParams;
    private SeekableRunnable seekableRunnable;
    private Handler handler;

    private static final String URL = "http://tv.theron.co.nz:9981/dvrfile/c27bb93d8be4b0946e0f1cf840863e0e";
    private static final String TAG = TVPlayer.class.getSimpleName();

    public interface Listener {
        void onTracks(List<TvTrackInfo> tracks, SparseArray<String> selectedTracks);
    }

    public TVPlayer(Context context, BaseConnection connection)
    {
        Log.d("TVPlayer", "Created!");
        this.context = context;

        trackSelector = new DefaultTrackSelector(context);
        this.player = new SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build();

        this.player.addListener(this);
        this.connection = connection;

        HTSPSubscriptionDataSourceFactory = new HTSPSubscriptionDataSource.Factory(context, connection, "htsp");
        extractorsFactory = new ExtendedExtractorsFactory(context);

        listeners = new ArrayList<>();
    }

    public boolean setSurface(Surface surface) {
        this.surface = surface;
        player.setVideoSurface(surface);

        return true;
    }

    public void prepare(Uri channelUri, boolean recording) {
        this.recording = recording;

        if (!recording) {

            mediaSource = new ProgressiveMediaSource.Factory(HTSPSubscriptionDataSourceFactory, extractorsFactory).createMediaSource(channelUri);

            player.prepare(mediaSource);
        } else {
            Log.d("TVPlayer", "captured recording ID" + RecordedProgram.getRecordingIdFromRecordingUri(context, channelUri));

            byte[] toEncrypt = ("development" + ":" + "development").getBytes();
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "OpenIPTV").replace("ExoPlayerLib", "Blah"));

            dataSourceFactory.getDefaultRequestProperties().set("Authorization", "Basic " + Base64.encodeToString(toEncrypt, Base64.DEFAULT));
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory).createMediaSource(Uri.parse(URL));

            player.prepare(videoSource);
        }
    }

    public void start() {
        player.setPlayWhenReady(true);
    }

    public void stop() {
        Log.d("TVPlayer", "Released Subscription");
        player.release();
        connection.stop();
        if(surface != null) {
            surface.release();
        }
        mediaSource.releaseSource(null);
    }

    public void resume() {

        /*if(seekableRunnable != null)
        {
            seekableRunnable.stopRewind();
            seekableRunnable = null;
        }*/

        player.setPlayWhenReady(true);
        player.setPlaybackParameters(new PlaybackParameters(1));

        dataSource = HTSPSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            Log.d("TVPlayer", "Resuming HtspDataSource");
            ((HTSPSubscriptionDataSource) dataSource).resume();
        } else {
            Log.w("TVPlayer", "Unable to resume, no HtspDataSource available");
        }
    }

    public void pause() {
        player.setPlayWhenReady(false);

        dataSource = HTSPSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            ((HTSPSubscriptionDataSource) dataSource).pause();
        }
    }

    public void setPlaybackParams(PlaybackParams playbackParams)
    {
        player.setPlayWhenReady(false);
        /*if(seekableRunnable != null)
        {
            seekableRunnable.stopRewind();
            seekableRunnable = null;
        }*/

        //this.playbackParams = playbackParams;
        dataSource = HTSPSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            Log.d("TVPlayer", "Resuming HtspDataSource");

            if(playbackParams.getSpeed() < 1)
            {
                Log.d(TAG, "REWINDING! - NOT SUPPORTED");
                //((HTSPSubscriptionDataSource) mDataSource).setSpeed(AndroidTVSpeedToTVH(playbackParams.getSpeed()));
                //seekableRunnable = new SeekableRunnable(player, (int) playbackParams.getSpeed(), (HTSPSubscriptionDataSource) mDataSource, (HTSPSubscriptionDataSource.Factory) mHtspSubscriptionDataSourceFactory);
                //seekableRunnable.startRewind();

                player.setPlayWhenReady(true);
                Toast.makeText(context, "Fast Rewind not Supported!", Toast.LENGTH_SHORT).show();
            }
            else {
                ((HTSPSubscriptionDataSource) dataSource).setSpeed(AndroidTVSpeedToTVH(playbackParams.getSpeed()));
                player.setPlaybackParameters(new PlaybackParameters(playbackParams.getSpeed()));
                player.setPlayWhenReady(true);
            }
        }
    }

    public long getTimeshiftStartPosition() {
        dataSource = HTSPSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            long startTime = ((HTSPSubscriptionDataSource) dataSource).getTimeshiftStartTime();
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
        dataSource = HTSPSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            long offset = ((HTSPSubscriptionDataSource) dataSource).getTimeshiftOffsetPts();

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

        if (dataSource != null) {
            Log.d(TAG, "Seeking to time: " + timeMs);

            long seekPts = (timeMs * 1000) - ((HTSPSubscriptionDataSource) dataSource).getTimeshiftStartTime();
            seekPts = Math.max(seekPts, ((HTSPSubscriptionDataSource) dataSource).getTimeshiftStartPts()) / 1000;
            Log.d(TAG, "Seeking to PTS: " + seekPts);
            Log.d(TAG, "BEFORE Player Position: " + player.getCurrentPosition() + ", DataSource Position: " + getTimeshiftCurrentPosition() + ", Offset: " +((HTSPSubscriptionDataSource) dataSource).getTimeshiftOffsetPts());


            ((HTSPSubscriptionDataSource) dataSource).seek(seekPts);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            player.seekTo(seekPts);
            Log.d(TAG, "AFTER Player Position: " + player.getCurrentPosition() + ", DataSource Position: " + getTimeshiftCurrentPosition() + ", Offset: " +((HTSPSubscriptionDataSource) dataSource).getTimeshiftOffsetPts());

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
            dataSource = HTSPSubscriptionDataSourceFactory.getCurrentDataSource();
        }
    }

    //this method take input from the TVInputService class onSetStreamVolume method or onSetStreamMute method
    //change the volume of the player
    public void changeVolume(float volume) {
        this.currentVolume = volume;
        this.player.setVolume(volume);
    }

    public float getCurrentVolume() {
        return this.currentVolume;
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

    public void addListener(Listener listener)
    {
        if(DEBUG)
        {
            Log.d(TAG, "Added Listener");
        }
        this.listeners.add(listener);
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if(DEBUG) {
            Log.d(TAG, "Tracks Changed");
        }
        MappingTrackSelector.MappedTrackInfo currentMappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (currentMappedTrackInfo == null)
            return;

        List<TvTrackInfo> tracks = new ArrayList<>();
        SparseArray<String> selectedTracks = new SparseArray<>();

        for (int renderersIndex = 0; renderersIndex < currentMappedTrackInfo.getRendererCount(); renderersIndex++) {

            TrackGroupArray rendererTrackGroups = currentMappedTrackInfo.getTrackGroups(renderersIndex);
            TrackSelection trackSelection = trackSelections.get(renderersIndex);

            if (rendererTrackGroups.length > 0) {
                for (int groupIndex = 0; groupIndex < rendererTrackGroups.length; groupIndex++) {
                    TrackGroup trackGroup = rendererTrackGroups.get(groupIndex);
                    for (int trackIndex = 0; trackIndex < trackGroup.length; trackIndex++) {
                        if (currentMappedTrackInfo.getTrackSupport(renderersIndex, groupIndex, trackIndex) == RendererCapabilities.FORMAT_HANDLED) {
                            Format format = trackGroup.getFormat(trackIndex);
                            TvTrackInfo tvTrackInfo = createTvTrackInfo(format);

                            if (tvTrackInfo != null) {
                                tracks.add(tvTrackInfo);

                                boolean selected = trackSelection != null && trackSelection.getTrackGroup() == trackGroup && trackSelection.indexOf(trackIndex) != C.INDEX_UNSET;

                                if (selected) {
                                    int trackType = MimeTypes.getTrackType(format.sampleMimeType);

                                    switch (trackType) {
                                        case C.TRACK_TYPE_VIDEO:
                                            selectedTracks.put(TvTrackInfo.TYPE_VIDEO, format.id);
                                            break;
                                        case C.TRACK_TYPE_AUDIO:
                                            selectedTracks.put(TvTrackInfo.TYPE_AUDIO, format.id);
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Notify all Listeners that the tracks have been changed
        for(Listener listener : listeners)
            listener.onTracks(tracks, selectedTracks);
    }

    /**
     * This is going to be used for the a custom fast rewind implementation.
     */
    private static class SeekableRunnable
    {
        private Handler repeatUpdateHandler = new Handler();
        public int value;
        private boolean rewind = false;

        private SimpleExoPlayer player;
        private HTSPSubscriptionDataSource dataSource;
        private HTSPSubscriptionDataSource.Factory htspDataSourceFactory;

        public SeekableRunnable(SimpleExoPlayer player, int speed, HTSPSubscriptionDataSource dataSource, HTSPSubscriptionDataSource.Factory htspDataSourceFactory)
        {
            this.player = player;
            this.value = speed;
            this.dataSource = dataSource;
            this.htspDataSourceFactory = htspDataSourceFactory;
        }

        private class Updater implements Runnable {
            public void run() {
                if(rewind)
                {
                    dataSource = (HTSPSubscriptionDataSource) htspDataSourceFactory.getCurrentDataSource();
                    long seekPtsPlayer = player.getContentBufferedPosition() + (value * 15); // TODO: Figure out the actual time conversion for ExoPlayer seekTo,
                    Log.d(TAG, "Seeking to PTS Player: " + seekPtsPlayer +", OFFSET: " + dataSource.getTimeshiftOffsetPts());

                    //dataSource.seek(seekPtsPlayer * 1000);
                    //player.seekTo(seekPtsPlayer);
                    repeatUpdateHandler.postDelayed( new Updater(), 100 );
                }
            }
        }

        public void startRewind()
        {
            rewind = true;
            repeatUpdateHandler.post( new Updater() );
            player.setPlayWhenReady(false);
        }

        public void stopRewind()
        {
            rewind = false;
            player.setPlayWhenReady(true);
        }
    }

    public static TvTrackInfo createTvTrackInfo(Format format) {
        if (format.id == null) {
            return null;
        }

        TvTrackInfo.Builder builder;
        int trackType = MimeTypes.getTrackType(format.sampleMimeType);

        switch (trackType) {
            case C.TRACK_TYPE_VIDEO:
                builder = new TvTrackInfo.Builder(TvTrackInfo.TYPE_VIDEO, format.id);
                builder.setVideoFrameRate(format.frameRate);
                if (format.width != Format.NO_VALUE && format.height != Format.NO_VALUE) {
                    builder.setVideoWidth(format.width);
                    builder.setVideoHeight(format.height);
                    builder.setVideoPixelAspectRatio(format.pixelWidthHeightRatio);
                }
                break;

            case C.TRACK_TYPE_AUDIO:
                builder = new TvTrackInfo.Builder(TvTrackInfo.TYPE_AUDIO, format.id);
                builder.setAudioChannelCount(format.channelCount);
                builder.setAudioSampleRate(format.sampleRate);
                break;

            case C.TRACK_TYPE_TEXT:
                builder = new TvTrackInfo.Builder(TvTrackInfo.TYPE_SUBTITLE, format.id);
                break;

            default:
                return null;
        }

        if (!TextUtils.isEmpty(format.language)
                && !format.language.equals("und")
                && !format.language.equals("nar")
                && !format.language.equals("syn")
                && !format.language.equals("mis")) {
            builder.setLanguage(format.language);
        }

        return builder.build();
    }
}
