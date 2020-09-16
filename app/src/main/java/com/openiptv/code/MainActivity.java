package com.openiptv.code;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.os.Bundle;

import com.openiptv.code.input.TVInputService;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = new Intent(this, TVInputService.class);
        startService(intent);

        Intent i;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            i = new Intent(TvInputManager.ACTION_SETUP_INPUTS);
        } else {
            i = new Intent(Intent.ACTION_VIEW, TvContract.Channels.CONTENT_URI);
            i.setData(TvContract.buildChannelsUriForInput(TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS))));
        }

        startActivity(i);

        finish();
    }
}
