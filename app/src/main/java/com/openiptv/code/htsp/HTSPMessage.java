package com.openiptv.code.htsp;

import android.util.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.openiptv.code.Constants.DEBUG;

/**
 *
 */
public class HTSPMessage extends HashMap<String, Object> {
    public HTSPMessage(Map<? extends String, ?> message)
    {
        super(message);
    }

    public HTSPMessage()
    {

    }

    @Override
    public Object put(String key, Object value)
    {
        if(value == null)
        {
            if(DEBUG) {
                Log.w("HTSP", "Null Values not allowed!");
            }
            return null;
        }

        return super.put(key, value);
    }

    public String getString(String key, String fallback) {
        if (!containsKey(key)) {
            return fallback;
        }

        return getString(key);
    }

    public String getString(String key) {
        Object obj = get(key);
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public long getLong(String key, long fallback) {
        if (!containsKey(key)) {
            return fallback;
        }

        return getLong(key);
    }

    public int getInteger(String key, int fallback) {
        if (!containsKey(key)) {
            return fallback;
        }

        return getInteger(key);
    }

    public int getInteger(String key) {
        Object obj = get(key);
        if (obj == null) {
            if(DEBUG) {
                System.out.println("Attempted to getInteger(" + key + ") on non-existent key");
            }
            return 0;
        }
        if (obj instanceof BigInteger) {
            return ((BigInteger) obj).intValue();
        }

        return (int) obj;
    }

    public long getLong(String key) {
        Object obj = get(key);
        if (obj == null) {
            throw new RuntimeException("Attempted to getLong("+key+") on non-existent key");
        }

        if (obj instanceof BigInteger) {
            return ((BigInteger) obj).longValue();
        }

        return (long) obj;
    }

    public byte[] getByteArray(String key) {
        if (!containsKey(key)) {
            return new byte[0];
        }
        Object value = get(key);
        return (byte[]) value;
    }

    public ArrayList getArrayList(String key, ArrayList fallback) {
        if (!containsKey(key)) {
            return fallback;
        }

        return getArrayList(key);
    }

    public ArrayList getArrayList(String key) {
        Object obj = get(key);

        return (ArrayList<String>) obj;
    }

    public HTSPMessage[] getHtspMessageArray(String key, HTSPMessage[] fallback) {
        if (!containsKey(key)) {
            return fallback;
        }

        return getHtspMessageArray(key);
    }

    public HTSPMessage[] getHtspMessageArray(String key) {
        ArrayList value = getArrayList(key);

        return (HTSPMessage[]) value.toArray(new HTSPMessage[value.size()]);
    }
}
