package com.kurtcan.client;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

public class ClientState {
    @Getter
    private volatile static UUID clientId = null;
    @Getter
    private volatile static UUID roomId = null;

    public static boolean isMyClient(UUID clientId) {
        return Objects.equals(ClientState.clientId, clientId);
    }

    public static boolean isRoomJoined() {
        return Objects.nonNull(roomId);
    }

    public static void leaveRoom() {
        ClientState.roomId = null;
    }

    public static void joinRoom(UUID roomId) {
        ClientState.roomId = roomId;
    }

    public static void connect(UUID clientId) {
        ClientState.clientId = clientId;
    }
}
