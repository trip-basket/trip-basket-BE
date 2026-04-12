package dev.jino.tripbasketnew.block.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BlockListPlaceResponseDto(
        @Schema(description = "Google Place ID", example = "ChIJdd4hrwug2EcRmSrV3Vo6llI")
        String placeId,

        @Schema(description = "장소 원본 이름", example = "대영박물관") String placeName,

        @Schema(description = "카테고리. 분류 정보가 없으면 null일 수 있습니다.", nullable = true, example = "attraction")
        String category) {}
