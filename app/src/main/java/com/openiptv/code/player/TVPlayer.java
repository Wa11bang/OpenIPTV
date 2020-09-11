package com.openiptv.code.player;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.openiptv.code.epg.RecordedProgram;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.player.test.HtspSubscriptionDataSource;
import com.openiptv.code.player.test.TvheadendExtractorsFactory;

public class TVPlayer implements Player.EventListener {
    private SimpleExoPlayer player;
    private Context context;
    private Surface surface;
    private MediaSource mediaSource;
    private BaseConnection connection;
    private HTSPMessage streams[];
    private HtspDataSource.Factory mHtspSubscriptionDataSourceFactory;
    private HtspDataSource mDataSource;
    private ExtractorsFactory mExtractorsFactory;
    private boolean recording;

    private static final String URL = "http://tv.theron.co.nz:9981/dvrfile/c27bb93d8be4b0946e0f1cf840863e0e";

    public TVPlayer(Context context, SimpleExoPlayer player)
    {
        Log.d("TVPlayer", "Created!");
        this.context = context;
        this.player = player;

        connection = new BaseConnection(new ConnectionInfo("10.0.0.57", 9982, "development", "development", "Subscription", "23"));
        connection.start();


        mHtspSubscriptionDataSourceFactory = new HtspSubscriptionDataSource.Factory(context, connection, "htsp");

        // Produces Extractor instances for parsing the media data.
        mExtractorsFactory = new TvheadendExtractorsFactory(context);
    }

    public boolean setSurface(Surface surface)
    {
        this.surface = surface;
        player.setVideoSurface(surface);

        return true;
    }

    public void prepare(Uri channelUri, boolean recording)
    {
        /*if(subscriber.getIsSubscribed())
        {
            subscriber.unsubscribe();
        }
        try {
            subscriber.subscribe(channelId);
        } catch (HTSPNotConnectedException ignored) {
        }*/

        this.recording = recording;

        if(!recording) {

            mediaSource = new ProgressiveMediaSource.Factory(mHtspSubscriptionDataSourceFactory)
                    .setExtractorsFactory(mExtractorsFactory)
                    .createMediaSource(channelUri);

            //dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "OpenIPTV"));
            //mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(URL));
            player.prepare(mediaSource);
        }
        else
        {
            Log.d("TVPlayer", "captured recording ID" + RecordedProgram.getRecordingIdFromRecordingUri(context, channelUri));

            //DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "OpenIPTV").replace("ExoPlayerLib", "Blah"));
            //ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            //mediaSource = new ProgressiveMediaSource
            //        .Factory(dataSourceFactory)
            //        .setExtractorsFactory(extractorsFactory)
            //        .createMediaSource(Uri.parse("http://Waldo:Waldo01jani02@10.0.0.57:9981/dvrfile/c27bb93d8be4b0946e0f1cf840863e0e?ticket=dd7e25aaa4cd2643dfe34621de36ca9ec350854d"));

            final String cred = "development" + ":" + "development";
            final String auth = "Basic "+ Base64.encodeToString(cred.getBytes(),Base64.URL_SAFE|Base64.NO_WRAP);

            byte[] toEncrypt = ("development" + ":" + "development").getBytes();
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "OpenIPTV").replace("ExoPlayerLib", "Blah"));

            dataSourceFactory.setDefaultRequestProperty("Authorization","Basic "+Base64.encodeToString(toEncrypt, Base64.DEFAULT));
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource videoSource = new ExtractorMediaSource(Uri.parse(URL),
                    dataSourceFactory, extractorsFactory, null, null);

            /*dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "OpenIPTV"));
            mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(URL));*/
            //player.prepare(mediaSource);
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
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (isLoading && !recording) {
            // Fetch the current DataSource for later use
            // TODO: Hold a WeakReference to the DataSource instead...
            // TODO: We should know if we're playing a channel or a recording...
            mDataSource = mHtspSubscriptionDataSourceFactory.getCurrentDataSource();
        }
    }
}
