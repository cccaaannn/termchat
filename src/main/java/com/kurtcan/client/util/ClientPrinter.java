package com.kurtcan.client.util;

import org.slf4j.helpers.MessageFormatter;

public class ClientPrinter {
    public static void print(String message) {
        System.out.println(message);
    }

    public static void print(String message, Object... args) {
        System.out.println(MessageFormatter.arrayFormat(message, args).getMessage());
    }
}
