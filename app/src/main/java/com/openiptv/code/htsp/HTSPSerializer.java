package com.openiptv.code.htsp;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class HTSPSerializer {
    /*
        DataType Constant for Map Object
     */
    private static final byte FIELD_MAP = 1;

    /*
        DataType Constant for Signed 64-bit Integer Object
     */
    private static final byte FIELD_S64 = 2;

    /*
        DataType Constant for String Object
     */
    private static final byte FIELD_STR = 3;

    /*
        DataType Constant for Raw Byte Array Object
     */
    private static final byte FIELD_BIN = 4;

    /*
        DataType Constant for List Object
     */
    private static final byte FIELD_LIST = 5;

    /**
     * This methods reads in a byte buffer, which is then parsed into an HTSPMessage object,
     *
     * @param buffer to parse
     * @return HTSPMessage object from parsed buffer
     */
    public HTSPMessage read(ByteBuffer buffer) {
        if (buffer.limit() < 4) {
            System.out.println("Buffer does not have enough data to read a message length");
            return null;
        }

        byte[] lenBytes = new byte[4];

        lenBytes[0] = buffer.get(0);
        lenBytes[1] = buffer.get(1);
        lenBytes[2] = buffer.get(2);
        lenBytes[3] = buffer.get(3);

        int length = (int) bin2long(lenBytes);
        int fullLength = length + 4;

        if (buffer.capacity() < fullLength) {
            throw new RuntimeException("Message exceeds buffer capacity: " + fullLength);
        }

        // Keep reading until we have the entire message
        if (buffer.limit() < fullLength) {
            return null;
        }

        // Set the buffers limit to ensure we don't read data belonging to the next message...
        buffer.limit(fullLength);

        buffer.position(4);

        HTSPMessage message = deserialize(buffer);

        return message;
    }

    /**
     * This method writes a given ByteBuffer to a given HTSPMessage object.
     * The ByteBuffer is serialised to the message.
     *
     * @param buffer  to write from
     * @param message to write to
     */
    public void write(ByteBuffer buffer, @NonNull HTSPMessage message) {
        // Skip forward 4 bytes to make space for the length field
        buffer.position(4);

        // Write the data
        serialize(buffer, message);

        // Figure out how long the data is
        int dataLength = buffer.position() - 4;

        // Drop in the length
        byte[] lengthBytes = long2bin(dataLength);

        for (int i = 0; i < lengthBytes.length; i++) {
            buffer.put(i, lengthBytes[i]);
        }
    }

    /**
     * Method used to accept a Map which is then serialised to a given ByteBuffer
     *
     * @param buffer to serialise to
     * @param map    to serialise
     */
    protected void serialize(ByteBuffer buffer, Map<String, Object> map) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            serialize(buffer, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Method used to accept Iterable objects which are serialised to a given ByteBuffer
     *
     * @param buffer to serialise to
     * @param list   to serialise
     */
    protected void serialize(ByteBuffer buffer, Iterable<?> list) {
        for (Object value : list) {
            // Lists are just like maps, but with empty / zero length keys.
            serialize(buffer, "", value);
        }
    }

    /**
     * This method serialises a key and value into a given ByteBuffer
     *
     * @param buffer to serialise to
     * @param key    to serialise
     * @param value  to serialise
     */
    protected void serialize(ByteBuffer buffer, String key, Object value) {
        byte[] keyBytes = key.getBytes();
        ByteBuffer valueBytes = ByteBuffer.allocate(65535);

        // 1 byte type
        if (value == null) {
            // Ignore and do nothing
            return;
        } else if (value instanceof String) {
            buffer.put(FIELD_STR);
            valueBytes.put(((String) value).getBytes());
        } else if (value instanceof BigInteger) {
            buffer.put(FIELD_S64);
            valueBytes.put(toByteArray((BigInteger) value));
        } else if (value instanceof Integer) {
            buffer.put(FIELD_S64);
            valueBytes.put(toByteArray(BigInteger.valueOf((Integer) value)));
        } else if (value instanceof Long) {
            buffer.put(FIELD_S64);
            valueBytes.put(toByteArray(BigInteger.valueOf((Long) value)));
        } else if (value instanceof Map) {
            buffer.put(FIELD_MAP);
            serialize(valueBytes, (Map<String, Object>) value);
        } else if (value instanceof byte[]) {
            buffer.put(FIELD_BIN);
            valueBytes.put((byte[]) value);
        } else if (value instanceof Iterable) {
            buffer.put(FIELD_LIST);
            serialize(valueBytes, (Iterable<?>) value);
        } else {
            throw new RuntimeException("Cannot serialize unknown data type, derp: " + value.getClass().getName());
        }

        // 1 byte key length
        buffer.put((byte) (keyBytes.length & 0xFF));

        // Reset the Value Buffer and grab it's length
        valueBytes.flip();
        int valueLength = valueBytes.limit();

        // 4 bytes value length
        buffer.put(long2bin(valueLength));

        // Key + Value Bytes
        buffer.put(keyBytes);
        buffer.put(valueBytes);
    }

    /**
     * This method deserialises a byte buffer into an HTSPMessage object
     *
     * @param buffer
     * @return
     */
    protected static HTSPMessage deserialize(ByteBuffer buffer) {
        HTSPMessage message = new HTSPMessage();

        byte fieldType;
        String key;
        byte keyLength;
        byte[] valueLengthBytes = new byte[4];
        long valueLength;
        byte[] valueBytes;
        Object value = null;

        int listIndex = 0;

        while (buffer.hasRemaining()) {
            fieldType = buffer.get();
            keyLength = buffer.get();
            buffer.get(valueLengthBytes);
            valueLength = bin2long(valueLengthBytes);

            if (valueLength > 50000000) {
                throw new RuntimeException("Attempted to deserialise an invalid field.");
            }

            // Deserialize the Key
            if (keyLength == 0) {
                // Working on a list...
                key = Integer.toString(listIndex++);
            } else {
                // Working on a map..
                byte[] keyBytes = new byte[keyLength];
                buffer.get(keyBytes);
                key = new String(keyBytes);
            }

            // Extract Value bytes
            valueBytes = new byte[(int) valueLength];
            buffer.get(valueBytes);

            // Deserialize the Value
            if (fieldType == FIELD_STR) {
                value = new String(valueBytes);

            } else if (fieldType == FIELD_S64) {
                value = toBigInteger(valueBytes);

            } else if (fieldType == FIELD_MAP) {
                value = deserialize(ByteBuffer.wrap(valueBytes));

            } else if (fieldType == FIELD_LIST) {
                value = new ArrayList<>(deserialize(ByteBuffer.wrap(valueBytes)).values());

            } else if (fieldType == FIELD_BIN) {
                value = valueBytes;

            } else {
                throw new RuntimeException("Cannot deserialize unknown data type, derp: " + fieldType);
            }

            if (value != null) {
                message.put(key, value);
            }
        }

        return message;
    }

    /**
     * Convert a byte array into a Big Integer
     *
     * @param b byte array
     * @return big int
     */
    private static BigInteger toBigInteger(byte b[]) {
        byte b1[] = new byte[b.length + 1];

        // Reverse the order
        for (int i = 0; i < b.length; i++) {
            b1[i + 1] = b[b.length - 1 - i];
        }

        // Convert to a BigInteger
        return new BigInteger(b1);
    }

    /**
     * Convert a byte array into a long
     *
     * @param bytes byte array
     * @return long
     */
    private static long bin2long(byte[] bytes) {
        /**
         *  return (ord(d[0]) << 24) + (ord(d[1]) << 16) + (ord(d[2]) <<  8) + ord(d[3])
         */
        long result = 0;

        result ^= (bytes[0] & 0xFF) << 24;
        result ^= (bytes[1] & 0xFF) << 16;
        result ^= (bytes[2] & 0xFF) << 8;
        result ^= bytes[3] & 0xFF;

        return result;
    }

    /**
     * Convert a BigInt to ByteArray following HTSP standards
     *
     * @param big bigint
     * @return byte array
     */
    private static byte[] toByteArray(BigInteger big) {
        // Convert to a byte array
        byte[] b = big.toByteArray();

        // Reverse the byte order
        byte b1[] = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            b1[i] = b[b.length - 1 - i];
        }

        // Negative numbers in HTSP are weird
        if (big.compareTo(BigInteger.ZERO) < 0) {
            byte[] b3 = new byte[8];
            Arrays.fill(b3, (byte) 0xFF);
            System.arraycopy(b1, 0, b3, 0, b1.length - 1);
            return b3;
        }

        return b1;
    }

    /**
     * Converts a long to a byte array
     *
     * @param l long
     * @return byte array
     */
    private static byte[] long2bin(long l) {
        /**
         * return chr(i >> 24 & 0xFF) + chr(i >> 16 & 0xFF) + chr(i >> 8 & 0xFF) + chr(i & 0xFF)
         */
        byte[] result = new byte[4];

        result[0] = (byte) ((l >> 24) & 0xFF);
        result[1] = (byte) ((l >> 16) & 0xFF);
        result[2] = (byte) ((l >> 8) & 0xFF);
        result[3] = (byte) (l & 0xFF);

        return result;
    }
}
