package com.openiptv.code;

import android.util.ArraySet;

import java.util.Arrays;
import java.util.Set;

public class Constants {
    public static final boolean DEBUG = true; // TODO: implement debug check in code
    public static final boolean RESTART_SERVICES = true;
    public static final String ACCOUNT = "openiptv";
    public static final String COMPONENT_PACKAGE = "com.openiptv.code";
    public static final String COMPONENT_CLASS = ".input.TVInputService";
    public static final String DEV_HOST = "tv.theron.co.nz";

    // HTSPMessage Sequence IDs
    public static final int UNIQUE_AUTH_SEQ_ID = 101;


    // EPG Details
    /*
        Retrieved from HTSP documentation.
        https://tvheadend.org/projects/tvheadend/wiki/Htsp
     */
    public static final String CHANNEL_ID = "channelId";
    public static final String CHANNEL_NAME = "channelName";
    public static final String CHANNEL_NUMBER = "channelNumber";
    public static final String CHANNEL_NUMBER_MINOR = "channelNumberMinor";
    public static final String CHANNEL_ICON = "channelIcon";

    public static final String PROGRAM_ID = "eventId";
    public static final String PROGRAM_TITLE = "title";
    public static final String PROGRAM_DESCRIPTION = "description";
    public static final String PROGRAM_SUMMARY = "summary";
    public static final String PROGRAM_CONTENT_TYPE = "contentType";
    public static final String PROGRAM_AGE_RATING = "ageRating";
    public static final String PROGRAM_START_TIME = "start";
    public static final String PROGRAM_FINISH_TIME = "stop";
    public static final String PROGRAM_IMAGE = "image";
    public static final String PROGRAM_TYPE = "contentType";

    public static final String RECORDED_PROGRAM_ID = "id";
    public static final String RECORDED_PROGRAM_CHANNEL = "channel";

    public static final String SUBSCRIPTION_ERROR = "subscriptionError";
    public static final String NO_FREE_ADAPTOR = "noFreeAdapter";
    public static final String SCRAMBLED = "scrambled";
    public static final String BAD_SIGNAL = "badSignal";
    public static final String TUNING_FAILED = "tuningFailed";
    public static final String SUBSCRIPTION_OVERRIDDEN = "subscriptionOverridden";
    public static final String MUX_NOT_ENABLED= "muxNotEnabled";
    public static final String INVALID_TARGET = "invalidTarget";
    public static final String USER_ACCESS = "userAccess";
    public static final String USER_LIMIT = "userLimit";
    
    public static final int NULL_CHANNEL = -5;
    public static final int NULL_PROGRAM = -5;
    public static final int NULL_RECORDING = -5;

    public static final int FALLBACK_SUBSCRIPTION_ID = -1;

    // HTSP Methods
    public static final Set<String> AUTH_METHODS = new ArraySet<>(Arrays.asList("hello","authenticate"));
    public static final Set<String> EPG_METHODS = new ArraySet<>(Arrays.asList("channelAdd","eventAdd","channelUpdate","eventUpdate","initialSyncCompleted","dvrEntryAdd","dvrEntryUpdate"));
    public static final Set<String> SUBSCRIPTION_METHODS = new ArraySet<>(Arrays.asList("subscriptionStart","subscriptionStatus","subscriptionStop","subscriptionSkip","subscriptionSpeed","muxpkt","timeshiftStatus"));

    // TVHeadEnd Audio Sample Rates
    public static final int[] AUDIO_SAMPLE_RATES = new int[]{
            96000, 88200, 64000, 48000,
            44100, 32000, 24000, 22050,
            16000, 12000, 11025,  8000,
            7350,     0,     0,     0
    };

    // Extended Extractors
    public static final int NUM_OF_EXTRACTORS = 13;

    // Preferences
    public static final int PREFERENCE_NOT_SET_INT = -1;
    public static final String PREFERENCE_NOT_SET_STRING = "";
    public static final boolean PREFERENCE_NOT_SET_BOOL = false;

    public static final String PREFERENCE_SETUP_COMPLETE = "SETUP_COMPLETE";

    //Keys
    public static final String KEY_PROFILE = "STREAM_PROFILE";
    public static final String KEY_QUICK_SYNC = "QUICK_SYNC";
    public static final String KEY_EPG_SYNC = "EPG_SYNC";
    public static final String KEY_BUFFER = "BUFFER_SIZE";
    public static final String KEY_DVR = "DVR_STORAGE";

}
