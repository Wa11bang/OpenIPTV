package com.openiptv.code.htsp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketIOHandler {
    private final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024 * 1024);
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(5242880);
    private final HTSPSerializer htspSerializer;
    private final HTSPMessageDispatcher htspMessageDispatcher;

    SocketIOHandler(HTSPSerializer htspSerializer, HTSPMessageDispatcher htspMessageDispatcher)
    {
        this.htspSerializer = htspSerializer;
        this.htspMessageDispatcher = htspMessageDispatcher;
    }

    public boolean hasWriteableData() {
        return htspMessageDispatcher.hasPendingMessages();
    }

    public boolean write(SocketChannel socketChannel) {
        //System.out.println("writing");
        writeBuffer.clear();

        HTSPMessage message = htspMessageDispatcher.getMessage();

            // Write the message to the buffer
        htspSerializer.write(writeBuffer, message);

            // Flip the buffer, limit=position, position=0.
        writeBuffer.flip();

        try {
            int bytesWritten = socketChannel.write(writeBuffer);
            //System.out.println("Wrote " + bytesWritten + " bytes to SocketChannel");
        } catch (IOException e) {
            System.out.println("Failed to write buffer to SocketChannel");
            return false;
        }

        return true;
    }

    public boolean read(SocketChannel socketChannel) {
        int bufferStartPosition = readBuffer.position();
        int bytesRead;

        try {
            bytesRead = socketChannel.read(readBuffer);
            //System.out.println("Read " + bytesRead + " bytes.");
        } catch (IOException e) {
            System.out.println("Failed to read from SocketChannel " + e);
            return false;
        }

        if (bytesRead == -1) {
            System.out.println(" Failed to read from SocketChannel, read -1 bytes");
            return false;
        } else if (bytesRead == 0) {
            // No data read, continue
            return true;
        }

        int bytesToBeConsumed = bufferStartPosition + bytesRead;
        // Flip the buffer, limit=position, position=0
        readBuffer.flip();

        // Read messages out of the buffer one by one, until we either:
        // * Consume 0 bytes, meaning we only have a partial message in the buffer.
        // * Have no remaining bytes left to consume.
        int bytesConsumed = -1;

        while (bytesConsumed != 0 && bytesToBeConsumed > 0) {
            // Ensure the buffer is at the start each of iteration, as we'll always have the
            // start of a message at this point (or it'll be empty)
            readBuffer.position(0);

            // Build a message
            HTSPMessage message = htspSerializer.read(readBuffer);

            if (message == null) {
                // We didn't have enough data to read a message.
                bytesConsumed = 0;
                continue;
            }

            // Dispatch the Message to it's listeners
            htspMessageDispatcher.onMessage(message);

            // We've read a full message. Our position() is set to the end of the message, and
            // out limit may also set to the position() / end of the message.

            // Figure out how much data we consumed
            bytesConsumed = readBuffer.position();

            // Reset the limit to the known full amount of data we had
            readBuffer.limit(bytesToBeConsumed);

            // Compact the buffer - position=limit, limit=capacity
            readBuffer.compact();

            // Figure out how much data is left to consume
            bytesToBeConsumed = bytesToBeConsumed - bytesConsumed;

            // Reset the limit to the known full amount of data we have remaining
            readBuffer.limit(bytesToBeConsumed);
        }

        // Place ourselves back at the right spot in the buffer, so that new reads append
        // rather than override the as yet unconsumed data.
        readBuffer.position(bytesToBeConsumed);

        // Ensure we have space to add data for the next iteration
        readBuffer.limit(readBuffer.capacity());

        return true;
    }
}
