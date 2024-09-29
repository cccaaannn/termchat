package com.kurtcan.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@Getter
@ToString
@RequiredArgsConstructor
public enum ResponseType {
    CONNECT("CONNECT"),
    ROOM_CREATE("ROOM_CREATE"),
    ROOM_JOIN("ROOM_JOIN"),
    ROOM_LEAVE("ROOM_LEAVE"),
    ROOM_MESSAGE("ROOM_MESSAGE");

    private final String name;

    public static Optional<ResponseType> fromString(String text) {
        for (ResponseType responseType : ResponseType.values()) {
            if (responseType.name.equalsIgnoreCase(text)) {
                return Optional.of(responseType);
            }
        }
        return Optional.empty();
    }

    public boolean equals(String text) {
        return name.equalsIgnoreCase(text);
    }
}
