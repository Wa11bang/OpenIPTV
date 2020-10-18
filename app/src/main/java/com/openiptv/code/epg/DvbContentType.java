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
        contentTypes.put(16,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(17,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(18,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(19,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(20,TvContract.Programs.Genres.encode(COMEDY));
        contentTypes.put(21,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(22,TvContract.Programs.Genres.encode(MOVIES));
        contentTypes.put(23,TvContract.Programs.Genres.encode(DRAMA));
        contentTypes.put(32,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(33,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(34,TvContract.Programs.Genres.encode(NEWS));
        contentTypes.put(35,TvContract.Programs.Genres.encode(TECH_SCIENCE));
        contentTypes.put(48,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(49,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(50,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(51,TvContract.Programs.Genres.encode(ENTERTAINMENT));
        contentTypes.put(64,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(65,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(66,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(67,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(68,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(69,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(70,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(71,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(72,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(73,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(74,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(75,TvContract.Programs.Genres.encode(SPORTS));
        contentTypes.put(80,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(81,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(82,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(83,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(84,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(85,TvContract.Programs.Genres.encode(FAMILY_KIDS));
        contentTypes.put(96,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(97,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(98,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(99,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(100,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(101,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(102,TvContract.Programs.Genres.encode(MUSIC));
        contentTypes.put(112,TvContract.Programs.Genres.encode(ARTS));
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
