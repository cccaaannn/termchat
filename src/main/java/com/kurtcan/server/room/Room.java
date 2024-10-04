package com.kurtcan.server.room;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@Builder
public class Room {
    private UUID id;
    @Builder.Default
    private List<RoomMember> members = new CopyOnWriteArrayList<>();

    public static Room withMember(RoomMember roomMember) {
        return Room.builder()
                .id(UUID.randomUUID())
                .members(new CopyOnWriteArrayList<>(List.of(roomMember)))
                .build();
    }

    public void addMember(RoomMember member) {
        members.add(member);
    }

    public Optional<RoomMember> getMember(UUID memberId) {
        return members.stream()
                .filter(member -> member.getClientId().equals(memberId))
                .findFirst();
    }

    public void removeMember(UUID memberId) {
        members.removeIf(member -> member.getClientId().equals(memberId));
    }
}
