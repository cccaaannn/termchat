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
                .receiverId(clientState.getClientId())
                .build();

        ServerState.queueClientResponse(clientResponse);
    }

    private void queueRoomResponse(Response response) {
        var roomResponse = RoomResponse.builder()
                .roomId(clientState.getRoomId())
                .response(response)
                .build();

        ServerState.queueRoomResponse(roomResponse);
    }

    public void createRoom(Request request) {
        log.debug("Creating room, client: {}, request: {}", clientState.getClientId(), request);

        if (request.getContent().isEmpty()) {
            log.debug("Invalid request: {}. Skipping", request);
            return;
        }

        if (clientState.isRoomJoined()) {
            log.debug("Client already in a room: {}", clientState.getRoomId());
            var response = Response.failure(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(new ErrorResponse("Client already in a room")));
            queueClientResponse(response);
            return;
        }

        var createRoomRequest = Serializer.deserialize(request.getContent().get(), CreateRoomRequest.class);

        var roomMember = RoomMember.builder()
                .username(createRoomRequest.getUsername())
                .clientId(clientState.getClientId())
                .build();

        var room = Room.withMember(roomMember);

        ServerState.addRoom(room);
        clientState.joinRoom(room.getId());

        var response = Response.success(ResponseType.CREATE_ROOM.getName(), Serializer.serialize(new CreateRoomResponse(room.getId())));

        queueClientResponse(response);
    }

    public void joinRoom(Request request) {
        log.debug("Joining room, client: {}, request: {}", clientState.getClientId(), request);

        if (request.getContent().isEmpty()) {
            log.debug("Invalid request: {}. Skipping", request);
            return;
        }

        if (clientState.isRoomJoined()) {
            log.debug("Client already in a room: {}", clientState.getRoomId());
            var response = Response.failure(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(new ErrorResponse("Client already in a room")));
            queueClientResponse(response);
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
                .clientId(clientState.getClientId())
                .build();

        room.addMember(roomMember);
        clientState.joinRoom(room.getId());

        var roomJoinResponse = JoinRoomResponse.builder()
                .roomId(room.getId())
                .clientId(clientState.getClientId())
                .username(roomMember.getUsername())
                .build();

        var response = Response.success(ResponseType.JOIN_ROOM.getName(), Serializer.serialize(roomJoinResponse));

        queueRoomResponse(response);
    }

    public void leaveRoom(Request request) {
        log.debug("Leaving room, client: {}, request: {}", clientState.getClientId(), request);
        leaveRoomWithCleanUpAndNotify();
    }

    public void roomMessage(Request request) {
        log.debug("Sending message to room, client: {}, request: {}", clientState.getClientId(), request);

        if (request.getContent().isEmpty()) {
            log.debug("Invalid request: {}. Skipping", request);
            return;
        }

        var roomMessageRequest = Serializer.deserialize(request.getContent().get(), RoomMessageRequest.class);

        var senderMember = ServerState.getRoom(roomMessageRequest.getRoomId()).getMember(clientState.getClientId());

        if (senderMember.isEmpty()) {
            log.debug("Sender client: {} not found in room: {}", clientState.getClientId(), clientState.getRoomId());
            var response = Response.failure(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(new ErrorResponse("Sender user not found in room")));
            queueClientResponse(response);
            return;
        }

        var roomMessageResponse = RoomMessageResponse.builder()
                .clientId(clientState.getClientId())
                .message(roomMessageRequest.getMessage())
                .username(senderMember.get().getUsername())
                .build();

        var response = Response.success(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(roomMessageResponse));

        queueRoomResponse(response);
    }

    public void leaveRoomWithCleanUpAndNotify() {
        if (!clientState.isRoomJoined()) {
            log.debug("Client {} is not in a room", clientState.getClientId());
            return;
        }

        var room = ServerState.getRoom(clientState.getRoomId());
        var roomMember = room.getMember(clientState.getClientId());
        if (roomMember.isEmpty()) {
            log.debug("Client: {} not found in room: {}", clientState.getClientId(), clientState.getRoomId());
            return;
        }

        // Leave room
        room.removeMember(clientState.getClientId());

        // Remove room if empty
        if (room.getMembers().isEmpty()) {
            ServerState.removeRoom(room.getId());
            return;
        }

        // Notify other members in the room, if room is not empty
        var response = Response.success(
                ResponseType.LEAVE_ROOM.getName(),
                Serializer.serialize(new LeaveRoomResponse(room.getId(), clientState.getClientId(), roomMember.get().getUsername()))
        );

        queueRoomResponse(response);

        // Clean up client state after notifying room members
        clientState.leaveRoom();
    }
}
