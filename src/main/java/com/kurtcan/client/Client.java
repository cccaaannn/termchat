package com.kurtcan.client;

import com.kurtcan.client.request.RequestHandler;
import com.kurtcan.client.response.ResponseHandler;
import com.kurtcan.client.util.ClientPrinter;
import com.kurtcan.shared.threading.Connection;
import com.kurtcan.shared.threading.ThreadingUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.util.concurrent.Executors;

@Slf4j
@SuppressWarnings("InfiniteLoopStatement")
public class Client {
    private final static String hostname = "localhost";

    private static void handleDisconnect() {
        ClientPrinter.print("Disconnected from server");
        System.exit(1);
    }

    public static void main(String[] args) {
        var port = args.length > 0 ? Integer.parseInt(args[0]) : 10042;

        try (
                var connection = new Connection(new Socket(hostname, port));
                var executor = Executors.newVirtualThreadPerTaskExecutor()
        ) {
            log.info("Connected to server");

            var requestHandler = new RequestHandler(connection);
            var responseHandler = new ResponseHandler(connection);
            executor.submit(requestHandler);
            executor.submit(responseHandler);

            while (true) {
                ThreadingUtils.sleep(200);

                if (connection.isDisconnected()) {
                    handleDisconnect();
                }
            }
        } catch (Exception ex) {
            log.warn("Server disconnected unexpectedly: {}", ex.getMessage());
            handleDisconnect();
        }
    }
}
