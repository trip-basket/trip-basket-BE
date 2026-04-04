package dev.jino.tripbasketnew.place.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class PlaceClient {

    private static final String GOOGLE_API_KEY_HEADER = "X-Goog-Api-Key";
    private static final String GOOGLE_FIELD_MASK_HEADER = "X-Goog-FieldMask";

    private final RestClient restClient;
    private final String apiKey;
    private final String fieldMask;
    private final String languageCode;
    private final int photoMaxHeightPx;

    public PlaceClient(
            @Value("${google.maps.api-key:}") String apiKey,
            @Value("${google.maps.places.base-url:https://places.googleapis.com/v1/places}") String baseUrl,
            @Value(
                            "${google.maps.places.field-mask:id,displayName,formattedAddress,location,regularOpeningHours,priceLevel,photos,primaryType,types,rating,userRatingCount}")
                    String fieldMask,
            @Value("${google.maps.places.language-code:ko}") String languageCode,
            @Value("${google.maps.places.photo-max-height-px:800}") int photoMaxHeightPx) {
        this.apiKey = apiKey;
        this.fieldMask = fieldMask;
        this.languageCode = languageCode;
        this.photoMaxHeightPx = photoMaxHeightPx;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public JsonNode fetchPlaceDetail(String placeId) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "GOOGLE_MAPS_API_KEY is not configured");
        }

        try {
            return restClient
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        } catch (HttpClientErrorException.BadRequest ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid placeId");
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch place detail from Google");
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
}
