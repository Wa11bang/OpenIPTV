package com.openiptv.code.epg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.openiptv.code.DatabaseActions;
import com.openiptv.code.PreferenceUtils;

import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.PREFERENCE_SETUP_COMPLETE;

public class EPGService extends Service {
    private EPGCaptureTask epgCaptureTask;

    @Override
    public void onCreate() {
        super.onCreate();

        // Sync currently active account. At this point, the is always an account present
        // in the application's database.
        DatabaseActions databaseActions = new DatabaseActions(getApplicationContext());
        databaseActions.syncActiveAccount();
        databaseActions.close();

        PreferenceUtils preferenceUtils = new PreferenceUtils(this);

        if (DEBUG) {
            Log.d("EPGService", "called!");
        }

        if (preferenceUtils.getBoolean(PREFERENCE_SETUP_COMPLETE)) {
            Log.d("EPGService", "Creating Capture Task");
            epgCaptureTask = new EPGCaptureTask(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        epgCaptureTask.stop();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
