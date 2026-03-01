package dev.jino.tripbasketnew.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PlaceDetailResponseDto(
    @Schema(description = "Google Place ID", example = "ChIJdd4hrwug2EcRmSrV3Vo6llI")
    String googlePlaceId,
    @Schema(description = "장소 이름", example = "대영박물관")
    String name,
    @Schema(description = "주소", example = "Great Russell St, London WC1B 3DG")
    String address,
    @Schema(description = "좌표 정보")
    Position position,
    @Schema(description = "영업 시간 목록")
    List<OpeningHour> openingHours,
    @Schema(description = "가격 레벨(0~4)", example = "2")
    Integer priceLevel,
    @Schema(description = "대표 사진 URL", example = "https://places.googleapis.com/v1/...")
    String photoUrl,
    @Schema(description = "카테고리", example = "museum")
    String category
) {

    public record Position(
        @Schema(description = "위도", example = "51.5194")
        Double lat,
        @Schema(description = "경도", example = "-0.1270")
        Double lng
    ) {
    }

    public record OpeningHour(
        @Schema(description = "요일(0=일요일, 1=월요일 ...)", example = "0")
        Integer day,
        @Schema(description = "오픈 시간(HH:mm)", example = "10:00")
        String open,
        @Schema(description = "마감 시간(HH:mm)", example = "17:00")
        String close
    ) {
    }
}
