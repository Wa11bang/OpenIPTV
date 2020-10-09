package com.openiptv.code;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.openiptv.code.epg.EPGService;
import com.openiptv.code.input.TVInputService;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getName();

    /**
     * Ensures that all necessary services are started upon booting of this device.
     *
     * @param context application context
     * @param intent  given intent (whether our services can start)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received intent: " + intent.getAction());

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d(TAG, "Starting OpenIPTV Services");
            context.startService(new Intent(context, TVInputService.class));
            context.startService(new Intent(context, EPGService.class));
        }
    }
}