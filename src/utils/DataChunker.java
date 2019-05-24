/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;



/**
 * Loops through an array or InputStream and produces chunks of it in sequential
 * fashion. In the process, it fires chunkFound events when it detects chunks of
 * data of the specified size. This will allow the user to do some
 * pre-processing on the chunk before using it. A grand application may be with
 * a websocket server or client which cannot accept more than a fixed number of
 * bytes per payload. You may use objects of this class to break the data up
 * into simple chunks and transmit over the connection and then rebuild on the
 * other end. Since chunks are generated sequentially, there is no fear of data
 * corruption. Just re-couple the chunks as they come in. When it gets to the
 * end of the array, it fires the remaining chunk(which may not be up to the
 * specified {@link DataChunker#chunkSize}) at the end.
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
     * @param chunkSize The sizeRatio of each chunk. Each chunk generated is
     * guaranteed to have this sizeRatio, save for the final chunk, which will
     * have a sizeRatio equal to the remaining number of elements in the main
     * array.
     * @param blob The array whose data is to be broken into chunks
     */
    public DataChunker(int chunkSize, byte[] blob) {
        this.chunkSize = chunkSize;
        chunk(blob);
    }
    
      /**
     *
     * @param chunkSize The sizeRatio of each chunk. Each chunk generated is
     * guaranteed to have this sizeRatio, save for the final chunk, which will
     * have a sizeRatio equal to the remaining number of elements in the main
     * array.
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

    public boolean isValid() {
        return valid;
    }

    private void chunk(InputStream blob) {
        this.valid = false;

        try {

            //bytes read at that instant from the InputStream
            int readBytes;
            //sum of all bytes read from the InputStream 
            int totalBytesRead = 0;
            // int processedBytes = 0;//bytes fed to the chunkFound method.
            byte[] chunk = new byte[chunkSize];

            //Marks the current position in the chunk array where new data should be copied into.
            int cursor = 0;
            /**
             * Fresh data from the InputStream is always read into this array.
             * Only `readBytes` elements in it are valid data from the stream.
             */
            byte[] tempChunk = new byte[chunkSize];
            /**
             * Holds the size of bytes that have been read from the stream and
             * need to be processed in chunks.
             */
            int readBuffer = 0;

            while ((readBytes = blob.read(tempChunk, 0, chunkSize)) != -1) {

                totalBytesRead += readBytes;

                readBuffer += readBytes;

                if (readBuffer < chunkSize) {

                    System.arraycopy(tempChunk, 0, chunk, cursor, readBytes);

                    cursor = readBuffer;
                } else {

                    int remainder = readBuffer % chunkSize;
                    int sizeToProcess = readBuffer - remainder;

                    byte[] bigChunk = new byte[sizeToProcess];
                    System.arraycopy(chunk, 0, bigChunk, 0, cursor);//number of valid bytes in chunk array is equal to cursor.
                    System.arraycopy(tempChunk, 0, bigChunk, cursor, sizeToProcess - cursor);

                    chunk = new byte[chunkSize];

                    System.arraycopy(tempChunk, sizeToProcess - cursor, chunk, 0, remainder);

                    readBuffer = remainder;//Account for unread items.
                    cursor = remainder;
                    DataChunker chunkParent = this;

                    final int allBytesRead = totalBytesRead;
                    DataChunker chunker = new DataChunker(chunkSize, bigChunk) {
                        @Override
                        public void chunkFound(byte[] foundChunk, int bytesProcessed) {
                            chunkParent.chunkFound(foundChunk, allBytesRead);
                        }

                        @Override
                        public void chunksExhausted(int bytesProcessed) {

                        }
                    };

                    bigChunk = null;
                }

            }

            if (readBuffer > 0) {
                byte[] finalChunk = new byte[cursor];
                System.arraycopy(chunk, 0, finalChunk, 0, cursor);
                chunkFound(finalChunk, totalBytesRead);
            }
            chunksExhausted(totalBytesRead);
            this.valid = true;
        } catch (IOException ex) {
            this.valid = false;
        } finally {
            if (blob != null) {
                try {
                    blob.close();
                } catch (IOException ex) {
                }
            }
        }

    }

    private void chunk(byte[] blob) {
        this.valid = false;
        try {
            // 0-8191,8192-2(8192)-1,2(8192)-3(8192)-1

            int sentBytes = 0;
            int len = blob.length;

            int cursor = 0;
            byte[] chunk = new byte[chunkSize <= len ? chunkSize : len];
            for (int i = 0; i < len; i++) {

                chunk[cursor++] = blob[i];

                sentBytes++;
                if (cursor == chunk.length) {
                    reset:
                    {
                        chunkFound(chunk, sentBytes);
                        cursor = 0;
                        chunk = new byte[chunkSize <= len - sentBytes ? chunkSize : len - sentBytes];

                    }
                }

            }

            chunksExhausted(sentBytes);
            this.valid = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * Called when a chunked array.
     *
     * @param foundChunk The current chunk.
     * @param bytesProcessed The total number of bytes processed including the
     * current chunk.
     */
    public abstract void chunkFound(byte[] foundChunk, int bytesProcessed);

    /**
     * Fired when all chunks have been detected.
     *
     * @param bytesProcessed The total number of bytes processed.
     */
    public abstract void chunksExhausted(int bytesProcessed);

}
