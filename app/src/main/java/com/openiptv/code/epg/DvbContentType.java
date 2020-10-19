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
        contentTypes.put(113,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(114,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(115,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(116,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(117,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(118,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(120,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(121,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(122,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(129,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(144,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(145,TvContract.Programs.Genres.encode(ANIMAL_WILDLIFE));
        contentTypes.put(146,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(147,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(148,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(150,TvContract.Programs.Genres.encode(EDUCATION));
        contentTypes.put(160,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(161,TvContract.Programs.Genres.encode(TRAVEL));
        contentTypes.put(162,TvContract.Programs.Genres.encode(ARTS));
        contentTypes.put(163,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(164,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(165,TvContract.Programs.Genres.encode(LIFE_STYLE));
        contentTypes.put(166,TvContract.Programs.Genres.encode(SHOPPING));
        contentTypes.put(167,TvContract.Programs.Genres.encode(LIFE_STYLE));


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
