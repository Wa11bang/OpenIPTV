package com.openiptv.code.player;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCrypto;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.ArrayList;

public class ExtendedRenderersFactory extends DefaultRenderersFactory {
    private static final String TAG = ExtendedRenderersFactory.class.getName();
    public ExtendedRenderersFactory(Context context) {
        super(context);
    }

    @Override
    protected void buildVideoRenderers(Context context, int extensionRendererMode, MediaCodecSelector mediaCodecSelector, @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager, boolean playClearSamplesWithoutKeys, boolean enableDecoderFallback, Handler eventHandler, VideoRendererEventListener eventListener, long allowedVideoJoiningTimeMs, ArrayList<Renderer> out)
    {
        if (Build.MODEL.equals("SHIELD Android TV")) {
            Log.d(TAG, "Adding ShieldVideoRenderer");
            out.add(new ShieldVideoRenderer(
                    context,
                    MediaCodecSelector.DEFAULT,
                    allowedVideoJoiningTimeMs,
                    eventHandler,
                    eventListener,
                    MAX_DROPPED_VIDEO_FRAME_COUNT_TO_NOTIFY));
        } else {
            super.buildVideoRenderers(context, extensionRendererMode, mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, enableDecoderFallback, eventHandler, eventListener, allowedVideoJoiningTimeMs, out);
        }
    }

    static class ShieldVideoRenderer extends MediaCodecVideoRenderer {

        private static final String TAG = ShieldVideoRenderer.class.getName();
        private static final int THIRTEEN_MINUTES = 13 * 60 * 1000;

        private long startTime;
        private boolean enabled;

        public ShieldVideoRenderer(Context context, MediaCodecSelector mediaCodecSelector, long allowedJoiningTimeMs, @Nullable Handler eventHandler, @Nullable VideoRendererEventListener eventListener, int maxDroppedFramesToNotify) {
            super(context, mediaCodecSelector, allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify);
        }

        @Override
        protected void configureCodec(MediaCodecInfo codecInfo, MediaCodec codec, Format format, @Nullable MediaCrypto crypto, float codecOperatingRate) {
            super.configureCodec(codecInfo, codec, format, crypto, codecOperatingRate);

            enabled = (format.height == 1080 || format.height == 576);
            startTime = System.currentTimeMillis();
        }

        @Override
        public void render(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
            long currentTime = System.currentTimeMillis();
            long diffMs = currentTime - startTime;

            //Log.d(TAG, "Is enabled: " + enabled);
            if(enabled && diffMs > THIRTEEN_MINUTES) {
                Log.d(TAG, "Resetting codecs as nVidia Shield workaround");
                releaseCodec();
                //startTime = System.currentTimeMillis();
            }

            super.render(positionUs, elapsedRealtimeUs);
        }
    }
}
