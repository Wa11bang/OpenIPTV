package com.openiptv.code.epg;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.sync.EpgSyncJobService;
import com.google.android.media.tv.companionlibrary.xmltv.XmlTvParser;

import java.util.ArrayList;
import java.util.List;

public class EPGService extends Service {
    private EPGCaptureTask epgCaptureTask;

    @Override
    public void onCreate()
    {
        super.onCreate();
        epgCaptureTask = new EPGCaptureTask(this, true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
