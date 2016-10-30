package com.lakomy.tomasz.androidpingclient;

import android.util.Log;

import java.security.SecureRandom;
import java.util.Random;

public class RandomDataGenerator {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    public String generateRandomData(int length) {
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }

        return sb.toString();
    }
}
