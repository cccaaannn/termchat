package com.kurtcan.server.client;

import com.kurtcan.server.ServerState;
import com.kurtcan.server.room.RoomService;
import com.kurtcan.shared.RequestType;
import com.kurtcan.shared.ResponseType;
import com.kurtcan.shared.dto.response.ConnectResponse;
import com.kurtcan.shared.serialization.Serializer;
import com.kurtcan.shared.threading.Connection;
import com.kurtcan.shared.threading.ThreadingUtils;
import com.kurtcan.sttp.request.RequestParser;
import com.kurtcan.sttp.response.Response;
import com.kurtcan.sttp.response.ResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class ClientHandler implements Runnable {
    private final Connection connection;
    private final UUID clientId;
    private final RoomService roomService;
    private final ClientState clientState = new ClientState();

    public ClientHandler(Socket socket) {
        var clientId = UUID.randomUUID();
        this.connection = new Connection(socket);
        this.clientId = clientId;
        this.roomService = new RoomService(clientId, clientState);
        ServerState.CLIENT_CONNECTIONS.put(clientId, connection);

        log.debug("Client connected with id: {}, address: {}", clientId, socket.getInetAddress());
    }

    private void handleConnect() {
        var connectResponse = ConnectResponse.builder()
                .clientId(clientId)
                .build();

        var response = Response.builder()
                .type(ResponseType.CONNECT.name())
                .status(ResponseStatus.SUCCESS)
                .content(Serializer.serialize(connectResponse))
                .build();

        var clientResponse = ClientResponse.builder()
                .receiverId(clientId)
                .response(response)
                .build();

        ServerState.RESPONSE_QUEUE.add(clientResponse);
    }

    private void handleDisconnect() {
        log.debug("Disconnecting client: {}", clientId);

        var clientConnection = ServerState.CLIENT_CONNECTIONS.remove(clientId);
        if (Objects.nonNull(clientConnection)) {
            clientConnection.close();
        }

        // Clean up room
        if (Objects.nonNull(clientState.ROOM_ID)) {
            roomService.leaveRoomWithCleanUpAndNotify();
        }
    }

    @Override
    public void run() {
        try (connection) {
            ThreadingUtils.sleep(200);
            handleConnect();

            while (true) {
                ThreadingUtils.sleep(200);

                if (connection.isDisconnected()) {
                    handleDisconnect();
                    break;
                }

                var requestStr = connection.getReader().readLine();
                if (requestStr == null) {
                    handleDisconnect();
                    break;
                }

                var request = RequestParser.deserialize(requestStr);
                log.debug("Incoming request: {}", request);

                if (request.isEmpty()) {
                    log.info("Invalid request: {}, Skipping", requestStr);
                    continue;
                }

                var type = RequestType.fromString(request.get().getType());
                if (type.isEmpty()) {
                    log.info("Invalid request type: {}, Skipping", request.get().getType());
                    continue;
                }

                switch (type.get()) {
                    case DISCONNECT -> handleDisconnect();
                    case CREATE_ROOM -> this.roomService.createRoom(request.get());
                    case JOIN_ROOM -> this.roomService.joinRoom(request.get());
                    case LEAVE_ROOM -> this.roomService.leaveRoom(request.get());
                    case MESSAGE_ROOM -> this.roomService.roomMessage(request.get());
                }
            }
        } catch (Exception ex) {
            log.info("Client disconnected unexpectedly: {}", ex.getMessage());
            handleDisconnect();
        }
    }
}