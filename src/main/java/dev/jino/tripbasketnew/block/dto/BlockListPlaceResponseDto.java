package dev.jino.tripbasketnew.block.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BlockListPlaceResponseDto(
        @Schema(description = "Google Place ID", example = "ChIJdd4hrwug2EcRmSrV3Vo6llI")
        String placeId,

        @Schema(description = "장소 원본 이름", example = "대영박물관") String placeName,

        @Schema(description = "위도", example = "51.5194") Double lat,

        @Schema(description = "경도", example = "-0.1270") Double lng) {}
