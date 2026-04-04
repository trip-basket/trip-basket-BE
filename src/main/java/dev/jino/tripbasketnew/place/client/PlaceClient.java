package dev.jino.tripbasketnew.place.client;

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
            throw new BusinessException(ErrorCode.PLACE_PROVIDER_NOT_CONFIGURED);
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
            throw new BusinessException(ErrorCode.PLACE_NOT_FOUND, ex);
        } catch (HttpClientErrorException.BadRequest ex) {
            throw new BusinessException(ErrorCode.PLACE_INVALID_ID, ex);
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
}
