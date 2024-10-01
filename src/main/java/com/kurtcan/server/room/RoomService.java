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
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class RoomService {
    private final UUID clientId;
    private final ClientState clientState;

    private void queueClientResponse(Response response) {
        var clientResponse = ClientResponse.builder()
                .response(response)
                .receiverId(clientId)
                .build();

        ServerState.RESPONSE_QUEUE.add(clientResponse);
    }

    public void leaveRoomWithCleanUpAndNotify() {
        if (Objects.isNull(clientState.ROOM_ID)) {
            log.debug("Client not in a room: {}", clientId);
            return;
        }

        var room = ServerState.ROOMS.get(clientState.ROOM_ID);
        var roomMember = room.getMember(clientId);
        if (roomMember.isEmpty()) {
            log.debug("User not found in room: {}", clientId);
            return;
        }

        // Leave room
        clientState.ROOM_ID = null;
        room.removeMember(clientId);
        // Remove room if empty
        if (room.getMembers().isEmpty()) {
            ServerState.ROOMS.remove(room.getId());
            return;
        }

        // Notify other members in the room, if room is not empty
        var response = Response.success(
                ResponseType.LEAVE_ROOM.getName(),
                Serializer.serialize(new LeaveRoomResponse(room.getId(), clientId, roomMember.get().getUsername()))
        );

        var roomResponse = RoomResponse.builder()
                .roomId(room.getId())
                .response(response)
                .build();

        ServerState.ROOM_RESPONSE_QUEUE.add(roomResponse);
    }

    public void createRoom(Request request) {
        log.debug("Creating room, client: {}, request: {}", clientId, request);

        if (request.getContent().isEmpty()) {
            log.info("Invalid request: {}. Skipping", request);
            return;
        }

        if (Objects.nonNull(clientState.ROOM_ID)) {
            // TODO: Send error response
            log.info("Client already in a room: {}", clientState.ROOM_ID);
            return;
        }

        var createRoomRequest = Serializer.deserialize(request.getContent().get(), CreateRoomRequest.class);

        var roomMember = RoomMember.builder()
                .username(createRoomRequest.getUsername())
                .clientId(clientId)
                .build();

        var room = Room.withMember(roomMember);

        ServerState.ROOMS.put(room.getId(), room);
        clientState.ROOM_ID = room.getId();

        log.info("Room created, id: {}, client: {}", room.getId(), clientId);

        var response = Response.success(ResponseType.CREATE_ROOM.getName(), Serializer.serialize(new CreateRoomResponse(room.getId())));

        queueClientResponse(response);
    }

    public void joinRoom(Request request) {
        log.debug("Joining room, client: {}, request: {}", clientId, request);

        if (request.getContent().isEmpty()) {
            log.info("Invalid request: {}. Skipping", request);
            return;
        }

        if (Objects.nonNull(clientState.ROOM_ID)) {
            // TODO: Send error response
            log.info("Client already in a room: {}", clientState.ROOM_ID);
            return;
        }

        var joinRoomRequest = Serializer.deserialize(request.getContent().get(), JoinRoomRequest.class);

        var room = ServerState.ROOMS.get(joinRoomRequest.getRoomId());
        if (Objects.isNull(room)) {
            log.info("Room not found: {}", joinRoomRequest.getRoomId());
            return;
        }

        var roomMember = RoomMember.builder()
                .username(joinRoomRequest.getUsername())
                .clientId(clientId)
                .build();

        room.addMember(roomMember);
        clientState.ROOM_ID = room.getId();

        log.debug("User: {} joined room: {}", roomMember, room);
        log.debug("Rooms: {}", ServerState.ROOMS);

        var roomJoinResponse = JoinRoomResponse.builder()
                .roomId(room.getId())
                .clientId(clientId)
                .username(roomMember.getUsername())
                .build();

        var response = Response.success(ResponseType.JOIN_ROOM.getName(), Serializer.serialize(roomJoinResponse));

        queueClientResponse(response);

        // Notify other members in the room
        var roomResponse = new RoomResponse(response, room.getId());

        ServerState.ROOM_RESPONSE_QUEUE.add(roomResponse);
    }

    public void leaveRoom(Request request) {
        log.debug("Leaving room, client: {}, request: {}", clientId, request);
        leaveRoomWithCleanUpAndNotify();
    }

    public void roomMessage(Request request) {
        log.debug("Sending message to room, client: {}, request: {}", clientId, request);

        if (request.getContent().isEmpty()) {
            log.info("Invalid request: {}. Skipping", request);
            return;
        }

        var roomMessageRequest = Serializer.deserialize(request.getContent().get(), RoomMessageRequest.class);

        var senderMember = ServerState.ROOMS.get(roomMessageRequest.getRoomId()).getMember(clientId);

        if (senderMember.isEmpty()) {
            log.info("Sender user not found in room: {}", clientId);

            var response = Response.failure(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(new ErrorResponse("Sender user not found in room")));

            queueClientResponse(response);
            return;
        }

        var roomMessageResponse = RoomMessageResponse.builder()
                .clientId(clientId)
                .message(roomMessageRequest.getMessage())
                .username(senderMember.get().getUsername())
                .build();

        var response = Response.success(ResponseType.MESSAGE_ROOM.getName(), Serializer.serialize(roomMessageResponse));

        var roomResponse = new RoomResponse(response, roomMessageRequest.getRoomId());

        ServerState.ROOM_RESPONSE_QUEUE.add(roomResponse);
    }

}
