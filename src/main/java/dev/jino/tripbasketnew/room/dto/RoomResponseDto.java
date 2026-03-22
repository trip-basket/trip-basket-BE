package dev.jino.tripbasketnew.room.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record RoomResponseDto(
        @Schema(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "방 이름", example = "런던 여행") String name,

        @Schema(description = "여행 시작일", example = "2026-03-16")
        LocalDate tripStartDate,

        @Schema(description = "여행 종료일", example = "2026-03-29")
        LocalDate tripEndDate,

        @Schema(description = "생성 일시", example = "2026-03-22T12:34:56")
        LocalDateTime createdAt) {}
