package com.openiptv.code.player;

import android.content.Context;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.MimeTypes;
import com.openiptv.code.htsp.HTSPMessage;

import java.util.Collections;
import java.util.List;

public class DVBSubReader extends SourceReader {
    /**
     * Set the track type
     *
     * @param context
     */
    public DVBSubReader(Context context) {
        super(context, C.TRACK_TYPE_TEXT);
    }

    @Override
    protected Format buildTrackFormat(HTSPMessage message, int index) {

        int compId = message.getInteger("composition_id");
        int anciId = message.getInteger("ancillary_id");

        byte[] initData = new byte[]{(byte) ((compId >> 16) & 0xff), (byte) (compId & 0xff), (byte) ((anciId >> 16) & 0xff), (byte) (anciId & 0xff)};


        List<byte[]> data = Collections.singletonList(initData);


        return Format.createImageSampleFormat(Integer.toString(index), MimeTypes.APPLICATION_DVBSUBS, null, Format.NO_VALUE, C.SELECTION_FLAG_DEFAULT, data, message.getString("language", "undefined"), null);
    }
}
