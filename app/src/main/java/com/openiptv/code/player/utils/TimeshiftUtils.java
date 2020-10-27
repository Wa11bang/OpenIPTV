package com.openiptv.code.player.utils;

import android.os.Handler;

import com.google.android.exoplayer2.ExoPlayer;
import com.openiptv.code.player.TVPlayer;

public class TimeshiftUtils {
    // Tested values for Delay
    // 2 ms - Works okay, does infrequently cause the system to bug out
    public static class Rewinder {
        private final static String TAG = Rewinder.class.getSimpleName();
        final private ExoPlayer player;
        final private Handler handler;
        final private TVPlayer tvPlayer;
        private boolean started = false;
        private long currentPos;
        private float speed = 1.0f;

        private Runnable doTick = this::tick;

        public Rewinder(Handler handler, ExoPlayer player, TVPlayer tvPlayer) {
            this.player = player;
            this.handler = handler;
            this.tvPlayer = tvPlayer;
        }

        public void start(float speed) {
            if (speed == 1.0) {
                stop();
                return;
            }

            this.speed = speed;
            this.currentPos = tvPlayer.getTimeshiftCurrentPosition();

            if (!started) {
                started = true;
                postTick();
            }
        }

        private void tick() {
            currentPos = (currentPos + (long) (speed*1000));
            long seekPos = -(tvPlayer.getTimeshiftStartPosition() - currentPos);
            //Log.d(TAG, "SEEKPOS: " + seekPos + ", Speed: " + speed);

            if (started) {
                if (seekPos >= 0) {
                    player.seekTo(seekPos);
                }
                else
                {
                    player.seekTo(0);
                }
                handler.postDelayed(doTick, 1000);
            }
        }

        public void stop() {
            if (!started) {
                return;
            }
            handler.removeCallbacks(doTick);
            reset();
            //tick();
        }

        public void reset() {
            started = false;
            handler.removeCallbacksAndMessages(null);
            handler.removeCallbacks(doTick);
        }

        public void postTick() {
            // I wonder what changing THIS delay does :/
            handler.post(doTick);
        }

        public boolean isRunning()
        {
            return started;
        }

        public long getCurrentPos()
        {
            return currentPos;
        }
    }
}
