package com.lakomy.tomasz.androidpingclient;

import java.util.Random;

public class RandomDataGenerator {
    StringBuilder stringBuilder;
    Random random = new Random();
    char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public String generateRandomData(int length) {
        stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }
}
