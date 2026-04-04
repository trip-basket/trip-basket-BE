package dev.jino.tripbasketnew.place.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.place.client.PlaceClient;
import dev.jino.tripbasketnew.place.dto.PlaceDetailResponseDto;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.place.entity.PlaceOpeningHour;
import dev.jino.tripbasketnew.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceClient placeClient;
    private final PlaceRepository placeRepository;

    public PlaceDetailResponseDto getPlaceDetail(String googlePlaceId) {
        ParsedPlaceDetail detail = fetchAndParse(googlePlaceId);
        return toResponse(detail);
    }

    public Place getOrSyncPlace(String googlePlaceId) {
        ParsedPlaceDetail detail = fetchAndParse(googlePlaceId);
        return placeRepository
                .findByGooglePlaceId(detail.googlePlaceId())
                .map(place -> {
                    place.updateDetails(
                            detail.placeName(),
                            detail.lat(),
                            detail.lng(),
                            detail.category(),
                            detail.formattedAddress(),
                            detail.rating(),
                            detail.reviewCount(),
                            detail.priceLevel(),
                            detail.photoUrl(),
                            detail.openingHours());
                    return place;
                })
                .orElseGet(() -> placeRepository.save(Place.builder()
                        .googlePlaceId(detail.googlePlaceId())
                        .placeName(detail.placeName())
                        .lat(detail.lat())
                        .lng(detail.lng())
                        .category(detail.category())
                        .formattedAddress(detail.formattedAddress())
                        .rating(detail.rating())
                        .reviewCount(detail.reviewCount())
                        .priceLevel(detail.priceLevel())
                        .photoUrl(detail.photoUrl())
                        .openingHours(detail.openingHours())
                        .build()));
    }

    private ParsedPlaceDetail fetchAndParse(String googlePlaceId) {
        if (!StringUtils.hasText(googlePlaceId)) {
            throw new BusinessException(ErrorCode.PLACE_ID_BLANK);
        }

        JsonNode detail = placeClient.fetchPlaceDetail(googlePlaceId);
        JsonNode location = detail.path("location");
        String photoName = firstPhotoName(detail.path("photos"));

        return new ParsedPlaceDetail(
                textOrNull(detail, "id"),
                textOrNull(detail.path("displayName"), "text"),
                textOrNull(detail, "formattedAddress"),
                doubleOrNull(location, "latitude"),
                doubleOrNull(location, "longitude"),
                parseOpeningHourEntities(detail.path("regularOpeningHours").path("periods")),
                parsePriceLevel(detail.path("priceLevel")),
                doubleOrNull(detail, "rating"),
                intOrNull(detail, "userRatingCount"),
                placeClient.buildPhotoMediaUrl(photoName),
                parseCategory(detail));
    }

    private PlaceDetailResponseDto toResponse(ParsedPlaceDetail detail) {
        return new PlaceDetailResponseDto(
                detail.googlePlaceId(),
                detail.placeName(),
                detail.formattedAddress(),
                new PlaceDetailResponseDto.Position(detail.lat(), detail.lng()),
                toOpeningHourResponses(detail.openingHours()),
                detail.priceLevel(),
                detail.rating(),
                detail.reviewCount(),
                detail.photoUrl(),
                detail.category());
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private Double doubleOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asDouble();
    }

    private Integer intOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asInt();
    }

    private List<PlaceOpeningHour> parseOpeningHourEntities(JsonNode periodsNode) {
        List<PlaceOpeningHour> result = new ArrayList<>();
        if (periodsNode == null || !periodsNode.isArray()) {
            return result;
        }

        for (JsonNode period : periodsNode) {
            JsonNode openNode = period.path("open");
            JsonNode closeNode = period.path("close");
            Integer day = openNode.has("day") ? openNode.get("day").asInt() : null;
            result.add(PlaceOpeningHour.of(day, parseTime(openNode), parseTime(closeNode)));
        }
        return result;
    }

    private LocalTime parseTime(JsonNode timeNode) {
        if (timeNode == null || timeNode.isMissingNode() || !timeNode.has("hour") || !timeNode.has("minute")) {
            return null;
        }
        int hour = timeNode.get("hour").asInt();
        int minute = timeNode.get("minute").asInt();
        return LocalTime.of(hour, minute);
    }

    private List<PlaceDetailResponseDto.OpeningHour> toOpeningHourResponses(List<PlaceOpeningHour> openingHours) {
        return openingHours.stream()
                .map(openingHour -> new PlaceDetailResponseDto.OpeningHour(
                        openingHour.getDay(),
                        formatTime(openingHour.getOpenAt()),
                        formatTime(openingHour.getCloseAt())))
                .toList();
    }

    private String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    private Integer parsePriceLevel(JsonNode priceLevelNode) {
        if (priceLevelNode == null || priceLevelNode.isNull()) {
            return null;
        }
        String raw = priceLevelNode.asText();
        return switch (raw) {
            case "PRICE_LEVEL_FREE" -> 0;
            case "PRICE_LEVEL_INEXPENSIVE" -> 1;
            case "PRICE_LEVEL_MODERATE" -> 2;
            case "PRICE_LEVEL_EXPENSIVE" -> 3;
            case "PRICE_LEVEL_VERY_EXPENSIVE" -> 4;
            default -> null;
        };
    }

    private String firstPhotoName(JsonNode photosNode) {
        if (photosNode == null || !photosNode.isArray() || photosNode.isEmpty()) {
            return null;
        }
        return textOrNull(photosNode.get(0), "name");
    }

    private String parseCategory(JsonNode detailNode) {
        String primaryType = textOrNull(detailNode, "primaryType");
        if (primaryType != null) {
            return primaryType;
        }

        JsonNode types = detailNode.path("types");
        if (types.isArray() && !types.isEmpty()) {
            JsonNode first = types.get(0);
            if (!first.isNull()) {
                return first.asText();
            }
        }
        return null;
    }

    private record ParsedPlaceDetail(
            String googlePlaceId,
            String placeName,
            String formattedAddress,
            Double lat,
            Double lng,
            List<PlaceOpeningHour> openingHours,
            Integer priceLevel,
            Double rating,
            Integer reviewCount,
            String photoUrl,
            String category) {}
}
