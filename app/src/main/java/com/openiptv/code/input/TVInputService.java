package com.openiptv.code.input;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import android.view.accessibility.CaptioningManager;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.openiptv.code.epg.Channel;
import com.openiptv.code.epg.EPGService;
import com.openiptv.code.player.TVPlayer;

import static com.openiptv.code.epg.EPGService.isSetupComplete;


public class TVInputService extends TvInputService {
    private static final String TAG = TVInputService.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final long EPG_SYNC_DELAYED_PERIOD_MS = 1000 * 2; // 2 Seconds

    private CaptioningManager mCaptioningManager;

    /**
     * Gets the track id of the track type and track index.
     *
     * @param trackType  the type of the track e.g. TvTrackInfo.TYPE_AUDIO
     * @param trackIndex the index of that track within the media. e.g. 0, 1, 2...
     * @return the track id for the type & index combination.
     */
    private static String getTrackId(int trackType, int trackIndex) {
        return trackType + "-" + trackIndex;
    }

    /**
     * Gets the index of the track for a given track id.
     *
     * @param trackId the track id.
     * @return the track index for the given id, as an integer.
     */
    private static int getIndexFromTrackId(String trackId) {
        return Integer.parseInt(trackId.split("-")[1]);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(isSetupComplete(this)) {
            getApplicationContext().startService(new Intent(getApplicationContext(), EPGService.class));
        }
        mCaptioningManager = (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
    }

    @Override
    public final Session onCreateSession(String inputId) {
        TVSession session = new TVSession(this, inputId);
        session.setOverlayViewEnabled(true);
        return session;
    }

    class TVSession extends TvInputService.Session {
        private static final float CAPTION_LINE_HEIGHT_RATIO = 0.0533f;
        private static final int TEXT_UNIT_PIXELS = 0;
        private static final String UNKNOWN_LANGUAGE = "und";

        private int mSelectedSubtitleTrackIndex;
        private SubtitleView mSubtitleView;
        private TVPlayer mPlayer;
        private boolean mCaptionEnabled;
        private String mInputId;
        private Context mContext;

        TVSession(Context context, String inputId) {
            super(context);
            mCaptionEnabled = mCaptioningManager.isEnabled();
            mContext = context;
            mInputId = inputId;
            SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
            mPlayer = new TVPlayer(context, player);
        }

        @Override
        public void onRelease() {
            mPlayer.stop();
        }

        @Override
        public boolean onSetSurface(@Nullable Surface surface) {
            Log.d(TAG, "Setting Surface");
            return mPlayer.setSurface(surface);
        }

        @Override
        public void onSetStreamVolume(float volume) {

        }

        @Override
        public boolean onTune(Uri channelUri) {
            mPlayer.prepare();
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            Log.d(TAG, "Android has request to tune to channel: " + Channel.getChannelIdFromChannelUri(mContext, channelUri));

            mPlayer.start();
            notifyContentAllowed();
            notifyVideoAvailable();
            return true;
        }

        @Override
        public void onSetCaptionEnabled(boolean enabled) {

        }
    }
}
