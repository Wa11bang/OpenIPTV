package com.openiptv.code.player;

import android.content.Context;
import android.graphics.Point;
import android.media.PlaybackParams;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;

import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;
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
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import com.openiptv.code.DatabaseActions;
import com.openiptv.code.TVHeadendAccount;
import com.openiptv.code.R;

import com.openiptv.code.epg.RecordedProgram;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.HTSPException;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.player.utils.TimeshiftUtils;

import java.util.ArrayList;
import java.util.List;

import static com.openiptv.code.Constants.DEBUG;

public class TVPlayer implements Player.EventListener {
    private SimpleExoPlayer player;
    private Context context;
    private Surface surface;
    private MediaSource mediaSource;
    private BaseConnection connection;
    private HTSPDataSource.Factory htspSubscriptionDataSourceFactory;
    private HTSPDataSource dataSource;
    private ExtractorsFactory extractorsFactory;

    private View overlayView;
    private View subtitleView;

    private boolean recording;
    private long recStartTime;

    private List<Listener> listeners;
    private ExtendedTrackSelector trackSelector;
    private float currentVolume;
    private TimeshiftUtils.Rewinder rewinder;

    private static final String TAG = TVPlayer.class.getSimpleName();

    /**
     * This interface is used to listen for changes to the available tracks. Primarily used in
     * TVInputService
     */
    public interface Listener {
        /**
         * Notifies listeners that a new set of tracks are available and the currently selected tracks.
         * @param tracks all available tracks
         * @param selectedTracks currently selected tracks
         */
        void onTracks(List<TvTrackInfo> tracks, SparseArray<String> selectedTracks);
    }

    /**
     * Constructor for TVPlayer object
     * @param context application context
     * @param connection BaseConnection used for subscribing to Channels/Recordings
     */
    public TVPlayer(Context context, BaseConnection connection)
    {
        Log.d("TVPlayer", "Created!");
        this.context = context;

        trackSelector = new ExtendedTrackSelector(context);
        this.player = new SimpleExoPlayer.Builder(context)
                .setTrackSelector(trackSelector)
                .build();

        this.player.addListener(this);
        this.connection = connection;

        htspSubscriptionDataSourceFactory = new HTSPSubscriptionDataSource.Factory(context, connection, "htsp");
        extractorsFactory = new ExtendedExtractorsFactory(context);

        listeners = new ArrayList<>();

        rewinder = new TimeshiftUtils.Rewinder(new Handler(), player, this);
    }

    /**
     * Sets the VideoSurface for ExoPlayer
     * @param surface videoSurface
     */
    public boolean setSurface(Surface surface) {
        this.surface = surface;
        player.setVideoSurface(surface);

        return true;
    }

    /**
     * Prepares the ExoPlayer MediaSource. Can be either a recording or Live TV Stream. Currently the
     * recording implementation is hard-coded, and can only play ONE stream.
     * @param channelUri to tune
     * @param recording is this source a recording
     */
    public void prepare(Uri channelUri, boolean recording) {
        this.recording = recording;

        if (!recording) {

            mediaSource = new ProgressiveMediaSource.Factory(htspSubscriptionDataSourceFactory, extractorsFactory).createMediaSource(channelUri);

        } else {

            mediaSource = buildRecordingMediaSource(channelUri);
        }
        player.prepare(mediaSource);
    }

    /**
     * Start the TVPlayer
     */
    public void start() {
        player.setPlayWhenReady(true);
    }

    /**
     * Stop the TVPlayer
     */
    public void stop() {
        Log.d(TAG, "Released TVPlayer");
        player.release();
        //connection.stop();
        if (surface != null)
        {
            surface.release();
        }
        if (dataSource != null)
        {
            dataSource.release(); // Releasing the dataSource fixes duplication of subscriptions.
        }
        if(mediaSource != null)
        {
            mediaSource.releaseSource(null);
        }
    }

