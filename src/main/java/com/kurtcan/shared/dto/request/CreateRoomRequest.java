package com.kurtcan.shared.dto.request;

import com.kurtcan.shared.serialization.SerializableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    @SerializableField
    private String username;
}
