package com.kurtcan.server.room;

import com.kurtcan.sttp.response.Response;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RoomResponse(Response response, UUID roomId) {
}
