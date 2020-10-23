package com.vadimfedchuk1994gmail.radio.utils;

import java.nio.charset.StandardCharsets;

public class StringFormatter {

    public StringFormatter(){

    }

    // convert UTF-8 to internal Java String format
    public static String convertUTF8ToString(String s) {
        String out = null;
        out = new String(s.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        return out;
    }

    // convert internal Java String format to UTF-8
    public static String convertStringToUTF8(String s) {
        String out = null;
        out = new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        return out;
    }
}
