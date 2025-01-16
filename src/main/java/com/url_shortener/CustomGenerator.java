package com.url_shortener;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.parameters.P;

import java.util.Random;


@Configuration
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


    public String generateId(long order) {
        if (order == 0) {
            return "a";
        }

        // since java does not offer a direct function to calculate the log with a random base: need to improvise
        int n = (int) Math.floor(Math.log(order) / Math.log(26));
        long power = (long) Math.pow(26, n);

        int asciiOfA = 'a';
        StringBuilder instanceId = new StringBuilder();

        while (order > 0) {
            long quotient = order / power;
            order = order - power * quotient;
            power = power / 26;
            instanceId.append((char)(asciiOfA + quotient));
        }

        return instanceId.toString();

    }
}
