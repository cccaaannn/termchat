package com.kurtcan.shared.threading;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;

@Slf4j
@Getter
public class Connection implements Closeable {
    @NonNull
    private final Socket socket;
    @NonNull
    private final InputStream input;
    @NonNull
    private final OutputStream output;
    @NonNull
    private final BufferedReader reader;
    @NonNull
    private final PrintWriter writer;

    private volatile boolean isDisconnected = false;

    @SneakyThrows
    public Connection(@NonNull final Socket socket) {
        this.socket = socket;
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
        this.reader = new BufferedReader(new InputStreamReader(input));
        this.writer = new PrintWriter(output, true);
    }

    public void setAsDisconnected() {
        isDisconnected = true;
    }

    public boolean isDisconnected() {
        return socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown() || isDisconnected;
    }

    @Override
    public void close() {
        try {
            writer.close();
            reader.close();
            output.close();
            input.close();
            socket.close();
        } catch (IOException e) {
            log.error("Error closing client connection: {}", e.getMessage());
        }
    }
}
