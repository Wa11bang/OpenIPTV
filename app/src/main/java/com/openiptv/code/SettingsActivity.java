package com.openiptv.code;


import android.app.Activity;
import android.os.Bundle;
import androidx.leanback.widget.VerticalGridView;

public class SettingsActivity extends Activity {
    private static final String TAG = SettingsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preferences_layout);
    }

}
