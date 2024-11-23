package com.kurtcan.server.client;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class ClientState {
    private volatile UUID clientId = null;
    private volatile UUID roomId = null;

    public boolean isRoomJoined() {
        return Objects.nonNull(this.roomId);
    }

    public void leaveRoom() {
        this.roomId = null;
    }

    public void joinRoom(UUID roomId) {
        this.roomId = roomId;
    }

    public void connect(UUID clientId) {
        this.clientId = clientId;
    }
}
