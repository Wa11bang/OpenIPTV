package com.openiptv.code.player;

import android.content.Context;
import android.media.PlaybackParams;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.AvcConfig;
import com.google.android.media.tv.companionlibrary.TvPlayer;
import com.openiptv.code.htsp.BaseConnection;
import com.openiptv.code.htsp.ConnectionInfo;
import com.openiptv.code.htsp.HTSPMessage;
import com.openiptv.code.htsp.HTSPNotConnectedException;
import com.openiptv.code.htsp.Subscriber;

import java.util.List;

public class TVPlayer implements Player.EventListener, Subscriber.Listener {
    private SimpleExoPlayer player;
    private Context context;
    private Surface surface;
    private MediaSource mediaSource;
    private DataSource.Factory dataSourceFactory;
    private BaseConnection connection;
    private Subscriber subscriber;
    private HTSPMessage streams[];

    private static final String URL = "https://moctobpltc-i.akamaihd.net/hls/live/571329/eight/playlist.m3u8";

    public TVPlayer(Context context, SimpleExoPlayer player)
    {
        Log.d("TVPlayer", "Created!");
        this.context = context;
        this.player = player;

        connection = new BaseConnection(new ConnectionInfo("10.0.0.57", 9982, "development", "development", "Subscription", "23"));
        connection.start();

        subscriber = new Subscriber(connection.getHtspMessageDispatcher());
        subscriber.addSubscriptionListener(this);
    }

    public boolean setSurface(Surface surface)
    {
        this.surface = surface;
        player.setVideoSurface(surface);

        return true;
    }

    public void prepare(int channelId)
    {
        if(subscriber.getIsSubscribed())
        {
            subscriber.unsubscribe();
        }
        try {
            subscriber.subscribe(channelId);
        } catch (HTSPNotConnectedException ignored) {
        }

        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "OpenIPTV"));
        mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(URL));
        player.prepare(mediaSource);
    }

    public void start()
    {
        player.setPlayWhenReady(true);
    }

    public void stop()
    {
        Log.d("TVPlayer", "Released Subscription");
        player.release();
        subscriber.unsubscribe();
    }

    @Override
    public void onSubscriptionStart(@NonNull HTSPMessage message) {
        streams = message.getHtspMessageArray("streams");

        for(HTSPMessage stream : streams)
        {
            Log.d("TVPlayer", "Stream: " + stream.getString("type") + ", Index: " + stream.getInteger("index"));
        }
    }

    @Override
    public void onSubscriptionStatus(@NonNull HTSPMessage message) {

    }

    @Override
    public void onSubscriptionStop(@NonNull HTSPMessage message) {

    }

    @Override
    public void onMuxpkt(@NonNull HTSPMessage message) {
        Log.d("TVPlayer", "Received MuxPkt");
        for(HTSPMessage stream : streams)
        {
            if(stream.getInteger("index") == message.getInteger("stream") && stream.getString("type").equals("H264"))
            {
                // We have found a matching MuxPkt for the stream index
                TrackOutput mTrackOutput;

                // Get ExoPlayer Media Format
                Format format = buildFormat(stream.getInteger("index"), stream);

            }
        }
    }

    protected Format buildFormat(int streamIndex, @NonNull HTSPMessage stream) {
        List<byte[]> initializationData = null;

        if (stream.containsKey("meta")) {
            try {
                AvcConfig avcConfig = AvcConfig.parse(new ParsableByteArray(stream.getByteArray("meta")));
                initializationData = avcConfig.initializationData;
            } catch (ParserException e) {
                Log.e("TVPlayer", "Failed to parse H264 meta, discarding");
            }
        }

        return Format.createVideoSampleFormat(
                Integer.toString(streamIndex),
                MimeTypes.VIDEO_H264,
                null,
                Format.NO_VALUE,
                Format.NO_VALUE,
                stream.getInteger("width"),
                stream.getInteger("height"),
                frameDurationToFrameRate(stream.getInteger("duration", Format.NO_VALUE)),
                initializationData,
                null);
    }

    static float frameDurationToFrameRate(int frameDuration) {
        float frameRate = Format.NO_VALUE;

        if (frameDuration != Format.NO_VALUE) {
            // 1000000 = 1 second, in microseconds.
            frameRate = 1000000 / (float) frameDuration;
        }

        return frameRate;
    }
}
