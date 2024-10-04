package com.kurtcan.server.room;

import com.kurtcan.server.ServerState;
import com.kurtcan.server.client.ClientResponse;
import com.kurtcan.server.client.ClientState;
import com.kurtcan.shared.ResponseType;
import com.kurtcan.shared.dto.request.CreateRoomRequest;
import com.kurtcan.shared.dto.request.JoinRoomRequest;
import com.kurtcan.shared.dto.request.RoomMessageRequest;
import com.kurtcan.shared.dto.response.*;
import com.kurtcan.shared.serialization.Serializer;
import com.kurtcan.sttp.request.Request;
import com.kurtcan.sttp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final ClientState clientState;

    private void queueClientResponse(Response response) {
        var clientResponse = ClientResponse.builder()
                .response(response)
                .receiverId(clientState.CLIENT_ID)
                .build();

        ServerState.queueClientResponse(clientResponse);
    }

    private void queueRoomResponse(Response response) {
        var roomResponse = RoomResponse.builder()
                .roomId(clientState.ROOM_ID)
                .response(response)
                .build();

        ServerState.queueRoomResponse(roomResponse);
    }

    public void leaveRoomWithCleanUpAndNotify() {
        if (Objects.isNull(clientState.ROOM_ID)) {
            log.debug("Client not in a room: {}", clientState.CLIENT_ID);
            return;
        }

        var room = ServerState.getRoom(clientState.ROOM_ID);
        var roomMember = room.getMember(clientState.CLIENT_ID);
        if (roomMember.isEmpty()) {
            log.debug("User not found in room: {}", clientState.CLIENT_ID);
            return;
        }

        // Leave room
        clientState.ROOM_ID = null;
        room.removeMember(clientState.CLIENT_ID);
        // Remove room if empty
        if (room.getMembers().isEmpty()) {
            ServerState.removeRoom(room.getId());
            return;
        }

        // Notify other members in the room, if room is not empty
        var response = Response.success(
                ResponseType.LEAVE_ROOM.getName(),
                Serializer.serialize(new LeaveRoomResponse(room.getId(), clientState.CLIENT_ID, roomMember.get().getUsername()))
        );

        queueRoomResponse(response);
    }

    public void createRoom(Request request) {
        log.debug("Creating room, client: {}, request: {}", clientState.CLIENT_ID, request);

        if (request.getContent().isEmpty()) {
            log.debug("Invalid request: {}. Skipping", request);
            return;
        }

        if (Objects.nonNull(clientState.ROOM_ID)) {
            // TODO: Send error response
            log.debug("Client already in a room: {}", clientState.ROOM_ID);
            return;
        }

        var createRoomRequest = Serializer.deserialize(request.getContent().get(), CreateRoomRequest.class);

        var roomMember = RoomMember.builder()
                .username(createRoomRequest.getUsername())
                .clientId(clientState.CLIENT_ID)
                .build();

        var room = Room.withMember(roomMember);

        ServerState.addRoom(room);
        clientState.ROOM_ID = room.getId();

        var response = Response.success(ResponseType.CREATE_ROOM.getName(), Serializer.serialize(new CreateRoomResponse(room.getId())));

        queueClientResponse(response);
    }

    public void joinRoom(Request request) {
        log.debug("Joining room, client: {}, request: {}", clientState.CLIENT_ID, request);

        if (request.getContent().isEmpty()) {
            log.debug("Invalid request: {}. Skipping", request);
            return;
        }

        if (Objects.nonNull(clientState.ROOM_ID)) {
            // TODO: Send error response
            log.debug("Client already in a room: {}", clientState.ROOM_ID);
            return;
        }

        var joinRoomRequest = Serializer.deserialize(request.getContent().get(), JoinRoomRequest.class);

        var room = ServerState.getRoom(joinRoomRequest.getRoomId());
        if (Objects.isNull(room)) {
            log.debug("Room not found: {}", joinRoomRequest.getRoomId());
            return;
        }

        var roomMember = RoomMember.builder()
                .username(joinRoomRequest.getUsername())
                .clientId(clientState.CLIENT_ID)
                .build();

        room.addMember(roomMember);
        clientState.ROOM_ID = room.getId();

        var roomJoinResponse = JoinRoomResponse.builder()
                .roomId(room.getId())
                .clientId(clientState.CLIENT_ID)
                .username(roomMember.getUsername())
                .build();

        var response = Response.success(ResponseType.JOIN_ROOM.getName(), Serializer.serialize(roomJoinResponse));

        queueClientResponse(response);

        queueRoomResponse(response);
    }

    public void leaveRoom(Request request) {
        log.debug("Leaving room, client: {}, request: {}", clientState.CLIENT_ID, request);
        leaveRoomWithCleanUpAndNotify();
    }

    public void roomMessage(Request request) {
        log.debug("Sending message to room, client: {}, request: {}", clientState.CLIENT_ID, request);

        if (request.getContent().isEmpty()) {
            log.debug("Invalid request: {}. Skipping", request);
            return;
        }

        var roomMessageRequest = Serializer.deserialize(request.getContent().get(), RoomMessageRequest.class);

        var senderMember = ServerState.getRoom(roomMessageRequest.getRoomId()).getMember(clientState.CLIENT_ID);

        if (senderMember.isEmpty()) {
            log.debug("Sender user not found in room: {}", clientState.CLIENT_ID);
            var response = Response.failure(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(new ErrorResponse("Sender user not found in room")));
            queueClientResponse(response);
            return;
        }

        var roomMessageResponse = RoomMessageResponse.builder()
                .clientId(clientState.CLIENT_ID)
                .message(roomMessageRequest.getMessage())
                .username(senderMember.get().getUsername())
                .build();

        var response = Response.success(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(roomMessageResponse));

        queueRoomResponse(response);
    }

}
