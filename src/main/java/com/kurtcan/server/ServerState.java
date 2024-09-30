package com.kurtcan.server;

import com.kurtcan.server.client.ClientResponse;
import com.kurtcan.server.room.Room;
import com.kurtcan.server.room.RoomResponse;
import com.kurtcan.shared.threading.Connection;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerState {
    public final static Map<UUID, Connection> CLIENT_CONNECTIONS = new ConcurrentHashMap<>();
    public final static BlockingQueue<ClientResponse> RESPONSE_QUEUE = new LinkedBlockingQueue<>();

    public final static Map<UUID, Room> ROOMS = new ConcurrentHashMap<>();
    public final static BlockingQueue<RoomResponse> ROOM_RESPONSE_QUEUE = new LinkedBlockingQueue<>();
}
