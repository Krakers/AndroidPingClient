package com.lakomy.tomasz.androidpingclient;

import android.util.Log;

import java.security.SecureRandom;

public class RandomDataGenerator {
    public String generateRandomData(int length) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        Log.d("aping", "randomData: " + sb.toString());

        return sb.toString();
    }
}
