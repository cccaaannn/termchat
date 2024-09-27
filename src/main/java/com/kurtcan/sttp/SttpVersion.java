package com.kurtcan.sttp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@Getter
@ToString
@RequiredArgsConstructor
public enum SttpVersion {
    V1("V1");

    private final String name;

    public static Optional<SttpVersion> fromString(String text) {
        for (SttpVersion sttpVersion : SttpVersion.values()) {
            if (sttpVersion.name.equalsIgnoreCase(text)) {
                return Optional.of(sttpVersion);
            }
        }
        return Optional.empty();
    }

    public boolean equals(String text) {
        return name.equalsIgnoreCase(text);
    }
}
