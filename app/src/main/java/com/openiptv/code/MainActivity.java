package com.openiptv.code;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.os.Build;
import android.os.Bundle;

import com.openiptv.code.input.TVInputService;

import static com.openiptv.code.Constants.PREFERENCE_SETUP_COMPLETE;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceUtils preferenceUtils = new PreferenceUtils(this);

        if(preferenceUtils.getBoolean(PREFERENCE_SETUP_COMPLETE)) {
            DatabaseActions databaseActions = new DatabaseActions(getApplicationContext());
            String accountId = databaseActions.getActiveAccount();
            databaseActions.setActiveAccount(accountId);
            databaseActions.close();
        }

        Intent intent = new Intent(this, TVInputService.class);
        startService(intent);

        Intent i;

        if(preferenceUtils.getBoolean(PREFERENCE_SETUP_COMPLETE)) {
            i = new Intent(this, PreferenceActivity.class);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                i = new Intent(TvInputManager.ACTION_SETUP_INPUTS);
            } else {
                i = new Intent(Intent.ACTION_VIEW, TvContract.Channels.CONTENT_URI);
                i.setData(TvContract.buildChannelsUriForInput(TvContract.buildInputId(new ComponentName(Constants.COMPONENT_PACKAGE, Constants.COMPONENT_CLASS))));
            }
        }

        startActivity(i);
        finish();
    }
}
