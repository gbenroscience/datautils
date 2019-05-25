/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;
 
/**
 *
 * @author JIBOYE, Oluwagbemiro Olaoluwa <gbenroscience@yahoo.com>
 */
public abstract class StringChunker {
     /**
     * The size with which you want to process the stream or byte array.
     */
    private int chunkSize;

    private boolean valid;

    public StringChunker(int chunkSize, String blob) {
        this.chunkSize = chunkSize;
        this.valid = valid;
        chunk(blob);
    } 

    public boolean isValid() {
        return valid;
    }
     
  /**
   * 
   * @param blob The text to be processed in chunks of text.
   */
    private void chunk(String blob) {
        this.valid = false;
        int len = blob.length();
        int charsSent = 0;
    
            for (int i = 0; i < len; i += chunkSize) {

                String chunk;
                if (i + chunkSize < len) {
                    chunk = blob.substring(i, i + chunkSize);
                } else {
                    chunk = blob.substring(i);
                }
                
                chunkFound(chunk, charsSent += chunk.length());

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
    public abstract void chunkFound(String foundChunk, int bytesProcessed);

     /**
     * Fired when all chunks have been detected.
     *
     * @param bytesProcessed The total number of bytes processed.
     */
    public abstract void chunksExhausted(int bytesProcessed);
    
    public static void main(String[] args) {
        final StringBuilder builder = new StringBuilder();
        StringChunker chunker = new StringChunker(1234, "The LORD God has said that: JIBOYE Oluwagbemiro Olaoluwa is the Programmer of Africa. Amen.") {
            @Override
            public void chunkFound(String foundChunk, int bytesProcessed) {
                builder.append(foundChunk);
                System.out.println("Found: "+foundChunk+", bytesProcessed: "+bytesProcessed+" bytes");
            }
            
            @Override
            public void chunksExhausted(int bytesProcessed) {
                System.out.println("Processed all of: "+bytesProcessed+" bytes. Rebuilt string is: "+builder.toString());
            }
        };
    }
    
    

}
