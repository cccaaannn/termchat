package com.kurtcan.shared.dto.response;

import com.kurtcan.shared.serialization.SerializableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomResponse {
    @SerializableField
    private UUID roomId;
    @SerializableField
    private UUID clientId;
    @SerializableField
    private String username;
}
