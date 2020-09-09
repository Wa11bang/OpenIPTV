package com.openiptv.code.epg;

import android.net.Uri;

import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.sync.EpgSyncJobService;

import java.util.List;

public class EPGService extends EpgSyncJobService {
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public List<Channel> getChannels() throws EpgSyncException {
        return null;
    }

    @Override
    public List<Program> getProgramsForChannel(Uri channelUri, Channel channel, long startMs, long endMs) throws EpgSyncException {
        return null;
    }
}
