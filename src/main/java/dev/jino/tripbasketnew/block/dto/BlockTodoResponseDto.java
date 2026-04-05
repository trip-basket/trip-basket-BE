package dev.jino.tripbasketnew.block.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record BlockTodoResponseDto(
        @Schema(description = "투두 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID id,

        @Schema(description = "투두 내용", example = "오디오 가이드 대여")
        String text,

        @Schema(description = "완료 여부", example = "false") boolean completed) {}
