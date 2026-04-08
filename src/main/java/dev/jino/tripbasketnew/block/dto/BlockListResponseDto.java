package dev.jino.tripbasketnew.block.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record BlockListResponseDto(
        @Schema(description = "블록 목록") List<BlockListItemResponseDto> blocks) {}
