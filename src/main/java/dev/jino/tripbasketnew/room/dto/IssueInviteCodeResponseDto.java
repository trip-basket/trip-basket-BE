package dev.jino.tripbasketnew.room.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record IssueInviteCodeResponseDto(
        @Schema(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID roomId,

        @Schema(description = "활성 초대코드", example = "A2C3D4") String inviteCode,

        @Schema(description = "초대코드 발급 시각", example = "2026-03-22T12:34:56")
        LocalDateTime issuedAt) {}
