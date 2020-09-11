package com.openiptv.code.input;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.openiptv.code.epg.Channel;
import com.openiptv.code.epg.EPGService;
import com.openiptv.code.epg.RecordedProgram;
import com.openiptv.code.player.TVPlayer;

import static com.openiptv.code.epg.EPGService.isSetupComplete;


public class TVInputService extends TvInputService {
    private static final String TAG = TVInputService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        if(isSetupComplete(this)) {
            getApplicationContext().startService(new Intent(getApplicationContext(), EPGService.class));
        }
    }

    @Override
    public final Session onCreateSession(String inputId) {
        TVSession session = new TVSession(this, inputId);
        session.setOverlayViewEnabled(true);
        return session;
    }

    class TVSession extends TvInputService.Session {
        private TVPlayer player;
        private String inputId;
        private Context context;

        TVSession(Context context, String inputId) {
            super(context);

            this.context = context;
            this.inputId = inputId;

            SimpleExoPlayer exoPlayer = new SimpleExoPlayer.Builder(context).build();
            player = new TVPlayer(context, exoPlayer);
        }

        @Override
        public void onRelease() {
            Log.d(TAG, "TVSession Released");
            player.stop();
        }

        @Override
        public boolean onSetSurface(@Nullable Surface surface) {
            Log.d(TAG, "Setting Surface");
            return player.setSurface(surface);
        }

        @Override
        public void onSetStreamVolume(float volume) {

        }

        @Override
        public boolean onTune(Uri channelUri) {
            player.prepare(channelUri, false);
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            Log.d(TAG, "Android has request to tune to channel: " + Channel.getChannelIdFromChannelUri(context, channelUri));

            player.start();
            notifyContentAllowed();
            notifyVideoAvailable();
            return true;
        }

        @Override
        public void onTimeShiftPause() {

        }

        @Override
        public void onTimeShiftResume() {

        }

        @Override
        public void onTimeShiftPlay(Uri recordedProgramUri) {
            Log.d(TAG, "recorded program: " + recordedProgramUri.getPathSegments().get(1));
            Log.d(TAG, "recorded program TVH ID: " + RecordedProgram.getRecordingIdFromRecordingUri(context, recordedProgramUri));
            player.prepare(recordedProgramUri, true);
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);

            player.start();
            notifyContentAllowed();
            notifyVideoAvailable();
        }

        @Override
        public void onSetCaptionEnabled(boolean enabled) {

        }
    }
}
