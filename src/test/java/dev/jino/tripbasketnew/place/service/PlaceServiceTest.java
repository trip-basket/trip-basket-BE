package dev.jino.tripbasketnew.place.service;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jino.tripbasketnew.place.client.PlaceClient;
import dev.jino.tripbasketnew.place.dto.PlaceDetailResponseDto;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.place.repository.PlaceRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private PlaceClient placeClient;

    @Mock
    private PlaceRepository placeRepository;

    private PlaceService placeService;

    @BeforeEach
    void setUp() {
        placeService = new PlaceService(placeClient, placeRepository);
    }

    @Test
    void getPlaceDetail_mapsExtendedFields() throws Exception {
        when(placeClient.fetchPlaceDetail("google-place-id")).thenReturn(sampleDetail());
        when(placeClient.buildPhotoMediaUrl("photos/abc")).thenReturn("https://example.com/photo");

        PlaceDetailResponseDto response = placeService.getPlaceDetail("google-place-id");

        assertThat(response.googlePlaceId()).isEqualTo("google-place-id");
        assertThat(response.placeName()).isEqualTo("대영박물관");
        assertThat(response.formattedAddress()).isEqualTo("Great Russell St, London WC1B 3DG");
        assertThat(response.position().lat()).isEqualTo(51.5194);
        assertThat(response.position().lng()).isEqualTo(-0.1270);
        assertThat(response.rating()).isEqualTo(4.7);
        assertThat(response.reviewCount()).isEqualTo(120345);
        assertThat(response.openingHours()).hasSize(2);
        assertThat(response.photoUrl()).isEqualTo("https://example.com/photo");
    }

    @Test
    void getOrSyncPlace_updatesExistingPlaceByGooglePlaceId() throws Exception {
        Place existing = Place.builder()
                .id(UUID.randomUUID())
                .googlePlaceId("google-place-id")
                .placeName("이전 장소명")
                .lat(0.0)
                .lng(0.0)
                .build();
        when(placeClient.fetchPlaceDetail("google-place-id")).thenReturn(sampleDetail());
        when(placeClient.buildPhotoMediaUrl("photos/abc")).thenReturn("https://example.com/photo");
        when(placeClient.fetchTimeZoneId(51.5194, -0.1270)).thenReturn("Europe/London");
        when(placeRepository.findByGooglePlaceId("google-place-id")).thenReturn(Optional.of(existing));

        Place synced = placeService.getOrSyncPlace("google-place-id");

        assertThat(synced.getPlaceName()).isEqualTo("대영박물관");
        assertThat(synced.getFormattedAddress()).isEqualTo("Great Russell St, London WC1B 3DG");
        assertThat(synced.getRating()).isEqualTo(4.7);
        assertThat(synced.getReviewCount()).isEqualTo(120345);
        assertThat(synced.getTimezoneId()).isEqualTo("Europe/London");
        assertThat(synced.getOpeningHours()).extracting("day").containsExactly(0, 1);
    }

    @Test
    void getOrSyncPlace_savesNewPlaceWhenNotExists() throws Exception {
        when(placeClient.fetchPlaceDetail("google-place-id")).thenReturn(sampleDetail());
        when(placeClient.buildPhotoMediaUrl("photos/abc")).thenReturn("https://example.com/photo");
        when(placeClient.fetchTimeZoneId(51.5194, -0.1270)).thenReturn("Europe/London");
        when(placeRepository.findByGooglePlaceId("google-place-id")).thenReturn(Optional.empty());
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Place saved = placeService.getOrSyncPlace("google-place-id");

        assertThat(saved.getGooglePlaceId()).isEqualTo("google-place-id");
        assertThat(saved.getPlaceName()).isEqualTo("대영박물관");
        assertThat(saved.getPriceLevel()).isEqualTo(0);
        assertThat(saved.getPhotoUrl()).isEqualTo("https://example.com/photo");
        assertThat(saved.getTimezoneId()).isEqualTo("Europe/London");
        assertThat(saved.getOpeningHours()).hasSize(2);
        assertThat(saved.getOpeningHours().get(0).getOpenAt()).isEqualTo(LocalTime.of(10, 0));
    }

    private JsonNode sampleDetail() throws Exception {
        return OBJECT_MAPPER.readTree("""
                {
                  "id": "google-place-id",
                  "displayName": { "text": "대영박물관" },
                  "formattedAddress": "Great Russell St, London WC1B 3DG",
                  "location": { "latitude": 51.5194, "longitude": -0.1270 },
                  "regularOpeningHours": {
                    "periods": [
                      {
                        "open": { "day": 0, "hour": 10, "minute": 0 },
                        "close": { "day": 0, "hour": 17, "minute": 0 }
                      },
                      {
                        "open": { "day": 1, "hour": 10, "minute": 0 },
                        "close": { "day": 1, "hour": 20, "minute": 30 }
                      }
                    ]
                  },
                  "priceLevel": "PRICE_LEVEL_FREE",
                  "rating": 4.7,
                  "userRatingCount": 120345,
                  "photos": [{ "name": "photos/abc" }],
                  "primaryType": "attraction"
                }
                """);
    }
}
