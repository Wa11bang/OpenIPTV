package com.openiptv.code.epg;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.sync.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.xmltv.XmlTvParser;
import com.openiptv.code.Constants;

import java.util.ArrayList;
import java.util.List;

public class EPGService extends Service {
    private EPGCaptureTask epgCaptureTask;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("EPGService", "called!");
        if(isSetupComplete(this)) {
            epgCaptureTask = new EPGCaptureTask(this, true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isSetupComplete(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.ACCOUNT, Context.MODE_PRIVATE);

        Log.d("EPG", "Setup complete: " + sharedPreferences.getBoolean("SETUP-COMPLETE", false));
        return sharedPreferences.getBoolean("SETUP-COMPLETE", false);
    }

    public static void setSetupComplete(Context context, boolean isSetupComplete) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.ACCOUNT, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("SETUP-COMPLETE", isSetupComplete);
        editor.apply();
    }
}
