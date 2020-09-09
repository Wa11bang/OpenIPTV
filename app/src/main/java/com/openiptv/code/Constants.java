package com.openiptv.code;

public class Constants {
    public static final boolean DEBUG = true; // TODO: implement debug check in code
    public static final String ACCOUNT = "openiptv";

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
    public static final String PROGRAM_TITLE_KEY = "title";
    public static final String PROGRAM_DESCRIPTION_KEY = "description";
    public static final String PROGRAM_SUMMARY_KEY = "summary";
    public static final String PROGRAM_CONTENT_TYPE_KEY = "contentType";
    public static final String PROGRAM_AGE_RATING_KEY = "ageRating";
    public static final String PROGRAM_START_TIME_KEY = "start";
    public static final String PROGRAM_FINISH_TIME_KEY = "stop";
    public static final String PROGRAM_IMAGE = "image";
}
