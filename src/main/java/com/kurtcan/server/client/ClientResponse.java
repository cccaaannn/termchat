package com.kurtcan.server.client;

import com.kurtcan.sttp.response.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
public record ClientResponse(Response response, UUID receiverId) {
}
