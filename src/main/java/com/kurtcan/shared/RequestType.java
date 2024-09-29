package com.kurtcan.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@Getter
@ToString
@RequiredArgsConstructor
public enum RequestType {
    DISCONNECT("DISCONNECT"),
    ROOM_MESSAGE("ROOM_MESSAGE"),
    CREATE_ROOM("CREATE_ROOM"),
    JOIN_ROOM("JOIN_ROOM"),
    LEAVE_ROOM("LEAVE_ROOM");

    private final String name;

    public static Optional<RequestType> fromString(String text) {
        for (RequestType requestType : RequestType.values()) {
            if (requestType.name.equalsIgnoreCase(text)) {
                return Optional.of(requestType);
            }
        }
        return Optional.empty();
    }

    public boolean equals(String text) {
        return name.equalsIgnoreCase(text);
    }
}
