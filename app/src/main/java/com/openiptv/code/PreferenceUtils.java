package com.openiptv.code;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import static com.openiptv.code.Constants.DEBUG;
import static com.openiptv.code.Constants.PREFERENCE_NOT_SET_BOOL;
import static com.openiptv.code.Constants.PREFERENCE_NOT_SET_INT;
import static com.openiptv.code.Constants.PREFERENCE_NOT_SET_STRING;

public class PreferenceUtils {
    private static final String TAG = PreferenceUtils.class.getSimpleName();

    private Context context;
    private SharedPreferences sharedPreferences;

    public PreferenceUtils(Context context)
    {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(Constants.ACCOUNT, Context.MODE_PRIVATE);
    }

    public void setBoolean(String key, boolean value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
        if(DEBUG)
        {
            Log.d(TAG, "Set value " + value + " to key: " + key);
        }
    }

    public boolean getBoolean(String key)
    {
        return sharedPreferences.getBoolean(key, PREFERENCE_NOT_SET_BOOL);
    }

    public void setString(String key, String value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        if(DEBUG)
        {
            Log.d(TAG, "Set value " + value + " to key: " + key);
        }
    }

    public String getString(String key)
    {
        return sharedPreferences.getString(key, PREFERENCE_NOT_SET_STRING);
    }

    public void setInteger(String key, int value)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
        if(DEBUG)
        {
            Log.d(TAG, "Set value " + value + " to key: " + key);
        }
    }

    public int getInteger(String key)
    {
        return sharedPreferences.getInt(key, PREFERENCE_NOT_SET_INT);
    }
}
