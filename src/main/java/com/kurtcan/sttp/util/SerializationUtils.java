package com.kurtcan.sttp.util;

public class SerializationUtils {
    public static boolean isNullOrEmpty(final String str) {
        return str == null || str.isEmpty() || str.isBlank() || str.equalsIgnoreCase("null");
    }
}
