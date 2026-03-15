package dev.jino.tripbasketnew.place.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

import dev.jino.tripbasketnew.place.client.PlaceClient;
import dev.jino.tripbasketnew.place.dto.PlaceDetailResponseDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceClient placeClient;

    public PlaceDetailResponseDto getPlaceDetail(String placeId) {
        if (!StringUtils.hasText(placeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "placeId must not be blank");
        }

        JsonNode detail = placeClient.fetchPlaceDetail(placeId);
        String googlePlaceId = textOrNull(detail, "id");
        JsonNode location = detail.path("location");
        String photoName = firstPhotoName(detail.path("photos"));

        return new PlaceDetailResponseDto(
                googlePlaceId,
                textOrNull(detail.path("displayName"), "text"),
                textOrNull(detail, "formattedAddress"),
                new PlaceDetailResponseDto.Position(
                        doubleOrNull(location, "latitude"), doubleOrNull(location, "longitude")),
                parseOpeningHours(detail.path("regularOpeningHours").path("periods")),
                parsePriceLevel(detail.path("priceLevel")),
                placeClient.buildPhotoMediaUrl(photoName),
                parseCategory(detail));
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

    private List<PlaceDetailResponseDto.OpeningHour> parseOpeningHours(JsonNode periodsNode) {
        List<PlaceDetailResponseDto.OpeningHour> result = new ArrayList<>();
        if (periodsNode == null || !periodsNode.isArray()) {
            return result;
        }

        for (JsonNode period : periodsNode) {
            JsonNode openNode = period.path("open");
            JsonNode closeNode = period.path("close");
            Integer day = openNode.has("day") ? openNode.get("day").asInt() : null;
            String open = formatTime(openNode);
            String close = formatTime(closeNode);
            result.add(new PlaceDetailResponseDto.OpeningHour(day, open, close));
        }
        return result;
    }

    private String formatTime(JsonNode timeNode) {
        if (timeNode == null || timeNode.isMissingNode() || !timeNode.has("hour") || !timeNode.has("minute")) {
            return null;
        }
        int hour = timeNode.get("hour").asInt();
        int minute = timeNode.get("minute").asInt();
        return String.format("%02d:%02d", hour, minute);
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
}
