package com.kurtcan.shared.threading;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadingUtils {
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Thread interrupted while sleep: {}", e.getMessage());
        }
    }
}
