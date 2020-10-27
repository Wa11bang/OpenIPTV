package com.openiptv.code.epg.OMDB;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

/*
    Developed by Waldo Theron
    For use with the OMBDAPI service.
 */

public class OMDBAPI {
    private String iMDbId;
    private String title;
    private Type type;
    private String year;
    private String plotSize;
    private String returnType;
    private String apiKey;
    private URL url;
    private HttpsURLConnection conn;

    private final static String HOSTNAME = "omdbapi.com";

    public String getiMDbId() {
        return iMDbId;
    }

    public enum Type {
        MOVIE,
        SERIES,
        EPISODE
    }

    public OMDBAPI()
    {
        this.setReturnType("json");
    }

    public OMDBAPI(String apiKey)
    {
        this.setApiKey(apiKey);
        this.setReturnType("json");
    }

    public OMDBAPI(String returnType, String apiKey)
    {
        this.setReturnType(returnType);
        this.setApiKey(apiKey);
    }

    public String byID()
    {
        if(iMDbId == null)
        {
            return "{error: \"iMDb ID not set\"}";
        }
        StringBuilder response = new StringBuilder();
        String urlS = "https://"+HOSTNAME+"/?apikey="+apiKey+"&i="+iMDbId
                + "&type="+type
                + "&y="+year
                + "&plot="+plotSize
                + "&r="+returnType;


        try {
            url = new URL(urlS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return run(response);
    }

    public String byTitle()
    {
        if(title == null)
        {
            return "{error: \"title not set\"}";
        }
        StringBuilder response = new StringBuilder();
        String urlS = null;
        try {
            urlS = "https://"+HOSTNAME+"/?apikey="+apiKey+"&t=" + URLEncoder.encode(title, String.valueOf(StandardCharsets.UTF_8))
                    + "&type="+type
                    + "&y="+year
                    + "&plot="+plotSize
                    + "&r="+returnType;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            assert urlS != null;
            url = new URL(urlS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return run(response);
    }

    public String search()
    {
        if(title == null)
        {
            return "{error: \"title not set\"}";
        }
        StringBuilder response = new StringBuilder();
        String urlS = null;
        try {
            urlS = "https://"+HOSTNAME+"/?apikey="+apiKey+"&s=" + URLEncoder.encode(title, String.valueOf(StandardCharsets.UTF_8))
                    + "&type="+type
                    + "&y="+year
                    + "&plot="+plotSize
                    + "&r="+returnType;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            assert urlS != null;
            url = new URL(urlS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return run(response);
    }

    private String run(StringBuilder response)
    {
        try {
            conn = (HttpsURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if(responseCode == 200)
            {
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNextLine())
                {
                    response.append(scanner.nextLine());
                }
                scanner.close();
            }

            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    public OMDBAPI setiMDbId(String iMDbId) {
        this.iMDbId = iMDbId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public OMDBAPI setTitle(String title) {
        this.title = title;
        return this;
    }

    public Type getType() {
        return type;
    }

    public OMDBAPI setType(Type type) {
        this.type = type;
        return this;
    }

    public String getYear() {
        return year;
    }

    public OMDBAPI setYear(String year) {
        this.year = year;
        return this;
    }

    public String getPlotSize() {
        return plotSize;
    }

    public OMDBAPI setPlotSize(String plotSize) {
        this.plotSize = plotSize;
        return this;
    }

    public String getReturnType() {
        return returnType;
    }

    public OMDBAPI setReturnType(String returnType) {
        this.returnType = returnType;
        return this;
    }

    public OMDBAPI setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }
}
