package com.kurtcan.shared.dto.request;

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
public class JoinRoomRequest {
    @SerializableField
    private UUID roomId;
    @SerializableField
    private String username;
}
