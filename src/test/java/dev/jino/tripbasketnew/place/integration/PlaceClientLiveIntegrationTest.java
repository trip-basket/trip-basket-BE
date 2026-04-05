package dev.jino.tripbasketnew.place.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.fasterxml.jackson.databind.JsonNode;

import dev.jino.tripbasketnew.place.client.PlaceClient;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceClientLiveIntegrationTest {

    private static final String GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/v1/places";
    private static final String GOOGLE_TIMEZONE_BASE_URL = "https://maps.googleapis.com/maps/api/timezone/json";
    private static final String DEFAULT_FIELD_MASK =
            "id,displayName,formattedAddress,location,regularOpeningHours,priceLevel,photos,primaryType,types,rating,userRatingCount";

    @Test
    @EnabledIfEnvironmentVariable(named = "GOOGLE_MAPS_API_KEY", matches = ".+")
    void fetchPlaceDetailAndTimezone_liveGoogleApi() {
        String apiKey = System.getenv("GOOGLE_MAPS_API_KEY");
        String placeId = "ChIJt9trB0euEmsR8NbepO14j3M";
        PlaceClient placeClient = new PlaceClient(
                apiKey, GOOGLE_PLACES_BASE_URL, GOOGLE_TIMEZONE_BASE_URL, DEFAULT_FIELD_MASK, "ko", 800);

        JsonNode detail = placeClient.fetchPlaceDetail(placeId);
        JsonNode location = detail.path("location");
        double lat = location.path("latitude").asDouble();
        double lng = location.path("longitude").asDouble();
        String placeName = detail.path("displayName").path("text").asText();

        String timezoneId = placeClient.fetchTimeZoneId(lat, lng);

        System.out.println("Live Google API result");
        System.out.println("placeId=" + placeId);
        System.out.println("placeName=" + placeName);
        System.out.println("lat=" + lat);
        System.out.println("lng=" + lng);
        System.out.println("timezoneId=" + timezoneId);

        assertThat(detail.path("id").asText()).isEqualTo(placeId);
        assertThat(placeName).isNotBlank();
        assertThat(location.path("latitude").isNumber()).isTrue();
        assertThat(location.path("longitude").isNumber()).isTrue();
        assertThat(timezoneId).isNotBlank();
    }
}
