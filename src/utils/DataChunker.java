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
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
 
/**
 * Loops through a byte array or InputStream or ByteBuffer , File or String and produces chunks of it in sequential
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
     * @param chunkSize The sizeRatio of each chunk. Each chunk generated is
     * guaranteed to have this sizeRatio, save for the final chunk, which will
     * have a sizeRatio equal to the remaining number of elements in the main
     * array.
     *
     * You may check the {@link DataChunker#isValid() } method to be sure that
     * no error occurred during chunking.
     * @param blob A block of text to be broken into chunks. The chunks of text
     * are produced as byte array chunks. If you need them to be produced as
     * text chunks(substrings), then look at
     */
    public DataChunker(int chunkSize, String blob) {
        this.chunkSize = chunkSize;
        chunk(blob);

    }

    static void checkBounds(int off, int len, int size) { // package-private
        if ((off | len | (off + len) | (size - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
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
                    final DataChunker chunkParent = this;

                    final int allBytesRead = totalBytesRead;
                    DataChunker chunker = new DataChunker(chunkSize, bigChunk) {
                        @Override
                        public void chunkFound(byte[] foundChunk, long bytesProcessed) {
                            chunkParent.chunkFound(foundChunk, allBytesRead);
                        }

                        @Override
                        public void chunksExhausted(long bytesProcessed) {

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

    /**
     * Upgraded chunk method for delivering byte arrays in chunks.
     * 1. Uses System.arraycopy to copy data.
     * 2. Does not do byte by byte copy in Java again, instead does it using 1. above.
     * 3. Does not create new chunking array instances except for the last chunk, if the size of the last
     * chunk is less than the chunk size. So it creates one array for the chunks and reuses it
     * throughout the whole copying process.
     * Speed gains will be high.
     * @param blob The array to copy.
     */
    private void chunk(byte[] blob) {
        this.valid = false;
        try {
            // 0-8191,8192-2(8192)-1,2(8192)-3(8192)-1

            int sentBytes = 0;
            int len = blob.length;

            int cursor = 0;
            byte[] chunk = new byte[chunkSize <= len ? chunkSize : len];

            while(sentBytes < len){

                long remainingBytes = len - sentBytes;
                if(remainingBytes >= chunkSize){
                    System.arraycopy(blob , sentBytes , chunk , 0 ,chunkSize);
                    sentBytes += chunkSize;
                    chunkFound(chunk, sentBytes);
                }else{
                    chunk = new byte[(int) remainingBytes];
                    System.arraycopy(blob , sentBytes , chunk , 0 , (int) remainingBytes);
                    sentBytes = len;
                    chunkFound(chunk, sentBytes);
                }
            }
            chunksExhausted(sentBytes);
           
            /*
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
            */
            this.valid = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private void chunk(ByteBuffer buffer){
        this.valid = false;
        int len = buffer.remaining();

        int cursor = 0;
        int sentBytes = 0;
        byte[] chunk = new byte[chunkSize <= len ? chunkSize : len];
        for(int i = 0; i < len; i++){

            chunk[cursor++] = buffer.get(i);
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
    }

    /**
     *
     * @param blob The text to be processed in bytes.
     */
    private void chunk(String blob) {
        this.valid = false;
        int len = blob.length();
        int bytesSent = 0;
        try {
            for (int i = 0; i < len; i += chunkSize) {

                String chunk;
                if (i + chunkSize < len) {
                    chunk = blob.substring(i, i + chunkSize);
                } else {
                    chunk = blob.substring(i);
                }

                byte[] bytes = chunk.getBytes("UTF-8");
                chunkFound(bytes, bytesSent += bytes.length);

            }
            chunksExhausted(len);
            this.valid = true;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DataChunker.class.getName()).log(Level.SEVERE, null, ex);
        }
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
          byte[] seed = new byte[150];
        Random r = new Random(System.nanoTime());
        r.nextBytes(seed);
        SecureRandom rnd = new SecureRandom(seed);
        int i = 0;
      while( i < 5){  
        byte[] b = new byte[20000];
        rnd.nextBytes(b);
         
        long start = System.nanoTime();
        DataChunker chunker = new DataChunker(199, b) {
            @Override
            public void chunkFound(byte[] foundChunk, long bytesProcessed) {
              //  System.out.println("...Now processed "+bytesProcessed+" bytes in "+foundChunk.length+" byte chunks"); 
            }
            
            @Override
            public void chunksExhausted(long bytesProcessed) {
                  // System.out.println("All "+bytesProcessed+" bytes processed; Thanks");
            }
        };
        double dur = (System.nanoTime() - start)/1.0E6;
        
        System.out.println("At i = "+(++i)+" Duration = "+dur+" ms.");
      }
        
        
        
         
    }

}
