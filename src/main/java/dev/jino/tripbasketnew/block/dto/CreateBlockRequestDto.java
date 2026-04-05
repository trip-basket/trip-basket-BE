package dev.jino.tripbasketnew.block.dto;

import java.time.LocalDateTime;

import dev.jino.tripbasketnew.block.dto.validation.ValidCreateBlockRequest;
import dev.jino.tripbasketnew.block.entity.BlockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidCreateBlockRequest
public record CreateBlockRequestDto(
        @Schema(
                description = "블록 상태",
                allowableValues = {"bucket", "scheduled"},
                example = "scheduled")
        @NotNull(message = "status는 필수입니다.")
        BlockStatus status,

        @Schema(description = "Google Place ID", example = "ChIJdd4hrwug2EcRmSrV3Vo6llI")
        @NotBlank(message = "googlePlaceId는 비어 있을 수 없습니다.")
        String googlePlaceId,

        @Schema(description = "사용자가 지정한 블록 표시명", example = "대영박물관 관람") @NotBlank(message = "name은 비어 있을 수 없습니다.")
        String name,

        @Schema(description = "블록 시작 시각. status가 scheduled일 때 필수이며 장소 현지 기준 로컬 시간입니다.", example = "2024-03-16T10:00:00")
        LocalDateTime startTime,

        @Schema(description = "블록 종료 시각. status가 scheduled일 때 필수이며 장소 현지 기준 로컬 시간입니다.", example = "2024-03-16T11:30:00")
        LocalDateTime endTime) {}
