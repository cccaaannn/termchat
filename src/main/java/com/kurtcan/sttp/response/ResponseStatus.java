package com.kurtcan.sttp.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@Getter
@ToString
@RequiredArgsConstructor
public enum ResponseStatus {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE");

    private final String name;

    public static Optional<ResponseStatus> fromString(String text) {
        for (ResponseStatus responseStatus : ResponseStatus.values()) {
            if (responseStatus.name.equalsIgnoreCase(text)) {
                return Optional.of(responseStatus);
            }
        }
        return Optional.empty();
    }

    public boolean equals(String text) {
        return name.equalsIgnoreCase(text);
    }
}
