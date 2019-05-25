# datautils
Utilities for processing or transmitting data

This project has a simple target: help users serve data in consistent chunks of equal size till the whole data has been served.

This applies to any kind of data, whether data in an array or an InputStream or a File.

It also generates events for every chunk of the pre-specified size which it generates.

If you wish to push data at a controlled rate through a connection to an endpoint and the designer of that API
has specified that the endpoint will not be allowed to receive more than a given amount of data at once.

OR

If you wish to periodically perform a task when a specified chunk of data has been processed(copied or transmitted etc.)

Then ```DataChunker``` is definitely the thing for you!

## A simple example


```java
DataChunker chunker = new DataChunker(8192, blob) {
@Override 
public void chunkFound(byte[] foundChunk, int bytesProcessed) {

}
@Override 
public void chunksExhausted(int bytesProcessed) { 

} 
};
```

The first value passed to the ```DataChunker``` constructor is the chunk-size in bytes. In this case, the chunk-size is 8192 bytes(8 kilobytes). This means that the chunker object is going to process any source data in 8192 byte chunks. 

The second argument; i.e ```blob``` , is a File, an InputStream, or a java byte array. The blob is the source data to be processed in chunks.

If you need to do anything with the generated chunks, The ```chunkFound``` method is there to detect each chunk. You can then specify what to do with the chunks within the body of the method.

For instance in one of my tomcat web applications, I set the maximum length of incoming binary messages that a websocket Session can buffer to about 8kilobytes.

Then I used ```DataChunker``` to serve up any file, image or text in 8192 byte chunks to the sessions.


This automatically took away packet size errors that tend to occur when large messages are sent via websocket sessions in tomcat.


Text blobs can also be served up in chunks in 2 ways depending on the programmer's choice.

1. In byte array chunks as usual.
Just use the constructor which takes the chunk-size and the text blob. And process the text chunks in the ```chunkFound``` method as usual

2. In text(string) chunks using the StringChunker class. 

## StringChunker.java

Here is a simple example which starts with a block of text and serves it up as chunks of text and merges them all together at the end.

```java

String blob = "Experience is wasted if history does not repeat itself...Gbemiro Jiboye";

 final StringBuilder builder = new StringBuilder();
        StringChunker chunker = new StringChunker(4, blob) {
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
```









