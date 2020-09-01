import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test extends Thread {
    private SocketChannel socketChannel = null;
    private Selector mSelector;
    private DataInputStream input   = null;
    private DataOutputStream out     = null;
    private Lock mLock = new ReentrantLock();

    private static final byte FIELD_MAP = 1;
    private static final byte FIELD_S64 = 2;
    private static final byte FIELD_STR = 3;
    private static final byte FIELD_BIN = 4;
    private static final byte FIELD_LIST = 5;

    private final ByteBuffer mWriteBuffer = ByteBuffer.allocateDirect(1024 * 1024); // 1024 * 1024 = Max TVH will accept
    private final ByteBuffer mReadBuffer = ByteBuffer.allocateDirect(5242880); // 5MB
    private byte[] challenge;
    private Scanner scanner = new Scanner(System.in);
    private boolean connected = false;
    private boolean i = false;
    private ConnectionInfo connectionInfo = new ConnectionInfo("10.0.0.57", 9982, "Waldo", "Walo01jani02", "open-iptv", "");

    private static final AtomicInteger sSequence = new AtomicInteger();

    @Override
    public void run() {
        boolean connect = openConnection();

        while (connect) {
            if (mSelector == null) {
                break;
            }

            try {
                mSelector.select();
            } catch (IOException e) {
                break;
            }

            if (mSelector == null || !mSelector.isOpen()) {
                break;
            }

            Set<SelectionKey> keys = mSelector.selectedKeys();
            Iterator<SelectionKey> i = keys.iterator();

            try {
                while (i.hasNext()) {
                    SelectionKey selectionKey = i.next();
                    i.remove();

                    if (!selectionKey.isValid()) {
                        break;
                    }

                    if (selectionKey.isValid() && selectionKey.isConnectable()) {
                        System.out.println("Connectable");
                        connectableSelectionKey(selectionKey);
                    }

                    if (selectionKey.isValid() && selectionKey.isReadable()) {
                        System.out.println("Readable");
                        readableSelectionKey(selectionKey);
                    }

                    if (selectionKey.isValid() && selectionKey.isWritable() && connected) {
                        System.out.println("Writable");
                        handleInputs();
                    }
                }

                /*if (socketChannel != null && socketChannel.isConnected()) {
                    socketChannel.register(mSelector, SelectionKey.OP_READ);
                }*/
            } catch (Exception e) {
                 System.out.println("ERROR");
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleInputs()
    {

        String l = "";

            if (scanner.hasNextLine()) {
                l = scanner.nextLine();

                if (l.equals("auth")) {

                    HTSPMessage msg = new HTSPMessage();
                    System.out.println(Arrays.toString(challenge));

                    msg.put("method", "authenticate");
                    msg.put("username", connectionInfo.getUsername());
                    msg.put("digest", calculateDigest(connectionInfo.getPassword()));

                    if (writeToSocket(socketChannel, msg)) {
                        System.out.println("Success");
                    } else {
                        System.out.println("Error");
                    }
                }

                if (l.equals("c")) {
                    HTSPMessage msg = new HTSPMessage();

                    msg.put("method", "enableAsyncMetadata");

                    connected = false;

                    writeToSocket(socketChannel, msg);

                }

                if(l.equals("e"))
                {
                    long epgMaxTime = 0L;
                    boolean mQuickSync = false;
                    System.out.println("Enabling Async Metadata: maxTime: " + epgMaxTime + ", quickSync: " + mQuickSync);

                    HTSPMessage enableAsyncMetadataRequest = new HTSPMessage();

                    enableAsyncMetadataRequest.put("method", "enableAsyncMetadata");
                    enableAsyncMetadataRequest.put("epg", 1);

                    epgMaxTime = epgMaxTime + (System.currentTimeMillis() / 1000L);
                    enableAsyncMetadataRequest.put("epgMaxTime", epgMaxTime);

                    writeToSocket(socketChannel, enableAsyncMetadataRequest);

                }
            }
    }

    private void connectableSelectionKey(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        try {
            System.out.println("Finishing COnnection");
            socketChannel.finishConnect();
            sendHello();
        } catch (ConnectException e) {
            return;
        }
    }

    private void sendHello()
    {
        HTSPMessage message = new HTSPMessage();

        message.put("method", "hello");
        message.put("htspversion", 23);
        message.put("clientname", connectionInfo.getClientName());
        message.put("clientversion", connectionInfo.getClientVersion());

        writeToSocket(socketChannel, message);
    }

    private void readableSelectionKey(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        handleResponse();
    }

    public void handleResponse()
    {
        new Thread(this::packageResponse).start();
    }

    private void packageResponse()
    {
        try {
            while (socketChannel.read(mReadBuffer) > 0) {
                System.out.println("Response:");
                HTSPMessage response = readFromSocket(mReadBuffer);
                ArrayList<String> capabilities = new ArrayList<String>(response.keySet());
                for(String s : capabilities)
                {
                    if(s.contains("challenge"))
                    {
                        challenge = response.getByteArray("challenge");
                        System.out.println("Challenge Stored!");
                    }

                    if(s.contains("channelAdd"))
                    {
                        System.out.println(response.getByteArray("channel"));
                    }
                    System.out.println(s);
                }

                mReadBuffer.clear();

                    connected = true;

            }
        } catch (IOException e) {
            System.out.println("NOT CONNECTED!");
        }
    }

    private byte[] calculateDigest(String password) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Your platform doesn't support SHA-1");
        }

        try {
            md.update(password.getBytes("utf8"));
            md.update(challenge);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Your platform doesn't support UTF-8");
        }

        return md.digest();
    }

    public boolean openConnection()
    {
        mLock.lock();

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress("10.0.0.57", 9982));
            mSelector = Selector.open();
            int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
            socketChannel.register(mSelector, operations);


        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            mLock.unlock();
        }

        return true;
    }

    public void writeSerialize(ByteBuffer buffer,HTSPMessage message) {
        // Skip forward 4 bytes to make space for the length field
        buffer.position(4);

        // Write the data
        serialize(buffer, message);

        // Figure out how long the data is
        int dataLength = buffer.position() - 4;

        // Drop in the length
        byte[] lengthBytes = long2bin(dataLength);

        for(int i=0; i < lengthBytes.length; i++){
            buffer.put(i, lengthBytes[i]);
        }
    }

    protected void serialize(ByteBuffer buffer, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            serialize(buffer, entry.getKey(), entry.getValue());
        }
    }

    protected void serialize(ByteBuffer buffer, Iterable<?> list) {
        for (Object value : list) {
            // Lists are just like maps, but with empty / zero length keys.
            serialize(buffer, "", value);
        }
    }

    public boolean writeToSocket(SocketChannel socketChannel, HTSPMessage msg) {
        // Clear the buffer out, ready for a new message.
        mWriteBuffer.clear();

        if(!socketChannel.isConnected())
        {
            System.out.println("Socket not connected!");
            return false;
        }

        writeSerialize(mWriteBuffer, msg);

        // Flip the buffer, limit=position, position=0.
        mWriteBuffer.flip();

        try {
            int bytesWritten = socketChannel.write(mWriteBuffer);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public HTSPMessage readFromSocket(ByteBuffer buffer) {
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
            throw new RuntimeException("Message exceeds buffer capacity ("+buffer.capacity()+"): " + fullLength);
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

    @SuppressWarnings("unchecked") // We cast LOTS here...
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

            // 50000000 is ~50MB, aka improbably large. Without this guard, we'll get a series of
            // OutOfMemoryError crash reports, which don't group nicely as the values are always
            // different. This makes it hard to understand the extent of the issue or begin tracing
            // the bug (it may even be a TVHeadend bug?)
            if (valueLength > 50000000) {
                System.out.println("Attempted to deserialize an improbably large field (" + valueLength + " bytes)");
                throw new RuntimeException("Attempted to deserialize an improbably large field");
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

    private static BigInteger toBigInteger(byte b[]) {
        byte b1[] = new byte[b.length + 1];

        // Reverse the order
        for (int i = 0; i < b.length; i++) {
            b1[i + 1] = b[b.length - 1 - i];
        }

        // Convert to a BigInteger
        return new BigInteger(b1);
    }

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

    public static void main(String... args)
    {
        Test t = new Test();
        t.run();
    }


}
