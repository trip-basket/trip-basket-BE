package dev.jino.tripbasketnew.place.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaceClientTest {

    private static HttpServer server;
    private static String baseUrl;

    private PlaceClient placeClient;

    @BeforeAll
    static void setUpServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void tearDownServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void setUp() {
        clearContexts();
        placeClient = new PlaceClient(
                "test-api-key",
                baseUrl + "/places",
                baseUrl + "/timezone",
                "id,displayName,formattedAddress",
                "ko",
                800);
    }

    @Test
    void fetchPlaceDetail_sendsHeadersAndReturnsJson() {
        AtomicReference<String> requestPath = new AtomicReference<>();
        AtomicReference<String> fieldMaskHeader = new AtomicReference<>();
        AtomicReference<String> apiKeyHeader = new AtomicReference<>();
        server.createContext("/places/google-place-id", exchange -> {
            requestPath.set(exchange.getRequestURI().toString());
            fieldMaskHeader.set(exchange.getRequestHeaders().getFirst("X-Goog-FieldMask"));
            apiKeyHeader.set(exchange.getRequestHeaders().getFirst("X-Goog-Api-Key"));
            writeJson(exchange, 200, """
                    {
                      "id": "google-place-id",
                      "displayName": { "text": "대영박물관" }
                    }
                    """);
        });

        String placeName = placeClient
                .fetchPlaceDetail("google-place-id")
                .path("displayName")
                .path("text")
                .asText();

        assertThat(placeName).isEqualTo("대영박물관");
        assertThat(requestPath.get()).isEqualTo("/places/google-place-id?languageCode=ko");
        assertThat(fieldMaskHeader.get()).isEqualTo("id,displayName,formattedAddress");
        assertThat(apiKeyHeader.get()).isEqualTo("test-api-key");
    }

    @Test
    void fetchPlaceDetail_maps404ToBusinessException() {
        server.createContext("/places/missing-place", exchange -> writeJson(exchange, 404, """
                {
                  "error": {
                    "message": "not found"
                  }
                }
                """));

        assertThatThrownBy(() -> placeClient.fetchPlaceDetail("missing-place"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLACE_NOT_FOUND);
    }

    @Test
    void fetchTimeZoneId_returnsTimeZoneIdWhenStatusIsOk() {
        AtomicReference<String> query = new AtomicReference<>();
        server.createContext("/timezone", exchange -> {
            query.set(exchange.getRequestURI().getQuery());
            writeJson(exchange, 200, """
                    {
                      "status": "OK",
                      "timeZoneId": "Europe/London"
                    }
                    """);
        });

        String timezoneId = placeClient.fetchTimeZoneId(51.5194, -0.1270);

        assertThat(timezoneId).isEqualTo("Europe/London");
        assertThat(query.get()).contains("location=51.5194,-0.127");
        assertThat(query.get()).contains("key=test-api-key");
        assertThat(query.get()).contains("timestamp=");
    }

    @Test
    void fetchTimeZoneId_throwsWhenGoogleReturnsNonOkStatus() {
        server.createContext("/timezone", exchange -> writeJson(exchange, 200, """
                {
                  "status": "ZERO_RESULTS"
                }
                """));

        assertThatThrownBy(() -> placeClient.fetchTimeZoneId(51.5194, -0.1270))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLACE_TIMEZONE_UNAVAILABLE);
    }

    private static void clearContexts() {
        removeContextIfExists("/places/google-place-id");
        removeContextIfExists("/places/missing-place");
        removeContextIfExists("/timezone");
    }

    private static void removeContextIfExists(String path) {
        try {
            server.removeContext(path);
        } catch (IllegalArgumentException ignored) {
            // no-op
        }
    }

    private static void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
