package dev.jino.tripbasketnew.block.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record BlockReactionResponseDto(
        @Schema(description = "리액션 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "블록 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID blockId,

        @Schema(description = "리액션한 멤버 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID memberId,

        @Schema(description = "리액션 타입", example = "like") String type) {}
