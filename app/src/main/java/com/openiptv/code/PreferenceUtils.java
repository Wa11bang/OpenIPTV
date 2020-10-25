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

    /**
     * Constructor for a PreferenceUtils object
     *
     * @param context application context - used for SharedPreferences
     */
    public PreferenceUtils(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(Constants.ACCOUNT, Context.MODE_PRIVATE);
    }

    /**
     * Sets a boolean type value to a given preference key
     *
     * @param key   preference key
     * @param value new value
     */
    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
        if (DEBUG) {
            Log.d(TAG, "Set value " + value + " to key: " + key);
        }
    }

    /**
     * Returns a boolean type value from a given preference key
     *
     * @param key preference key
     * @return stored value
     */
    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, PREFERENCE_NOT_SET_BOOL);
    }

    /**
     * Sets a String type value to a given preference key
     *
     * @param key   preference key
     * @param value new value
     */
    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
        if (DEBUG) {
            Log.d(TAG, "Set value " + value + " to key: " + key);
        }
    }

    /**
     * Returns a String type value from a given preference key
     *
     * @param key preference key
     * @return    stored value
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, PREFERENCE_NOT_SET_STRING);
    }

    /**
     * Sets an int type value to a given preference key
     *
     * @param key   preference key
     * @param value new value
     */
    public void setInteger(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
        if (DEBUG) {
            Log.d(TAG, "Set value " + value + " to key: " + key);
        }
    }

    /**
     * Returns an int type value from a given preference key
     *
     * @param key preference key
     * @return stored value
     */
    public int getInteger(String key) {
        return sharedPreferences.getInt(key, PREFERENCE_NOT_SET_INT);
    }
}
