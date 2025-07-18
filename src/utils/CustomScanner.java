/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Objects of this class scans a string into tokens based on a list of tokenizer
 * values.
 *
 * @author GBEMIRO JIBOYE <gbenroscience@gmail.com>
 */
public class CustomScanner {

    private String input;
    private String[] tokens;
    /**
     * If true the tokens will be included in the output.
     */
    private boolean includeTokensInOutput;

    public CustomScanner(String input, boolean includeTokensInOutput, String... tokens) {
        this.input = input;
        this.includeTokensInOutput = includeTokensInOutput;
        this.tokens = tokens;
    }

    public CustomScanner(String input, boolean includeTokensInOutput, String[] moreTokens, String... tokens) {
        this.input = input;
        this.includeTokensInOutput = includeTokensInOutput;
        List<String> copier = new ArrayList<String>();
        copier.addAll(Arrays.asList(tokens));
        copier.addAll(Arrays.asList(moreTokens));

        this.tokens = copier.toArray(new String[]{});

    }

    /**
     * A convenience constructor used when there exists more than one array
     * containing the tokenizer data.
     *
     * @param input The input to scan.
     * @param includeTokensInOutput Will allow the splitting tokens to be added
     * to the final scan if this attribute is set to true.
     * @param splitterTokens An array of tokens on which the input is to be
     * split.
     * @param splitterTokens1 A second array of tokens on which the input is to
     * be split.
     * @param splitterTokens2 A second array of tokens..input as a variable
     * argument list... on which the input is to be split.
     *
     */
    public CustomScanner(String input, boolean includeTokensInOutput, String[] splitterTokens, String[] splitterTokens1, String... splitterTokens2) {
        this.input = input;

        List<String> copier = new ArrayList<String>();
        copier.addAll(Arrays.asList(splitterTokens));
        copier.addAll(Arrays.asList(splitterTokens1));
        copier.addAll(Arrays.asList(splitterTokens2));

        this.tokens = copier.toArray(new String[]{});
        this.includeTokensInOutput = includeTokensInOutput;
    }

    public List<String> scanOld() {

        String in = this.input;

        List<String> parse = new ArrayList<>();

        Arrays.sort(tokens, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

        for (int i = 0; i < in.length(); i++) {

            for (String token : tokens) {
                int len = token.length();

                if (len > 0 && i + len <= in.length()) {
                    String portion = in.substring(i, i + len);

                    if (portion.equals(token)) {
                        if (i != 0) {//avoid empty spaces
                            parse.add(in.substring(0, i));
                        }
                        if (includeTokensInOutput) {
                            parse.add(token);
                        }
                        in = in.substring(i + len);
                        i = -1;
                        break;
                    }

                }

            }

        }
        if (!in.isEmpty()) {
            parse.add(in);
        }

        return parse;
    }

    /**
     * USE THIS!!!
     * @return the scanned output
     */
    public List<String> scan() {

        String in = this.input;

        List<String> parse = new ArrayList<>();

        Arrays.sort(tokens, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

        int leftIndex = 0;

        int strLen = in.length();
        for (int i = 0; i < strLen; i++) {

            for (String token : tokens) {
                int len = token.length();

                if (len > 0 && i + len <= strLen) {//ignore empty tokens and do not read beyond the end of the input string
                    String portion = in.substring(i, i + len);
                    if (portion.equals(token)) {
                        if (leftIndex != i) {//escape from empty strings to the left of the token
                            String left = in.substring(leftIndex, i);
                            parse.add(left);
                        }

                        if (includeTokensInOutput) {
                            parse.add(token);
                        }
                        leftIndex = i + len;
                        i = leftIndex - 1;//skip the token and subtract 1 to allow the outer loop to increment i back to leftIndex
                        break;
                    }
                }

            }

        }

        if (leftIndex < strLen) {
            String left = in.substring(leftIndex);
            parse.add(left);

        }

        return parse;
    }

    private static void testNew(String in, boolean showOutput) {
        //"sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")"
        CustomScanner sc = new CustomScanner(in, true, "sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")");

        long start = System.currentTimeMillis();
        List<String> scan = sc.scan();
        long duration = (System.currentTimeMillis()- start);// / 1000000L;

        //  System.out.println("Logs: \n\n" + sc.logs);
        System.out.println("New method done in: " + duration + " ms\n\n");
        if (showOutput) {
            System.out.println("Output: " + scan + "\n\nNew Tests End.");
        }
        
            System.out.println("Output size for new algorithm: " + scan.size() + " items\n\nNew Tests End.");

    }
    
    
     private static void testOld(String in, boolean showOutput) {
        //"sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")"
        CustomScanner sc = new CustomScanner(in, true, "sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")");

        long start = System.currentTimeMillis();
        List<String> scan = sc.scanOld();
        long duration = (System.currentTimeMillis()- start);// / 1000000L;

        //  System.out.println("Logs: \n\n" + sc.logs);
        System.out.println("Old method done in: " + duration + " ms\n\n");
        if (showOutput) {
            System.out.println("Output: " + scan + "\n\nNew Tests End.");
        }
              System.out.println("Output size for old algorithm: " + scan.size() + " items\n\nNew Tests End.");

    }

    public static void main(String[] args) {

        String text = Scanner.generateInput(1000);

        boolean showIO = false;
        System.out.println("Input build done...\n");
        if (showIO) {
            System.out.println("\nInput has "+text.length()+" characters. It is: " + text + "\n: Scan will begin soon.");
        }
        
        
 System.out.println("\nInput has "+text.length()+" characters.");
        testOld(text, false);

        testNew(text, false);

    }

}
