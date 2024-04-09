package com.jones.tank.util;

import java.util.Random;

public class RandomString {
    public static final String SOURCES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    public static final String SOURCES_DIGIST = "1234567890";

    public static void main(String[] args) {
        System.out.println(generate(5));
    }

    public static String generate(int size) {
        return generateString(new Random(), "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", size);
    }

    public static String generateVerifyCode(){
        return generateString(new Random(), SOURCES_DIGIST, 6);
    }

    private static String generateString(Random random, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }
}

