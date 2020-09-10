package com.openiptv.code.player;

import android.content.Context;
import android.media.PlaybackParams;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.media.tv.companionlibrary.TvPlayer;

public class TVPlayer implements Player.EventListener {
    private SimpleExoPlayer player;
    private Context context;
    private Surface surface;
    private MediaSource mediaSource;
    private DataSource.Factory dataSourceFactory;

    private static final String URL = "https://moctobpltc-i.akamaihd.net/hls/live/571329/eight/playlist.m3u8";

    public TVPlayer(Context context, SimpleExoPlayer player)
    {
        Log.d("TVPlayer", "Created!");
        this.context = context;
        this.player = player;
    }

    public boolean setSurface(Surface surface)
    {
        this.surface = surface;
        player.setVideoSurface(surface);

        return true;
    }

    public void prepare()
    {
        int channelId = 1997018292;

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
        player.release();
    }

}
