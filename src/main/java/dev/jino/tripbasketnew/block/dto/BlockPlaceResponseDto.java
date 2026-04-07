package dev.jino.tripbasketnew.block.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record BlockPlaceResponseDto(
        @Schema(description = "Google Place ID", example = "ChIJdd4hrwug2EcRmSrV3Vo6llI")
        String googlePlaceId,

        @Schema(description = "장소 원본 이름", example = "대영박물관") String placeName,

        @Schema(description = "좌표 정보") Position position,

        @Schema(description = "카테고리. 분류 정보가 없으면 null일 수 있습니다.", nullable = true, example = "attraction")
        String category,

        @Schema(description = "포맷팅된 주소", example = "Great Russell St, London WC1B 3DG")
        String formattedAddress,

        @Schema(description = "평점. 제공되지 않으면 null일 수 있습니다.", nullable = true, example = "4.7")
        Double rating,

        @Schema(description = "리뷰 수. 제공되지 않으면 null일 수 있습니다.", nullable = true, example = "120345")
        Integer reviewCount,

        @Schema(description = "영업 시간 목록. 정보가 없으면 빈 배열로 반환됩니다.")
        List<OpeningHour> openingHours,

        @Schema(description = "가격 레벨(0~4). 제공되지 않으면 null일 수 있습니다.", nullable = true, example = "0")
        Integer priceLevel,

        @Schema(
                description = "대표 사진 URL. 사진 정보가 없으면 null일 수 있습니다.",
                nullable = true,
                example = "https://places.googleapis.com/v1/...")
        String photoUrl) {

    public record Position(
            @Schema(description = "위도", example = "51.5194") Double lat,
            @Schema(description = "경도", example = "-0.1270") Double lng) {}

    public record OpeningHour(
            @Schema(description = "요일(0=일요일, 1=월요일 ...)", example = "0")
            Integer day,

            @Schema(description = "오픈 시간(HH:mm)", example = "10:00")
            String open,

            @Schema(description = "마감 시간(HH:mm)", example = "17:00")
            String close) {}
}
