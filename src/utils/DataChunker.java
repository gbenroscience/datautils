/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;


import com.google.gson.Gson;
import com.itis.liveservice.include.Config;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

/**
 * Loops through a byte array or InputStream or ByteBuffer , File or String and
 * produces chunks of it in sequential fashion. In the process, it fires
 * chunkFound events when it detects chunks of data of the specified size. This
 * will allow the user to do some pre-processing on the chunk before using it. A
 * grand application may be with a websocket server or client which cannot
 * accept more than a fixed number of bytes per payload. You may use objects of
 * this class to break the data up into simple chunks and transmit over the
 * connection and then rebuild on the other end. Since chunks are generated
 * sequentially, there is no fear of data corruption. Just re-couple the chunks
 * as they come in. When it gets to the end of the array, it fires the remaining
 * chunk(which may not be up to the specified {@link DataChunker#chunkSize}) at
 * the end.
 *
 * Each chunk is guaranteed to be of the specified {@link DataChunker#chunkSize}
 * except for the final chunk, as expected. {@link DataChunker#chunkSize}
 *
 * @author JIBOYE, Oluwagbemiro Olaoluwa <gbenroscience@yahoo.com>
 */
public abstract class DataChunker {

    /**
     * The size with which you want to process the stream or byte array.
     */
    private int chunkSize;

    private boolean valid;

    /**
     *
     * @param chunkSize The sizeRatio of each chunk. Each chunk generated is
     * guaranteed to have this sizeRatio, save for the final chunk, which will
     * have a sizeRatio equal to the remaining number of elements in the main
     * array.
     *
     * You may check the {@link DataChunker#isValid() } method to be sure that
     * no error occurred during chunking.
     * @param blob The stream whose data is to be broken into chunks
     */
    public DataChunker(int chunkSize, InputStream blob) {
        this.chunkSize = chunkSize;
        chunk(blob);
    }

    /**
     *
     * @param chunkSize The size of each chunk. Each chunk generated is
     * guaranteed to have this size, save for the final chunk, which will have a
     * size equal to the remaining number of elements in the main array.
     * @param blob The array whose data is to be broken into chunks
     */
    public DataChunker(int chunkSize, byte[] blob) {
        this.chunkSize = chunkSize;
        chunk(blob);
    }