    /**
     * Resume the TV Input / ExoPlayer
     */
    public void resume() {
        player.setPlayWhenReady(true);
        player.setPlaybackParameters(new PlaybackParameters(1));
        rewinder.stop();

        dataSource = htspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            dataSource.resume();
        } else {
            Log.w(TAG, "Unable to resume, no DataSource available");
        }
    }

    /**
     * Pause the TV Input / ExoPlayer
     */
    public void pause() {
        player.setPlayWhenReady(false);
        rewinder.stop();

        dataSource = htspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            dataSource.pause();
        }
    }

    /**
     * Sets the PlayBackParams for the DataSource AND ExoPlayer. Currently only FAST FORWARD can be
     * implemented due to Clock Limitations. FAST REWIND is being worked on using a runnable.
     * @param playbackParams to set
     */
    public void setPlaybackParams(PlaybackParams playbackParams)
    {
        player.setPlayWhenReady(false);
        if(rewinder.isRunning()) {
            rewinder.stop();
        }

        if(recording)
        {
            if(playbackParams.getSpeed() < 1)
            {
                rewinder.start(playbackParams.getSpeed());
            }
            else {
                player.setPlaybackParameters(new PlaybackParameters(playbackParams.getSpeed()));
                player.setPlayWhenReady(true);
            }

            return;
        }

        dataSource = htspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            Log.d(TAG, "Resuming DataSource");

            if(playbackParams.getSpeed() < 1)
            {
                dataSource.setSpeed(AndroidTVSpeedToTVH(playbackParams.getSpeed()));
                rewinder.start(playbackParams.getSpeed());
            }
            else {
                dataSource.setSpeed(AndroidTVSpeedToTVH(playbackParams.getSpeed()));
                player.setPlaybackParameters(new PlaybackParameters(playbackParams.getSpeed()));
                player.setPlayWhenReady(true);
            }
        }
    }

    /**
     * Returns the start position for the TV Input / ExoPlayer
     * @return startPosition
     */
    public long getTimeshiftStartPosition() {
        if(recording)
        {
            return recStartTime;
        }

        dataSource = htspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            long startTime = dataSource.getTimeshiftStartTime();
            if (startTime != -1) {
                // For live content
                return (startTime / 1000);
            } else {
                // For recorded content
                return 0;
            }
        } else {
            Log.w(TAG, "Unable to getTimeshiftStartPosition, no DataSource available");
        }

        return -1;
    }

    /**
     * Returns the current position in the TV input / ExoPlayer
     * @return currentPosition
     */
    public long getTimeshiftCurrentPosition() {
        if(recording)
        {
            return recStartTime + player.getCurrentPosition();
        }

        dataSource = htspSubscriptionDataSourceFactory.getCurrentDataSource();
        if (dataSource != null) {
            if(rewinder.isRunning())
            {
                //Log.d(TAG, "Calculated CurrentPos R: " + rewinder.getCurrentPos());
                return rewinder.getCurrentPos();
            }
            long offset = dataSource.getTimeshiftOffsetPts();
            //Log.d(TAG, "Calculated CurrentPos: " + Math.max((System.currentTimeMillis() + (offset / 1000)), getTimeshiftStartPosition()));
            return Math.max((System.currentTimeMillis() + (offset / 1000)), getTimeshiftStartPosition());

        } else {
            Log.w(TAG, "Unable to getTimeshiftCurrentPosition, no DataSource available");
        }

        return -1;
    }

    /**
     * Seeks the DataSource AND ExoPlayer by the given timeMs. The actual seek time is calculated by
     * the current position and offset.
     * @param timeMs to seek
     */
    public void seek(long timeMs)
    {
        if (dataSource != null) {

            long seekPts = (timeMs * 1000) - dataSource.getTimeshiftStartTime();
            seekPts = Math.max(seekPts, dataSource.getTimeshiftStartPts()) / 1000;

            dataSource.seek(seekPts);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            player.seekTo(seekPts);
        } else {
            Log.w(TAG, "Unable to seek, no HtspDataSource available");
        }

        //resume();
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (isLoading && !recording) {
            dataSource = htspSubscriptionDataSourceFactory.getCurrentDataSource();
        }
    }

    /**
     * this method take input from the TVInputService class onSetStreamVolume method or
     * onSetStreamMute method change the volume of the player
     * @param volume to set
     */
    public void changeVolume(float volume) {
        this.currentVolume = volume;
        this.player.setVolume(volume);
    }

    /**
     * Returns the current volume
     * @return currentVolume
     */
    public float getCurrentVolume() {
        return this.currentVolume;
    }

    /**
     * Internal Helper Method which is used to convert the Android TV speeds to TVHeadEnd Speeds
     * @param speed android speed
     * @return TVHeadEnd speed
     */
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

    /**
     * Add a Player.Listener to the Listeners list
     * @param listener to add
     */
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
                                        case C.TRACK_TYPE_TEXT:
                                            selectedTracks.put(TvTrackInfo.TYPE_SUBTITLE, format.id);
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
     * Create the TvTrackInfo object for a given Format
     * @param format to parse into TvTrackInfo object
     * @return TvTrackInfo
     */
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

    private MediaSource buildRecordingMediaSource(Uri recordingUri)
    {
        HTSPMessage message = new HTSPMessage();
        message.put("method", "getTicket");
        message.put("dvrId", RecordedProgram.getRecordingIdFromRecordingUri(context, recordingUri).toString());

        HTSPMessage response = null;
        try {
            response = connection.getHTSPMessageDispatcher().sendMessage(message, 2000);
        } catch (HTSPException e) {
            e.printStackTrace();
        }

        TVHeadendAccount account = new TVHeadendAccount(DatabaseActions.activeAccount);
        String url = "http://"+account.getHostname() + ":" + "9981" + response.getString("path");

        Log.d(TAG, "Recording Url: " + url);

        byte[] toEncrypt = (account.getUsername() + ":" + account.getPassword()).getBytes();
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "OpenIPTV").replace("ExoPlayerLib", "Blah"));

        dataSourceFactory.getDefaultRequestProperties().set("Authorization", "Basic " + Base64.encodeToString(toEncrypt, Base64.DEFAULT));
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        recStartTime = System.currentTimeMillis();

        return new ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory).createMediaSource(Uri.parse(url));
    }
  
    public boolean selectTrack(int type, String track)
    {
        return trackSelector.selectTrack(type, track);
    }

    public View getOverlayView(CaptioningManager.CaptionStyle captionStyle) {
        if (overlayView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            overlayView = layoutInflater.inflate(R.layout.subtitle_overlay_view, null);
        }

        if (subtitleView == null) {
            subtitleView = getSubtitleView(captionStyle);

            if (subtitleView != null) {
                // SubtitleView implements TextOutput
                player.addTextOutput((TextOutput) subtitleView);
            }
        }

        return overlayView;
    }

    private SubtitleView getSubtitleView(CaptioningManager.CaptionStyle captionStyle) {
        SubtitleView view = overlayView.findViewById(R.id.subtitle_view);
        CaptionStyleCompat captionStyleCompat = CaptionStyleCompat.createFromCaptionStyle(captionStyle);

        view.setStyle(captionStyleCompat);
        view.setVisibility(View.VISIBLE);
        view.setApplyEmbeddedStyles(true);

        return view;
    }
}
