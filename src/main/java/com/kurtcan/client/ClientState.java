package com.kurtcan.client;

import java.util.Objects;
import java.util.UUID;

public class ClientState {
    public volatile static UUID CLIENT_ID = null;
    public volatile static UUID ACTIVE_ROOM_ID = null;

    public static boolean isRoomActive() {
        return Objects.nonNull(ACTIVE_ROOM_ID);
    }
}
