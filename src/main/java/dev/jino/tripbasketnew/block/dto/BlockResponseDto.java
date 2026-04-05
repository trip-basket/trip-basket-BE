package dev.jino.tripbasketnew.block.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import dev.jino.tripbasketnew.block.entity.BlockStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record BlockResponseDto(
        @Schema(description = "블록 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID roomId,

        @Schema(
                description = "블록 상태",
                allowableValues = {"bucket", "scheduled"},
                example = "scheduled")
        BlockStatus status,

        @Schema(description = "장소 정보") BlockPlaceResponseDto place,

        @Schema(description = "사용자가 지정한 블록 표시명", example = "대영박물관 관람")
        String name,

        @Schema(description = "블록 시작 시각. timezoneId 기준 로컬 시간입니다.", example = "2024-03-16T10:00:00")
        LocalDateTime startTime,

        @Schema(description = "블록 종료 시각. timezoneId 기준 로컬 시간입니다.", example = "2024-03-16T11:30:00")
        LocalDateTime endTime,

        @Schema(description = "블록 시간대 ID", example = "Europe/London")
        String timezoneId,

        @Schema(
                description = "시작 시각 기준 UTC 오프셋(분). DST에 따라 endUtcOffsetMinutes와 다를 수 있습니다.",
                nullable = true,
                example = "60")
        Integer startUtcOffsetMinutes,

        @Schema(
                description = "종료 시각 기준 UTC 오프셋(분). DST에 따라 startUtcOffsetMinutes와 다를 수 있습니다.",
                nullable = true,
                example = "60")
        Integer endUtcOffsetMinutes,

        @Schema(description = "예상 또는 기록 비용. 아직 미구현 단계에서는 null일 수 있습니다.", nullable = true, example = "12.50")
        BigDecimal cost,

        @Schema(description = "메모. 아직 미구현 단계에서는 null일 수 있습니다.", nullable = true, example = "입장 무료, 기부금 환영")
        String memo,

        @Schema(description = "블록을 추가한 멤버 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID addedBy,

        @Schema(description = "블록 생성 시각", example = "2026-02-12T12:00:00Z")
        OffsetDateTime addedAt,

        @Schema(description = "리액션 목록. 아직 미구현 단계에서는 빈 배열일 수 있습니다.")
        List<BlockReactionResponseDto> reactions,

        @Schema(description = "투두 목록. 아직 미구현 단계에서는 빈 배열일 수 있습니다.")
        List<BlockTodoResponseDto> todos) {}
