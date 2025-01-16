package com.url_shortener;

import org.springframework.context.annotation.Configuration;
import java.util.Random;


@Configuration
public class CustomGenerator {
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
            +"lmnopqrstuvwxyz!@#$%&";

    public String randomString(int length) {
        StringBuilder str = new StringBuilder();
        int randomCharIndex;
        Random random = new Random();
        for (int i = 0 ; i < length; i++) {
            str.append(CHARS.charAt(random.nextInt(0, CHARS.length())));
        }

        return str.toString();
    }


    public String generateId(long order) {

        StringBuilder instanceId = new StringBuilder();

        while (order >= 26) {
            instanceId.append("z");
            order = order / 26;
        }

        int asciiOfA = 'a';
        instanceId.append((char)(asciiOfA + order));
        return instanceId.toString();

    }
}
