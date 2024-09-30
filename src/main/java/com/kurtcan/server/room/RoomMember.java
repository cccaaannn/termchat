package com.kurtcan.server.room;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomMember {
    private String username;
    @EqualsAndHashCode.Include
    private UUID clientId;
}
