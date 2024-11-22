package com.kurtcan.client.request;

import com.kurtcan.client.ClientState;
import com.kurtcan.shared.threading.Connection;
import lombok.extern.slf4j.Slf4j;

import java.io.Console;

@Slf4j
@SuppressWarnings("InfiniteLoopStatement")
public class RequestHandler implements Runnable {
    private final Connection connection;
    private final Console console = System.console();
    private final CommandHandler commandHandler;

    public RequestHandler(Connection connection) {
        this.connection = connection;
        this.commandHandler = new CommandHandler(connection);
    }

    @Override
    public void run() {
        try {
            while (true) {
                console.flush();
                var consoleInput = console.readLine();

                if (!ClientState.isRoomJoined()) {
                    if (CommandHandler.isCommand(consoleInput, CommandHandler.CREATE_ROOM_COMMAND)) {
                        commandHandler.createRoom(consoleInput);
                        continue;
                    }
                    if (CommandHandler.isCommand(consoleInput, CommandHandler.JOIN_ROOM_COMMAND)) {
                        commandHandler.joinRoom(consoleInput);
                        continue;
                    }
                }

                if (CommandHandler.isCommand(consoleInput, CommandHandler.LEAVE_ROOM_COMMAND)) {
                    commandHandler.leaveRoom();
                    continue;
                }

                if (ClientState.isRoomJoined()) {
                    commandHandler.messageRoom(consoleInput);
                    continue;
                }

                commandHandler.showHelp();
            }

        } catch (Exception ex) {
            log.error("Cannot start request handler: {}", ex.getMessage());
            connection.setAsDisconnected();
        }
    }
}
