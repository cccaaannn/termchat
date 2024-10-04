package com.kurtcan.server.processor;

import com.kurtcan.server.ServerState;
import com.kurtcan.server.client.ClientResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@SuppressWarnings("InfiniteLoopStatement")
public class RoomMessageQueueProcessor implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                var roomResponse = ServerState.takeRoomResponse();
                var room = ServerState.getRoom(roomResponse.roomId());
                if (Objects.isNull(room)) {
                    log.error("Cannot find room: {}", roomResponse.roomId());
                    continue;
                }
                var roomMembers = room.getMembers();

                for (var member : roomMembers) {
                    var clientResponse = ClientResponse.builder()
                            .response(roomResponse.response())
                            .receiverId(member.getClientId())
                            .build();

                    ServerState.queueClientResponse(clientResponse);
                }
            } catch (Exception ex) {
                log.error("Cannot process message: {}", ex.getMessage());
            }
        }
    }
}
