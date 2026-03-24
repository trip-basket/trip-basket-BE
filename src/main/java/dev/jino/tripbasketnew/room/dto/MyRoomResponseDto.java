package dev.jino.tripbasketnew.room.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import dev.jino.tripbasketnew.room.entity.RoomRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record MyRoomResponseDto(
        @Schema(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID roomId,

        @Schema(description = "방 이름", example = "런던 여행") String name,

        @Schema(description = "여행 시작일", example = "2026-03-16")
        LocalDate tripStartDate,

        @Schema(description = "여행 종료일", example = "2026-03-29")
        LocalDate tripEndDate,

        @Schema(description = "내 역할", example = "MEMBER") RoomRole role,

        @Schema(description = "참여 일시", example = "2026-03-10T14:00:00")
        LocalDateTime joinedAt,

        @Schema(description = "현재 참여자 수", example = "3") long memberCount) {}
