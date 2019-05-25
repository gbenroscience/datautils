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