    /**
     *
     * @param chunkSize The size of each chunk. Each chunk generated is
     * guaranteed to have this size, save for the final chunk, which will have a
     * size equal to the remaining number of elements in the main array.
     *
     * You may check the {@link DataChunker#isValid() } method to be sure that
     * no error occurred during chunking.
     * @param blob The File whose data is to be broken into chunks.
     */
    public DataChunker(int chunkSize, File blob) {
        this.chunkSize = chunkSize;

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(blob))) {
            chunk(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param chunkSize The size of each chunk. Each chunk generated is
     * guaranteed to have this size, save for the final chunk, which will have a
     * size equal to the remaining number of elements in the main array.
     *
     * You may check the {@link DataChunker#isValid() } method to be sure that
     * no error occurred during chunking.
     * @param blob The File whose data is to be broken into chunks.
     */
    public DataChunker(int chunkSize, ByteBuffer blob) {
        this.chunkSize = chunkSize;

        try {
            chunk(blob);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param chunkSize The size of each chunk. Each chunk generated is
     * guaranteed to have this size, save for the final chunk, which will have a
     * size equal to the remaining number of elements in the main array.
     *
     * You may check the {@link DataChunker#isValid() } method to be sure that
     * no error occurred during chunking.
     * @param blob A block of text to be broken into chunks. The chunks of text
     * are produced as byte array chunks. If you need them to be produced as
     * text chunks(substrings), then look at
     * @param charset The charset of the string
     */
    public DataChunker(int chunkSize, String blob, Charset charset) {
        this.chunkSize = chunkSize;
        chunk(blob, charset);

    }

    public boolean isValid() {
        return valid;
    }

    private void chunk(InputStream blob) {
        this.valid = false;
        try {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = blob.read(buffer, 0, chunkSize)) != -1) {
                totalBytesRead += bytesRead;
                byte[] chunk = bytesRead < chunkSize ? Arrays.copyOf(buffer, bytesRead) : buffer;
                chunkFound(chunk, totalBytesRead);
            }
            chunksExhausted(totalBytesRead);
            this.valid = true;
        } catch (IOException ex) {
            System.err.println("Chunking failed: " + ex.getMessage());
        } finally {
            try {
                blob.close();
            } catch (IOException ex) {
                System.err.println("Failed to close stream: " + ex.getMessage());
            }
        }
    }

    /**
     * Upgraded chunk method for delivering byte arrays in chunks. 1. Uses
     * System.arraycopy to copy data. 2. Does not do byte by byte copy in Java
     * again, instead does it using 1. above. 3. Does not create new chunking
     * array instances except for the last chunk, if the size of the last chunk
     * is less than the chunk size. So it creates one array for the chunks and
     * reuses it throughout the whole copying process. Speed gains will be high.
     *
     * @param blob The array to copy.
     */
    private void chunk(byte[] blob) {
        this.valid = false;
        try {
            int sentBytes = 0;
            int len = blob.length;
            while (sentBytes < len) {
                int remaining = len - sentBytes;
                int size = Math.min(chunkSize, remaining);
                byte[] chunk = new byte[size];
                System.arraycopy(blob, sentBytes, chunk, 0, size);
                sentBytes += size;
                chunkFound(chunk, sentBytes);
            }
            chunksExhausted(sentBytes);
            this.valid = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void chunk(ByteBuffer buffer) {
        this.valid = false;

        int remaining = buffer.remaining();
        int sentBytes = 0;

        while (remaining > 0) {
            int size = Math.min(chunkSize, remaining);
            byte[] chunk = new byte[size];
            buffer.get(chunk); // advances buffer position
            sentBytes += size;
            chunkFound(chunk, sentBytes); // send chunk
            remaining -= size;
        }

        chunksExhausted(sentBytes);
        this.valid = true;
    }

    /**
     *
     * @param blob The text to be processed in bytes.
     * @param charset 
     */
private void chunk(String blob, Charset charset) {
    this.valid = false;
    byte[] allBytes = blob.getBytes(charset);
    int len = allBytes.length;
    int bytesSent = 0;
    for (int i = 0; i < len; i += chunkSize) {
        int end = Math.min(i + chunkSize, len);
        byte[] chunk = Arrays.copyOfRange(allBytes, i, end);
        bytesSent += chunk.length;
        chunkFound(chunk, bytesSent);
    }
    chunksExhausted(len);
    this.valid = true;
}

    /**
     * Called when a chunked array.
     *
     * @param foundChunk The current chunk.
     * @param bytesProcessed The total number of bytes processed including the
     * current chunk.
     */
    public abstract void chunkFound(byte[] foundChunk, long bytesProcessed);

    /**
     * Fired when all chunks have been detected.
     *
     * @param bytesProcessed The total number of bytes processed.
     */
    public abstract void chunksExhausted(long bytesProcessed);

    public static void main(String[] args) {
        byte[] b = new byte[2000];
        Random r = new Random(System.nanoTime());
        r.nextBytes(b);

        Config.logInfo(new Gson().toJson(b));
        ByteBuffer bb = ByteBuffer.wrap(b);System.out.println("direct: "+bb.isDirect());

        DataChunker chunker = new DataChunker(10, bb) {
            @Override
            public void chunkFound(byte[] foundChunk, long bytesProcessed) {
                Config.logInfo(new Gson().toJson(foundChunk) + "...Now processed " + bytesProcessed + " bytes in " + foundChunk.length + " byte chunks");
            }

            @Override
            public void chunksExhausted(long bytesProcessed) {
                Config.logInfo("All " + bytesProcessed + " bytes processed; Thanks");
            }
        };

    }

}
