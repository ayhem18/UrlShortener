package org.utils;

import java.util.Random;
import java.util.List;
import java.util.HashSet;

public class CustomGenerator {
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
            +"lmnopqrstuvwxyz!@#$%&";

    public String randomStringFromChars(int length, String characters) {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0 ; i < length; i++) {
            str.append(characters.charAt(random.nextInt(0, characters.length())));
        }

        return str.toString();
    }

    public String randomString(int length) {
        return randomStringFromChars(length, CHARS);
    }

    public String randomAlphaString(int length) {
        return randomStringFromChars(length, ALPHA);
    }


    private int verify_power_26(long number) {
        int log26 = 0;
        while (number > 0 && number % 26 == 0) {
            number /= 26;
            log26 += 1;
        }
        if (number == 1) {
            return log26;
        }
        return -1;
    }

    public String generateId(long order) {
        if (order == 0) {
            return "a";
        }

        // the id generation would basically be the decomposition of the integer into the base of 26

        // the first step is to calculate the logarithm of n with respect to 26
        // for numbers that aren't a power of 26, using log_a(x) = log(x) / log(a) trick seems to work well enough
        // however for powers of 26, the same trick leads to bugs

        int n;
        int power26 = verify_power_26(order);

        if (power26 == -1) {
            n = (int) Math.floor(Math.log(order) / Math.log(26));
        }
        else {
            n = power26;
        }

        int asciiOfA = 'a';
        StringBuilder instanceId = new StringBuilder();

        long power = (long) Math.pow(26, n);
        long quotient;

        while (power > 0) {
            quotient = order / power; // integer division
            instanceId.append((char)(asciiOfA + quotient));
            order = order - power * quotient;
            power = power / 26;
        }

        return instanceId.toString();
    }

    public long orderFromId(String id) {
        int n = id.length();
        long power = (long) (Math.pow(26, n - 1));
        long number = 0;

        int asciiA = 'a';
        for (int i = 0; i < n;i++) {
            number += power * (((int) id.charAt(i)) - asciiA);
            power = power / 26;
        }

        return number;
    }

    public String randomCaseString(String str) {
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < str.length();i++) {
            String c = Character.toString(str.charAt(i));
            if (Math.random() < 0.5) {
                c = c.toLowerCase();
            }
            else {
                c = c.toUpperCase();
            }
            newString.append(c);
        }
        return newString.toString();
    }

    /**
     * Generates a random string of specified length that excludes any characters in the excludeSet
     * @param excludeChars List of characters to exclude
     * @param length Length of the string to generate
     * @return Random string without any characters from excludeSet
     */
    public String randomStringExclude(List<Character> excludeChars, int length) {
        // Convert the list to a HashSet for O(1) lookups
        HashSet<Character> excludeSet = new HashSet<>(excludeChars);
        
        StringBuilder allowedChars = new StringBuilder();
        
        // Start with all characters in CHARS
        for (char c : CHARS.toCharArray()) {
            // Only include if not in excludeSet
            if (!excludeSet.contains(c)) {
                allowedChars.append(c);
            }
        }
        
        // If we excluded too many characters, throw an exception
        if (allowedChars.isEmpty()) {
            throw new IllegalArgumentException("excludeSet contains all available characters");
        }
        
        return randomStringFromChars(length, allowedChars.toString());
    }

}
