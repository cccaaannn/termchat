package com.kurtcan.server;

import com.kurtcan.server.client.ClientHandler;
import com.kurtcan.server.processor.ResponseQueueProcessor;
import com.kurtcan.server.processor.RoomMessageQueueProcessor;
import com.kurtcan.shared.threading.ThreadingUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

@Slf4j
@SuppressWarnings("InfiniteLoopStatement")
public class Server {
    public static void main(String[] args) {
        var port = args.length > 0 ? Integer.parseInt(args[0]) : 10042;

        try (
                var serverSocket = new ServerSocket(port);
                var executor = Executors.newVirtualThreadPerTaskExecutor();
        ) {
            log.info("Server starting on port {}", port);

            executor.submit(new RoomMessageQueueProcessor());
            executor.submit(new ResponseQueueProcessor());

            while (true) {
                ThreadingUtils.sleep(200);
                var socket = serverSocket.accept();
                var clientHandler = new ClientHandler(socket);
                executor.submit(clientHandler);
            }
        } catch (IOException ex) {
            log.error("Cannot start server: {}", ex.getMessage());
        }
    }
}
