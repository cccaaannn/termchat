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
    CREATE_ROOM("CREATE_ROOM"),
    JOIN_ROOM("JOIN_ROOM"),
    LEAVE_ROOM("LEAVE_ROOM"),
    MESSAGE_ROOM("MESSAGE_ROOM");

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
