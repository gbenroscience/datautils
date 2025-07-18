package utils.benchmarks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;
import utils.ByteArrayBuilder;

public class ByteArrayBuilderWars {

    public static void main(String[] args) throws IOException {

        int dataArraySize = 10000;
        int numberOfArrays = 100000;
        
        for (int i = 0; i < 10; i++) {
            ByteArrayBuilder builder = ByteArrayBuilderWars.benchmarkAppendForByteArrayBuilder(dataArraySize, numberOfArrays);
            ByteBuffer buffer = ByteArrayBuilderWars.benchmarkAppendForByteBuffer(dataArraySize, numberOfArrays);

            ByteArrayBuilderWars.benchmarkReadBytes(builder);
            ByteArrayBuilderWars.benchmarkReadBytes(buffer);
        }

    }

    private static ByteBuffer benchmarkAppendForByteBuffer(int dataArraySize, int numberOfArrays) {
        ByteBuffer buffer = ByteBuffer.allocate(dataArraySize * numberOfArrays);

        byte[] seed = new byte[1000];
        Random rnd1 = new Random(System.currentTimeMillis());
        rnd1.nextBytes(seed);
        SecureRandom secRnd1 = new SecureRandom(seed);

        // Benchmark ByteBuffer#append()
        long start = System.nanoTime();
        for (int i = 0; i < numberOfArrays; i++) {
            byte[] data = new byte[dataArraySize];
            secRnd1.nextBytes(data);
            buffer.put(data);
        }
        long appendTime = System.nanoTime() - start;

        System.out.printf("ByteBuffer#put() time for " + numberOfArrays + " byte arrays of size " + dataArraySize + "(" + (dataArraySize * numberOfArrays / 1000000) + "MB): %.2f ms%n", appendTime / 1_000_000.0);

        return buffer;
    }

    private static ByteArrayBuilder benchmarkAppendForByteArrayBuilder(int dataArraySize, int numberOfArrays) {
        ByteArrayBuilder builder1 = new ByteArrayBuilder();

        byte[] seed = new byte[1000];
        Random rnd = new Random(System.currentTimeMillis());
        rnd.nextBytes(seed);
        SecureRandom secRnd = new SecureRandom(seed);

        // Benchmark ByteArrayBuilder#append()
        long start = System.nanoTime();
        for (int i = 0; i < numberOfArrays; i++) {
            byte[] data = new byte[dataArraySize];
            secRnd.nextBytes(data);
            builder1.append(data, i == (numberOfArrays - 1));
        }
        long appendTime = System.nanoTime() - start;
        System.out.printf("ByteArrayBuilder#append() time for " + numberOfArrays + " byte arrays of size " + dataArraySize + "(" + (dataArraySize * numberOfArrays / 1000000) + "MB): %.2f ms%n", appendTime / 1_000_000.0);
        return builder1;
    }

    private static void benchmarkReadBytes(ByteArrayBuilder builder) {
        long start = System.nanoTime();
        byte[] builder1Bytes = builder.getBytes();
        long appendTime = System.nanoTime() - start;
        System.out.printf("ByteArrayBuilder#getBytes() time to read (" + (builder.length() / 1000000) + "MB) byte array: %.2f ms%n", appendTime / 1_000_000.0);
    }

    private static void benchmarkReadBytes(ByteBuffer buffer) {
        long start = System.nanoTime();
        buffer.flip(); // prepare for reading

        byte[] completePayload = new byte[buffer.remaining()];
        buffer.get(completePayload);
        long appendTime = System.nanoTime() - start;
        System.out.printf("ByteBuffer#-read-bytes time to read (" + (completePayload.length / 1000000) + "MB) byte array: %.2f ms%n", appendTime / 1_000_000.0);
    }

}
