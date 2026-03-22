package dev.jino.tripbasketnew.room.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomRequestDto(
        @Schema(description = "방 이름", example = "런던 여행") @NotBlank(message = "name은 비어 있을 수 없습니다.")
        String name,

        @Schema(description = "여행 시작일", example = "2026-03-16") @NotNull(message = "tripStartDate는 필수입니다.")
        LocalDate tripStartDate,

        @Schema(description = "여행 종료일", example = "2026-03-29") @NotNull(message = "tripEndDate는 필수입니다.")
        LocalDate tripEndDate) {}
