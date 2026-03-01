package dev.jino.tripbasketnew.place.dto;

import java.util.List;

public record PlaceDetailResponseDto(
    String googlePlaceId,
    String name,
    String address,
    Position position,
    List<OpeningHour> openingHours,
    Integer priceLevel,
    String photoUrl,
    String category
) {

    public record Position(
        Double lat,
        Double lng
    ) {
    }

    public record OpeningHour(
        Integer day,
        String open,
        String close
    ) {
    }
}
