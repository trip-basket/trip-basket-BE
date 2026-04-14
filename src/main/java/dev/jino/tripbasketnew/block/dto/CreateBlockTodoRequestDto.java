package dev.jino.tripbasketnew.block.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBlockTodoRequestDto(
        @NotBlank(message = "text must not be blank")
        @Size(max = 1000, message = "text must be at most 1000 characters")
        @Schema(description = "투두 내용", example = "오디오 가이드 대여")
        String text) {}
