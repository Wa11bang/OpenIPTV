package com.openiptv.code;

public class Constants {
    public static final boolean DEBUG = true; // TODO: implement debug check in code
    public static final String ACCOUNT = "openiptv";
    public static final String COMPONENT_PACKAGE = "com.openiptv.code";
    public static final String COMPONENT_CLASS = ".input.TVInputService";


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

    public static final int NULL_CHANNEL = -5;
    public static final int NULL_PROGRAM = -5;
}
