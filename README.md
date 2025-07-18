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

## ByteArrayBuilder

`ByteArrayBuilder` is a class that simplifies the core operation of `ByteBuffer` in Java. It allows easy collocation of byte arrays and reading of a portion or the full merge of the collocated arrays.
This is useful in streaming applications, file uploads etc.

Beyond simple collocation(merging of byte arrays), it allows a lot of manipulation also: e.g insertion of byte arrays in certain positions of the collocated array.
Its operation is surprisingly fast, its `append(byte[])` operations compare favorably and at times slightly outperform `java.nio.ByteBuffer.put` and at at all times, its `getBytes()` operations is orders of magnitude times
faster than `java.nio.ByteBuffer.get(byte[])`

### Usage
#### append
To collocate byte arrays, use the `append(byte[], false)` method to continually add them till you receive the last byte array, which you must receive with  `append(byte[], true)`.
When you set the second argument to true, the `ByteArrayBuilder` runs some reconciliation operations to build all the arrays it has received into a single contiguous or continuous unit.
When it is set to false, the byte arrays are added at virtually no cost; the cost being that of adding data to an `ArrayList` and updating a simple `AtomicInteger` via its `addAndGet` method.

Here is how to use the `ByteArrayBuilder.append(byte[], last)`.
```Java
   ByteArrayBuilder builder1 = new ByteArrayBuilder();

        byte[] seed = new byte[1000];
        Random rnd = new Random(System.currentTimeMillis());
        rnd.nextBytes(seed);
        SecureRandom secRnd = new SecureRandom(seed);

  for (int i = 0; i < numberOfArrays; i++) {
            byte[] data = new byte[dataArraySize];
            secRnd.nextBytes(data);
            builder1.append(data, i == (numberOfArrays - 1));
        }
```

To read out the data as a single byte array, do:

```Java
        long start = System.nanoTime();
        byte[] builder1Bytes = builder.getBytes();
```

There are benchmarks included for this `ByteArrayBuilder` vs `java.nio.ByteBuffer` in `utils.benchmarks.ByteArrayBuilderWars` and `utils.benchmarks.ByteArrayBuilderWarsHTML`. The `utils.benchmarks.ByteArrayBuilderWarsHTML` presents the benchmarks as
an html table, so include it in `html`  and `body` tags and view the results.


Here are sample results:

<b>Results for `ByteArrayBuilder.append(byte[])` vs `java.nio.ByteBuffer.put(byte[])` in `ms`</b>
<table>
<caption style="font-weight: bold;">RESULTS</caption>
<tbody>
  <tr>
      <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
  </tr>
<tr><td>10000</td><td>100</td><td>26.768</td><td>17.873</td></tr>
<tr><td>10000</td><td>100</td><td>16.603</td><td>16.675</td></tr>
<tr><td>10000</td><td>100</td><td>14.322</td><td>12.785</td></tr>
<tr><td>10000</td><td>100</td><td>12.853</td><td>12.43</td></tr>
<tr><td>10000</td><td>100</td><td>13.246</td><td>13.609</td></tr>
</tbody>
</table>

 <b>Results for `ByteArrayBuilder.getBytes()` vs `java.nio.ByteBuffer.get(byte[])` in `ms`</b>
<table>
<caption style="font-weight: bold;">RESULTS</caption>
<tbody>
  <tr>
     <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
   </tr>
<tr><td>10000</td><td>100</td><td>0.006</td><td>0.615</td></tr>
<tr><td>10000</td><td>100</td><td>0.014</td><td>0.918</td></tr>
<tr><td>10000</td><td>100</td><td>0.007</td><td>0.646</td></tr>
<tr><td>10000</td><td>100</td><td>0.006</td><td>0.167</td></tr>
<tr><td>10000</td><td>100</td><td>0.006</td><td>0.25</td></tr>
</tbody>
</table>

 <b>Results for `ByteArrayBuilder.append(byte[])` vs `java.nio.ByteBuffer.put(byte[])` in `ms`</b>
<table>
<caption style="font-weight: bold;">RESULTS</caption>
<tbody>
  <tr>
      <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
  </tr>
<tr><td>10000</td><td>1000</td><td>138.45</td><td>130.052</td></tr>
<tr><td>10000</td><td>1000</td><td>122.226</td><td>118.364</td></tr>
<tr><td>10000</td><td>1000</td><td>107.506</td><td>104.455</td></tr>
<tr><td>10000</td><td>1000</td><td>110.469</td><td>112.331</td></tr>
<tr><td>10000</td><td>1000</td><td>110.11</td><td>108.182</td></tr>
</tbody>
</table>

  <b>Results for `ByteArrayBuilder.getBytes()` vs `java.nio.ByteBuffer.get(byte[])` in `ms`</b>
<table>
<caption style="font-weight: bold;">RESULTS for bytes-reader ops(ms)</caption>
<tbody>
  <tr>
     <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
   </tr>
<tr><td>10000</td><td>1000</td><td>0.008</td><td>5.364</td></tr>
<tr><td>10000</td><td>1000</td><td>0.005</td><td>2.88</td></tr>
<tr><td>10000</td><td>1000</td><td>0.006</td><td>2.834</td></tr>
<tr><td>10000</td><td>1000</td><td>0.007</td><td>3.194</td></tr>
<tr><td>10000</td><td>1000</td><td>0.008</td><td>2.53</td></tr>
</tbody>
</table>

<b>Results for `ByteArrayBuilder.append(byte[])` vs `java.nio.ByteBuffer.put(byte[])` in `ms`</b> 
<table>
<caption style="font-weight: bold;">RESULTS for append ops(ms)</caption>
<tbody>
  <tr>
      <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
  </tr>
<tr><td>10000</td><td>10000</td><td>1272.281</td><td>1155.673</td></tr>
<tr><td>10000</td><td>10000</td><td>1146.638</td><td>1092.822</td></tr>
<tr><td>10000</td><td>10000</td><td>1112.499</td><td>1078.252</td></tr>
<tr><td>10000</td><td>10000</td><td>1132.246</td><td>1126.832</td></tr>
<tr><td>10000</td><td>10000</td><td>1119.094</td><td>1152.625</td></tr>
</tbody>
</table>

  <b>Results for `ByteArrayBuilder.getBytes()` vs `java.nio.ByteBuffer.get(byte[])` in `ms`</b>
<table>
<caption style="font-weight: bold;">RESULTS</caption>
<tbody>
  <tr>
     <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
   </tr>
<tr><td>10000</td><td>10000</td><td>0.005</td><td>29.317</td></tr>
<tr><td>10000</td><td>10000</td><td>0.007</td><td>45.433</td></tr>
<tr><td>10000</td><td>10000</td><td>0.004</td><td>29.521</td></tr>
<tr><td>10000</td><td>10000</td><td>0.004</td><td>37.145</td></tr>
<tr><td>10000</td><td>10000</td><td>0.005</td><td>35.544</td></tr>
</tbody>
</table>



