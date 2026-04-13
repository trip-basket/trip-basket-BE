package dev.jino.tripbasketnew.block.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import dev.jino.tripbasketnew.block.entity.BlockStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record UpdateBlockRequestDto(
        @Schema(
                description = "변경할 블록 상태. 미전송 시 기존 값을 유지합니다.",
                allowableValues = {"bucket", "scheduled"},
                nullable = true,
                example = "scheduled")
        BlockStatus status,

        @Pattern(regexp = ".*\\S.*", message = "name must not be blank")
        @Schema(description = "변경할 블록 표시명. 미전송 시 기존 값을 유지합니다.", nullable = true, example = "대영박물관 관람")
        String name,

        @Schema(
                description = "변경할 블록 시작 시각. timezoneId 기준 로컬 시간입니다. 미전송 시 기존 값을 유지합니다.",
                nullable = true,
                example = "2024-03-16T10:00:00")
        LocalDateTime startTime,

        @Schema(
                description = "변경할 블록 종료 시각. timezoneId 기준 로컬 시간입니다. 미전송 시 기존 값을 유지합니다.",
                nullable = true,
                example = "2024-03-16T11:30:00")
        LocalDateTime endTime,

        @Schema(
                description = "변경할 메모. 미전송 시 기존 값을 유지하고, null 전송 시 값을 삭제합니다.",
                type = "string",
                nullable = true,
                example = "입장 무료")
        JsonNode memo) {}
