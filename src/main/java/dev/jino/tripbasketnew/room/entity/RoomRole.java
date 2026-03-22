package dev.jino.tripbasketnew.room.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "방 참여자 역할")
public enum RoomRole {
    OWNER,
    MEMBER
}
