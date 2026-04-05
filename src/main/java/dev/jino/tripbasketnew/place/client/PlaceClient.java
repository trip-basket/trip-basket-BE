package dev.jino.tripbasketnew.place.client;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;

@Component
public class PlaceClient {

    private static final String GOOGLE_API_KEY_HEADER = "X-Goog-Api-Key";
    private static final String GOOGLE_FIELD_MASK_HEADER = "X-Goog-FieldMask";

    private final RestClient placesRestClient;
    private final RestClient timezoneRestClient;
    private final String apiKey;
    private final String fieldMask;
    private final String languageCode;
    private final int photoMaxHeightPx;

    public PlaceClient(
            @Value("${google.maps.api-key:}") String apiKey,
            @Value("${google.maps.places.base-url:https://places.googleapis.com/v1/places}") String placesBaseUrl,
            @Value("${google.maps.timezone.base-url:https://maps.googleapis.com/maps/api/timezone/json}")
                    String timezoneBaseUrl,
            @Value(
                            "${google.maps.places.field-mask:id,displayName,formattedAddress,location,regularOpeningHours,priceLevel,photos,primaryType,types,rating,userRatingCount}")
                    String fieldMask,
            @Value("${google.maps.places.language-code:ko}") String languageCode,
            @Value("${google.maps.places.photo-max-height-px:800}") int photoMaxHeightPx) {
        this.apiKey = apiKey;
        this.fieldMask = fieldMask;
        this.languageCode = languageCode;
        this.photoMaxHeightPx = photoMaxHeightPx;
        this.placesRestClient = RestClient.builder().baseUrl(placesBaseUrl).build();
        this.timezoneRestClient = RestClient.builder().baseUrl(timezoneBaseUrl).build();
    }

    public JsonNode fetchPlaceDetail(String placeId) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ErrorCode.PLACE_PROVIDER_NOT_CONFIGURED);
        }

        try {
            return placesRestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{placeId}")
                            .queryParam("languageCode", languageCode)
                            .build(placeId))
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .header(GOOGLE_API_KEY_HEADER, apiKey)
                    .header(GOOGLE_FIELD_MASK_HEADER, fieldMask)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND, ex);
        } catch (HttpClientErrorException.BadRequest ex) {
            throw new BusinessException(ErrorCode.PLACE_INVALID_ID, ex);
        } catch (RestClientResponseException ex) {
            throw new BusinessException(ErrorCode.PLACE_PROVIDER_ERROR, ex);
        }
    }

    public String fetchTimeZoneId(Double lat, Double lng) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ErrorCode.PLACE_PROVIDER_NOT_CONFIGURED);
        }

        if (lat == null || lng == null) {
            throw new BusinessException(ErrorCode.PLACE_TIMEZONE_UNAVAILABLE);
        }

        try {
            JsonNode response = timezoneRestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("location", lat + "," + lng)
                            .queryParam("timestamp", Instant.now().getEpochSecond())
                            .queryParam("key", apiKey)
                            .build())
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(JsonNode.class);

            String status = textOrNull(response, "status");
            String timeZoneId = textOrNull(response, "timeZoneId");

            if (!"OK".equals(status) || !StringUtils.hasText(timeZoneId)) {
                throw new BusinessException(ErrorCode.PLACE_TIMEZONE_UNAVAILABLE, status);
            }

            return timeZoneId;
        } catch (RestClientResponseException ex) {
            throw new BusinessException(ErrorCode.PLACE_PROVIDER_ERROR, ex);
        }
    }

    public String buildPhotoMediaUrl(String photoName) {
        if (!StringUtils.hasText(photoName) || !StringUtils.hasText(apiKey)) {
            return null;
        }
        return "https://places.googleapis.com/v1/" + photoName
                + "/media?maxHeightPx=" + photoMaxHeightPx
                + "&key=" + apiKey;
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
