package dev.jino.tripbasketnew.block.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

public record CreateBlockTodoRequestDto(
        @Pattern(regexp = ".*\\S.*", message = "text must not be blank")
        @Schema(description = "투두 내용", example = "오디오 가이드 대여")
        String text) {}
