package dev.jino.tripbasketnew.block.dto;

import dev.jino.tripbasketnew.block.entity.BlockReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreateBlockReactionRequestDto(
        @NotNull(message = "type must not be null")
        @Schema(
                description = "리액션 타입",
                allowableValues = {"like"},
                example = "like")
        BlockReactionType type) {}
