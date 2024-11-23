package com.kurtcan.server;

import com.kurtcan.server.client.ClientResponse;
import com.kurtcan.server.room.Room;
import com.kurtcan.server.room.RoomResponse;
import com.kurtcan.shared.threading.Connection;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class ServerState {
    private final static Map<UUID, Connection> CLIENT_CONNECTIONS = new ConcurrentHashMap<>();
    private final static BlockingQueue<ClientResponse> RESPONSE_QUEUE = new LinkedBlockingQueue<>();

    private final static Map<UUID, Room> ROOMS = new ConcurrentHashMap<>();
    private final static BlockingQueue<RoomResponse> ROOM_RESPONSE_QUEUE = new LinkedBlockingQueue<>();

    // Client connection management
    public static Connection getClientConnection(UUID clientId) {
        return CLIENT_CONNECTIONS.get(clientId);
    }

    public static void addClient(UUID clientId, Connection connection) {
        CLIENT_CONNECTIONS.put(clientId, connection);
        log.debug("Client added {}, Clients: {}", clientId, CLIENT_CONNECTIONS);
    }

    public static Connection removeClient(UUID clientId) {
        log.debug("Client removed {}, Clients: {}", clientId, CLIENT_CONNECTIONS);
        return CLIENT_CONNECTIONS.remove(clientId);
    }

    // Room management
    public static Room getRoom(UUID roomId) {
        return ROOMS.get(roomId);
    }

    public static void addRoom(Room room) {
        ROOMS.put(room.getId(), room);
        log.debug("Room added {}, Rooms: {}", room.getId(), ROOMS);
    }

    public static void removeRoom(UUID roomId) {
        ROOMS.remove(roomId);
        log.debug("Room removed {}, Rooms: {}", roomId, ROOMS);
    }

    // Response management
    public static void queueRoomResponse(RoomResponse roomResponse) {
        ROOM_RESPONSE_QUEUE.add(roomResponse);
        log.debug("Room response queued: {}", roomResponse);
    }

    public static RoomResponse takeRoomResponse() throws InterruptedException {
        return ROOM_RESPONSE_QUEUE.take();
    }

    public static void queueClientResponse(ClientResponse clientResponse) {
        RESPONSE_QUEUE.add(clientResponse);
        log.debug("Client response queued: {}", clientResponse);
    }

    public static ClientResponse takeClientResponse() throws InterruptedException {
        return RESPONSE_QUEUE.take();
    }
}
