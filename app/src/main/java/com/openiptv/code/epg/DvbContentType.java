package com.openiptv.code.epg;

import android.media.tv.TvContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static android.media.tv.TvContract.Programs.Genres.ANIMAL_WILDLIFE;
import static android.media.tv.TvContract.Programs.Genres.ARTS;
import static android.media.tv.TvContract.Programs.Genres.COMEDY;
import static android.media.tv.TvContract.Programs.Genres.DRAMA;
import static android.media.tv.TvContract.Programs.Genres.EDUCATION;
import static android.media.tv.TvContract.Programs.Genres.ENTERTAINMENT;
import static android.media.tv.TvContract.Programs.Genres.FAMILY_KIDS;
import static android.media.tv.TvContract.Programs.Genres.LIFE_STYLE;
import static android.media.tv.TvContract.Programs.Genres.MOVIES;
import static android.media.tv.TvContract.Programs.Genres.MUSIC;
import static android.media.tv.TvContract.Programs.Genres.NEWS;
import static android.media.tv.TvContract.Programs.Genres.SHOPPING;
import static android.media.tv.TvContract.Programs.Genres.TECH_SCIENCE;
import static android.media.tv.TvContract.Programs.Genres.SPORTS;
import static android.media.tv.TvContract.Programs.Genres.TRAVEL;

//
public class DvbContentType {
    private HashMap<Integer, String> contentTypes = new LinkedHashMap<>();

    /**
     * Constructor for a Program object
     * Classification number for each channel
     */
    public DvbContentType()
    {
        contentTypes.put(0x10,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(0x11,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(0x12,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(0x13,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(0x14,TvContract.Programs.Genres.encode(COMEDY));
        contentTypes.put(0x15,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(0x16,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(0x17,TvContract.Programs.Genres.encode(DRAMA));
        contentTypes.put(0x20,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(0x21,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(0x22,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(0x23,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(0x30,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(0x31,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(0x32,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(0x33,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(0x40,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x41,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x42,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x43,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x44,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x45,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x46,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x47,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x48,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x49,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x4A,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x4B,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(0x50,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(0x51,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(0x52,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(0x53,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(0x54,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(0x55,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(0x60,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(0x61,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(0x62,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(0x63,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(0x64,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(0x65,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(0x66,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(0x70,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(0x71,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(0x72,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(0x73,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(0x74,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(0x75,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(0x76,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(0x78,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(0x79,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(0x7A,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(0x81,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(0x90,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(0x91,TvContract.Programs.Genres.encode(ANIMAL_WILDLIFE));
        contentTypes.put(0x92,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(0x93,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(0x95,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(0x96,TvContract.Programs.Genres.encode(EDUCATION));
        contentTypes.put(0xA0,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(0xA1,TvContract.Programs.Genres.encode(TRAVEL));
        contentTypes.put(0xA2,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(0xA3,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(0xA4,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(0xA5,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(0xA6,TvContract.Programs.Genres.encode(SHOPPING));
        contentTypes.put(0xA7,TvContract.Programs.Genres.encode(LIFE_STYLE));
        
    }


    /**
     * Rreturn The contentType
     * @return contentType
     */
    public String getType(Integer key)
    {
        return contentTypes.get(key);
    }
}
