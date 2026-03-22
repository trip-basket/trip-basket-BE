package dev.jino.tripbasketnew.room.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import dev.jino.tripbasketnew.room.entity.RoomRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record RoomMemberResponseDto(
        @Schema(description = "사용자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID memberId,

        @Schema(description = "닉네임", example = "Jino") String nickname,

        @Schema(description = "방 역할", example = "OWNER") RoomRole role,

        @Schema(description = "참여 시각", example = "2026-03-22T12:34:56")
        LocalDateTime joinedAt) {}
