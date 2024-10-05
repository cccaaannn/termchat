package com.kurtcan.client.response;

import com.kurtcan.client.ClientState;
import com.kurtcan.client.util.ClientPrinter;
import com.kurtcan.shared.dto.response.CreateRoomResponse;
import com.kurtcan.shared.dto.response.JoinRoomResponse;
import com.kurtcan.shared.dto.response.LeaveRoomResponse;
import com.kurtcan.shared.dto.response.RoomMessageResponse;
import com.kurtcan.shared.serialization.Serializer;

public class RoomHandler {
    public void createRoom(String content) {
        var roomResponse = Serializer.deserialize(content, CreateRoomResponse.class);
        var roomId = roomResponse.getRoomId();
        ClientState.ACTIVE_ROOM_ID = roomId;

        ClientPrinter.print("Room created with id: {}", roomId);
    }

    public void joinRoomSelf(String content) {
        var roomResponse = Serializer.deserialize(content, JoinRoomResponse.class);
        var roomId = roomResponse.getRoomId();
        var clientId = roomResponse.getClientId();
        var username = roomResponse.getUsername();

        if (clientId.equals(ClientState.CLIENT_ID)) {
            ClientState.ACTIVE_ROOM_ID = roomId;
            ClientPrinter.print("Joined to: {}", roomId);
        }

        ClientPrinter.print("User joined: {}", username);
    }

    public void leaveRoom(String content) {
        var roomResponse = Serializer.deserialize(content, LeaveRoomResponse.class);
        var roomId = roomResponse.getRoomId();
        var clientId = roomResponse.getClientId();
        var username = roomResponse.getUsername();

        if (clientId.equals(ClientState.CLIENT_ID)) {
            ClientState.ACTIVE_ROOM_ID = null;
            ClientPrinter.print("Room left: {}", roomId);
        }

        ClientPrinter.print("User left: {}", username);
    }

    public void messageRoom(String content) {
        var roomMessage = Serializer.deserialize(content, RoomMessageResponse.class);
        var clientId = roomMessage.getClientId();
        var username = roomMessage.getUsername();
        var message = roomMessage.getMessage();

        if (!clientId.equals(ClientState.CLIENT_ID)) {
            ClientPrinter.print("{}:{}", username, message);
        }
    }
}
