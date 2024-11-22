package com.kurtcan.client.request;

import com.kurtcan.client.ClientState;
import com.kurtcan.client.util.ClientPrinter;
import com.kurtcan.shared.RequestType;
import com.kurtcan.shared.dto.request.CreateRoomRequest;
import com.kurtcan.shared.dto.request.JoinRoomRequest;
import com.kurtcan.shared.dto.request.LeaveRoomRequest;
import com.kurtcan.shared.dto.request.RoomMessageRequest;
import com.kurtcan.shared.serialization.Serializer;
import com.kurtcan.shared.threading.Connection;
import com.kurtcan.sttp.request.Request;
import com.kurtcan.sttp.request.RequestParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CommandHandler {

    public static final char CREATE_ROOM_COMMAND = 'c';
    public static final char JOIN_ROOM_COMMAND = 'j';
    public static final char LEAVE_ROOM_COMMAND = 'l';

    private final Connection connection;

    private void send(Request request) {
        log.debug("Sending request: {}", request);
        connection.getWriter().println(RequestParser.serialize(request));
    }

    private Optional<List<String>> splitCommand(String consoleInput, int expectedLength, String usage) {
        var split = consoleInput.strip().split(" ");
        if (split.length < expectedLength) {
            ClientPrinter.print(usage);
            return Optional.empty();
        }
        return Optional.of(List.of(split));
    }

    public static boolean isCommand(String consoleInput, char command) {
        return consoleInput.startsWith("/" + command);
    }

    public void showHelp() {
        ClientPrinter.print("""
                Available commands:
                /c <username> - Create a room and join with username
                /j <roomId> <username> - Join a room with username
                /l - Leave the active room
                Type a message to send to the active room
                """);
    }

    public void createRoom(String consoleInput) {
        var split = splitCommand(consoleInput, 2, "Usage: /c <username>");
        if (split.isEmpty()) return;

        var roomRequest = CreateRoomRequest.builder()
                .username(split.get().get(1))
                .build();

        var roomRequestStr = Serializer.serialize(roomRequest);

        var request = Request.builder()
                .type(RequestType.CREATE_ROOM.getName())
                .content(roomRequestStr)
                .build();

        send(request);
    }

    public void joinRoom(String consoleInput) {
        var split = splitCommand(consoleInput, 3, "Usage: /j <roomId> <username>");
        if (split.isEmpty()) return;

        var roomRequest = JoinRoomRequest.builder()
                .roomId(UUID.fromString(split.get().get(1)))
                .username(split.get().get(2))
                .build();

        var request = Request.builder()
                .type(RequestType.JOIN_ROOM.getName())
                .content(Serializer.serialize(roomRequest))
                .build();

        send(request);
    }

    public void leaveRoom() {
        var request = Request.builder()
                .type(RequestType.LEAVE_ROOM.getName())
                .content(Serializer.serialize(new LeaveRoomRequest()))
                .build();

        send(request);

        // Reset active room id, even if the server fails to process the request
        ClientState.leaveRoom();
    }

    public void messageRoom(String consoleInput) {
        if (consoleInput.isBlank()) return;

        var roomMessage = RoomMessageRequest.builder()
                .roomId(ClientState.getRoomId())
                .message(consoleInput)
                .build();

        var request = Request.builder()
                .type(RequestType.MESSAGE_ROOM.getName())
                .content(Serializer.serialize(roomMessage))
                .build();

        send(request);
    }
}
