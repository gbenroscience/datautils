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
 *
 * @author GBEMIRO JIBOYE <gbenroscience@gmail.com>
 */
public class Scanner {

    private String input;

    private String[] tokens;

    private boolean includeTokens;

    public Scanner(String input, boolean includeTokens, String... tokens) {
        this.input = input;
        this.tokens = tokens;
        this.includeTokens = includeTokens;

        //Sort in descending order of number of characters
        Arrays.sort(tokens, new Comparator<java.lang.String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });

    }

    public ArrayList<String> scan() {

        if (tokens.length == 0) {
            return new ArrayList<>();
        }

        int processedTokenIndex = 0;

        ArrayList<String> storage = scan(input, tokens[processedTokenIndex++]);

        int sz = storage.size();

        for (; processedTokenIndex < tokens.length; processedTokenIndex++) {

            //fetch the next token
            String token = tokens[processedTokenIndex];
            for (int i = 0; i < sz; i++) {
                String in = storage.get(i);

                if (!isProcessedToken(in, processedTokenIndex)) {//do not split positions in the original string that have tokens that have been split already

                    ArrayList<String> subStorage = scan(in, token);
                    int subSz = subStorage.size();

                    //If the token was able to split the substring, replace the substring with the split product 
                    if (subSz > 0) {
                        storage.remove(i);
                        storage.addAll(i, subStorage);

                        //re-calculate the list boundaries and index dynamically
                        sz += (subSz - 1);
                        i += (subSz - 1);
                    }

                }
            }
        }

        return storage;

    }

    private boolean isProcessedToken(String in, int index) {

        for (int i = 0; i < index; i++) {

            if (in.equals(tokens[i])) {
                return true;
            }

        }

        return false;
    }

    private ArrayList<String> scan(String in, String token) {

        ArrayList<String> storage = new ArrayList<>();
        int lastCutIndex = 0;

        int len = token.length();
        int strLen = in.length();

        for (int i = 0; i < strLen; i++) {

            if (i + len <= strLen) {
                if (in.substring(i, i + len).equals(token)) {
                    if (lastCutIndex != i) {//shorthand for "DO NOT ADD EMPTY STRINGS TO THE LEFT OF THE TOKEN"
                        storage.add(in.substring(lastCutIndex, i));
                    }
                    if (includeTokens) {
                        storage.add(token);
                    }
                    lastCutIndex = i + len;
                }
            }

        }

        //shorthand for "do not add empty strings at the end of this sub-parse"
        if (lastCutIndex < strLen) {
            storage.add(in.substring(lastCutIndex));
        }

        return storage;

    }

    static String generateInput(int iters) {
        StringBuilder builder = new StringBuilder();
        String function = "32sinh(3)*cosh(4)+cosh(2.25)-3*cos(sin(5))/(tan(4)*tanh(2))";
 

        int i = 0;
        while (i < iters) {
            // builder.append("DAWN").append(",DAY").append(",NOON").append(",EVENING").append(",DUSK").append(",NIGHT").append(",MIDNIGHT,");
            builder.append(function);
            i++;
        }

        return builder.toString();
    }

    private static void testOld(String in, boolean showOutput) {
        //"sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")"
        CustomScanner sc = new CustomScanner(in, true, "sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")");

        long start = System.nanoTime();
        List<String> scan = sc.scan();
        long duration = (System.nanoTime() - start) / 1000L;

        // System.out.println("Logs: \n\n"+sc.logs);
        System.out.println("Old method done in: " + duration + " microsecs.\n\n");

        if (showOutput) {
            System.out.println("Output: " + scan + "\n\nOld Tests End.");
        }
          System.out.println("Output size for old algorithm: " + scan.size() + " items\n\nNew Tests End.");

    }

    private static void testNew(String in, boolean showOutput) {
        //"sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")"
        Scanner sc = new Scanner(in, true, "sin", "cos", "sinh", "cosh", "tan", "tanh", "*", "+", "/", "-", "%", "(", ")");

        long start = System.nanoTime();
        ArrayList<String> scan = sc.scan();
        long duration = (System.nanoTime() - start) / 1000L;

        //  System.out.println("Logs: \n\n" + sc.logs);
        System.out.println("New method done in: " + duration + " microsecs\n\n");
        if (showOutput) {
            System.out.println("Output: " + scan + "\n\nNew Tests End.");
        }

          System.out.println("Output size for new algorithm: " + scan.size() + " items\n\nNew Tests End.");
    }

    private static void testNewAgain() {

        String in = "DAWN,DAY,NOON,EVENING,DUSK,NIGHT,MIDNIGHT";
        Scanner sc = new Scanner(in, true, ",D");

        long start = System.nanoTime();
        ArrayList<String> scan = sc.scan();
        long duration = (System.nanoTime() - start);

        //  System.out.println("Logs: \n\n" + sc.logs);
        System.out.println("New method done in: " + duration + " nanoseconds\n\n..." + scan);

    }

    public static void main(String[] args) {

        String text = generateInput(100);
        
        int len = text.length();
        
        System.out.println("Will process: "+len+" characters");

        boolean showIO = false;
        System.out.println("Input build done...\n");
        if (showIO) {
            System.out.println("\nInput is: " + text + "\n: Scan will begin soon.");
        }

       
           int i=0;     
      while(i++<10){  

        testOld(text, showIO);
        testNew(text, showIO);
      }

    }

}
