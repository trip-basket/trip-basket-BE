package dev.jino.tripbasketnew.room.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateRoomRequestDto(
        @Schema(description = "방 이름", example = "런던 여행 수정안") String name,

        @Schema(description = "여행 시작일", example = "2026-03-17")
        LocalDate tripStartDate,

        @Schema(description = "여행 종료일", example = "2026-03-30")
        LocalDate tripEndDate) {}
