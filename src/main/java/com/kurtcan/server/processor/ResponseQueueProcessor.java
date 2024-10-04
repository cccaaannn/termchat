package com.kurtcan.server.processor;

import com.kurtcan.server.ServerState;
import com.kurtcan.sttp.response.ResponseParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("InfiniteLoopStatement")
public class ResponseQueueProcessor implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                var clientResponse = ServerState.takeClientResponse();
                var clientConnection = ServerState.getClientConnection(clientResponse.receiverId());
                var response = ResponseParser.serialize(clientResponse.response());

                clientConnection.getWriter().println(response);
            } catch (Exception ex) {
                log.error("Cannot process message: {}", ex.getMessage());
            }
        }
    }
}
