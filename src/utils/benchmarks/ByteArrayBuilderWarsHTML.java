package utils.benchmarks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom; 
import java.util.Random;

public class ByteArrayBuilderWarsHTML {

    static class Result<C, V> {

        private C c;
        private V v;

        public Result(C c, V v) {
            this.c = c;
            this.v = v;
        }

    }

    public static void main(String[] args) throws IOException {
        for(int i=0;i<3;i++){
            collate(10000, (int) Math.pow(10, i+2));
        }
    } 
    
    
    
    
    
    

    private static void collate(int dataArraySize, int numOfArrays) {
  

        double avg4Builder = 0;
        double avg4Buffer = 0;

        double avg4Builder1 = 0;
        double avg4Buffer1 = 0;

        int nAverages = 5;
        int n = 10;

        StringBuilder table1 = new StringBuilder("""
                                                 <table>
                                                 <caption style=\"font-weight: bold;\">RESULTS for append ops(ms)</caption>
                                                 <tbody>\n
                                                   <tr>
                                                       <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
                                                   </tr>
                                                 """);
        StringBuilder table2 = new StringBuilder("""
                                                 <table>
                                                 <caption style=\"font-weight: bold;\">RESULTS for bytes-reader ops(ms)</caption>
                                                 <tbody>
                                                   <tr>
                                                      <th>arraySize</th><th>numArrays</th><th>ByteArrayBuilder(ms)</th><th>java.nio.ByteBuffer(ms)</th>
                                                    </tr>
                                                 """);

        int j = 0;
        while (j++ < nAverages) {
            for (int i = 0; i < n; i++) {
                Result<ByteArrayBuilder, Double> builder = ByteArrayBuilderWarsHTML.benchmarkAppendForByteArrayBuilder(dataArraySize, numOfArrays);
                Result<ByteBuffer, Double> buffer = ByteArrayBuilderWarsHTML.benchmarkAppendForByteBuffer(dataArraySize, numOfArrays);

                double res1 = ByteArrayBuilderWarsHTML.benchmarkReadBytes(builder.c);
                double res2 = ByteArrayBuilderWarsHTML.benchmarkReadBytes(buffer.c);

                avg4Builder += builder.v;
                avg4Buffer += buffer.v;

                avg4Builder1 += res1;
                avg4Buffer1 += res2;

            }

            StringBuilder row = new StringBuilder("<tr>");
            row.append("<td>").append(dataArraySize).append("</td>").append("<td>").append(numOfArrays).append("</td>");
            row.append("<td>").append(Utils.round(avg4Builder / n, 3)).append("</td>").append("<td>").append(Utils.round(avg4Buffer / n, 3)).append("</td>");
            row.append("</tr>\n");

            StringBuilder row1 = new StringBuilder("<tr>");
            
            row1.append("<td>").append(dataArraySize).append("</td>").append("<td>").append(numOfArrays).append("</td>");
            row1.append("<td>").append(Utils.round(avg4Builder1/n, 3)).append("</td>").append("<td>").append(Utils.round(avg4Buffer1/n, 3)).append("</td>");
            row1.append("</tr>\n");

            table1.append(row);
            table2.append(row1);

        }

        table1.append("</tbody>\n</table>\n");
        table2.append("</tbody>\n</table>\n");

        System.out.println("table1: \n" + table1);
        System.out.println("table2: \n" + table2);
    }

    private static Result<ByteBuffer, Double> benchmarkAppendForByteBuffer(int dataArraySize, int numberOfArrays) {
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

//        System.out.printf("ByteBuffer#put() time for " + numberOfArrays + " byte arrays of size " + dataArraySize + "(" + (dataArraySize * numberOfArrays / 1000000) + "MB): %.2f ms%n", appendTime / 1_000_000.0);
//      
        return new Result<>(buffer, appendTime / 1_000_000.0);
    }

    private static Result<ByteArrayBuilder, Double> benchmarkAppendForByteArrayBuilder(int dataArraySize, int numberOfArrays) {
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
//        System.out.printf("ByteArrayBuilder#append() time for " + numberOfArrays + " byte arrays of size " + dataArraySize + "(" + (dataArraySize * numberOfArrays / 1000000) + "MB): %.2f ms%n", appendTime / 1_000_000.0);

        
        return new Result<>(builder1, appendTime / 1_000_000.0);
    }

    private static double benchmarkReadBytes(ByteArrayBuilder builder) {
        long start = System.nanoTime();
        byte[] builder1Bytes = builder.getBytes();
        long appendTime = System.nanoTime() - start;
       // System.out.printf("ByteArrayBuilder#getBytes() time to read (" + (builder.length() / 1000000) + "MB) byte array: %.2f ms%n", appendTime / 1_000_000.0);

         return appendTime / 1_000_000.0;
    }

    private static double benchmarkReadBytes(ByteBuffer buffer) {
        long start = System.nanoTime();
        buffer.flip(); // prepare for reading

        byte[] completePayload = new byte[buffer.remaining()];
        buffer.get(completePayload);
        long appendTime = System.nanoTime() - start;
        //System.out.printf("ByteBuffer#-read-bytes time to read (" + (completePayload.length / 1000000) + "MB) byte array: %.2f ms%n", appendTime / 1_000_000.0);
 
        return appendTime / 1_000_000.0;
    }

}
