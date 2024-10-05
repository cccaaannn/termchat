package com.kurtcan.client.response;

import com.kurtcan.client.util.ClientPrinter;
import com.kurtcan.client.ClientState;
import com.kurtcan.shared.threading.Connection;
import com.kurtcan.shared.ResponseType;
import com.kurtcan.shared.dto.response.ConnectResponse;
import com.kurtcan.shared.dto.response.ErrorResponse;
import com.kurtcan.shared.serialization.Serializer;
import com.kurtcan.shared.threading.ThreadingUtils;
import com.kurtcan.sttp.response.ResponseParser;
import com.kurtcan.sttp.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("InfiniteLoopStatement")
public class ResponseHandler implements Runnable {

    private final Connection connection;
    private final RoomHandler roomHandler = new RoomHandler();

    private void handleConnect(String content) {
        var response = Serializer.deserialize(content, ConnectResponse.class);
        ClientState.CLIENT_ID = response.getClientId();
        ClientPrinter.print("Connected with id: {}", ClientState.CLIENT_ID);
    }

    @Override
    public void run() {
        try {
            while (true) {
                ThreadingUtils.sleep(200);

                try {
                    String responseStr = connection.getReader().readLine();
                    if (responseStr == null) {
                        connection.setAsDisconnected();
                        continue;
                    }

                    log.debug("Incoming response raw: {}", responseStr);
                    var response = ResponseParser.deserialize(responseStr);
                    if (response.isEmpty()) {
                        log.error("Cannot parse response: {}", responseStr);
                        continue;
                    }
                    log.debug("Incoming response: {}", response);

                    var content = response.get().getContent();

                    if (ResponseStatus.FAILURE.equals(response.get().getStatus())) {
                        if (content.isPresent()) {
                            var errorResponse = Serializer.deserialize(content.get(), ErrorResponse.class);
                            ClientPrinter.print("Error response: {}", errorResponse.getMessage());
                        } else {
                            ClientPrinter.print("Error response without content received");
                        }
                        continue;
                    }

                    if (content.isEmpty()) {
                        log.debug("Response content is empty");
                        continue;
                    }

                    var type = ResponseType.fromString(response.get().getType());
                    if (type.isEmpty()) {
                        log.error("Cannot parse response type: {}", response.get().getType());
                        continue;
                    }

                    switch (type.get()) {
                        case CONNECT -> handleConnect(content.get());
                        case CREATE_ROOM -> roomHandler.createRoom(content.get());
                        case JOIN_ROOM -> roomHandler.joinRoomSelf(content.get());
                        case LEAVE_ROOM -> roomHandler.leaveRoom(content.get());
                        case MESSAGE_ROOM -> roomHandler.messageRoom(content.get());
                    }
                } catch (Exception ex) {
                    log.error("Cannot process response: {}", ex.getMessage());
                }
            }

        } catch (Exception ex) {
            log.error("Cannot start response handler: {}", ex.getMessage());
            connection.setAsDisconnected();
        }
    }
}
