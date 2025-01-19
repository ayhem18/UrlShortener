package org.utils;

import java.util.Random;

public class CustomGenerator {
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
            +"lmnopqrstuvwxyz!@#$%&";

    public String randomString(int length) {
        StringBuilder str = new StringBuilder();
        Random random = new Random();
        for (int i = 0 ; i < length; i++) {
            str.append(CHARS.charAt(random.nextInt(0, CHARS.length())));
        }

        return str.toString();
    }

    public int verify_power_26(long number) {
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
        if (order <= 0) {
            throw new RuntimeException("No negative numbers allowed");
        }

        if (order == 1) {
            return "a";
        }

        // the first step is to calculate the logarithm of n with respect to 26

        // for numbers that aren't a power of 26, using log_a(x) = log(x) / log(a) trick seems to work well enough
        // however for powers of 26, the same trick leads to bugs
        int n;
        int power26 = verify_power_26(order);

        if (power26 == -1) {
            n = (int) Math.floor(Math.log(order) / Math.log(26)) + 1;
        }
        else {
            n = power26 ;
        }

//        System.out.println("\norder: " +  order + "\n");
//        System.out.println("\n n " +  n + "\n");

        int asciiOfA = 'a';
        StringBuilder instanceId = new StringBuilder();

        long power;
        long quotient;

        for (int i = n - 1; i >= 0; i--) {
            power = (long) Math.pow(26, i);
            quotient = order / power;
            instanceId.append((char)(asciiOfA + quotient - 1));
            order = order - power * quotient;
        }

        return instanceId.toString();

    }
}
