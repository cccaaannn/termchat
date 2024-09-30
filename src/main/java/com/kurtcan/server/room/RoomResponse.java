package com.kurtcan.server.room;

import com.kurtcan.sttp.response.Response;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RoomResponse {
    private final Response response;
    private final UUID roomId;
}
