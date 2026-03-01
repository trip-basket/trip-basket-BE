package dev.jino.tripbasketnew.security.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jino.tripbasketnew.security.config.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OAuth2AuthorizationRequestCookieRepository
    implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final Duration AUTH_REQUEST_COOKIE_TTL = Duration.ofMinutes(3);

    private final AuthCookieProperties authCookieProperties;
    private final ObjectMapper objectMapper;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Assert.notNull(request, "request cannot be null");

        String state = request.getParameter(OAuth2ParameterNames.STATE);
        if (!StringUtils.hasText(state)) {
            return null;
        }

        Cookie cookie = findCookie(request, AUTH_REQUEST_COOKIE_NAME);
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            return null;
        }

        OAuth2AuthorizationRequest authorizationRequest = deserialize(cookie.getValue());
        if (authorizationRequest == null) {
            return null;
        }

        return state.equals(authorizationRequest.getState()) ? authorizationRequest : null;
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");

        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }

        Assert.hasText(authorizationRequest.getState(), "authorizationRequest.state cannot be empty");
        addCookie(response, serialize(authorizationRequest), AUTH_REQUEST_COOKIE_TTL);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        Assert.notNull(request, "request cannot be null");
        Assert.notNull(response, "response cannot be null");

        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response);
        return authorizationRequest;
    }

    private Cookie findCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
            .filter(cookie -> name.equals(cookie.getName()))
            .findFirst()
            .orElse(null);
    }

    private void addCookie(HttpServletResponse response, String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(AUTH_REQUEST_COOKIE_NAME, value)
            .httpOnly(true)
            .secure(authCookieProperties.secure())
            .path(authCookieProperties.path())
            .sameSite(authCookieProperties.sameSite())
            .maxAge(maxAge);

        if (StringUtils.hasText(authCookieProperties.domain())) {
            builder.domain(authCookieProperties.domain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private void deleteCookie(HttpServletResponse response) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(AUTH_REQUEST_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(authCookieProperties.secure())
            .path(authCookieProperties.path())
            .sameSite(authCookieProperties.sameSite())
            .maxAge(Duration.ZERO);

        if (StringUtils.hasText(authCookieProperties.domain())) {
            builder.domain(authCookieProperties.domain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        OAuth2AuthorizationRequestPayload payload = OAuth2AuthorizationRequestPayload.from(authorizationRequest);
        try {
            byte[] serialized = objectMapper.writeValueAsBytes(payload);
            return Base64.getUrlEncoder().encodeToString(serialized);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize OAuth2AuthorizationRequest");
        }
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(value);
            OAuth2AuthorizationRequestPayload payload =
                objectMapper.readValue(decoded, OAuth2AuthorizationRequestPayload.class);
            return payload.toAuthorizationRequest();
        } catch (IOException e) {
            return null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private record OAuth2AuthorizationRequestPayload(
        String authorizationUri,
        String clientId,
        String redirectUri,
        Set<String> scopes,
        String state,
        String authorizationRequestUri,
        Map<String, Object> additionalParameters,
        Map<String, Object> attributes
    ) {

        static OAuth2AuthorizationRequestPayload from(OAuth2AuthorizationRequest request) {
            return new OAuth2AuthorizationRequestPayload(
                request.getAuthorizationUri(),
                request.getClientId(),
                request.getRedirectUri(),
                new LinkedHashSet<>(request.getScopes()),
                request.getState(),
                request.getAuthorizationRequestUri(),
                new LinkedHashMap<>(request.getAdditionalParameters()),
                new LinkedHashMap<>(request.getAttributes())
            );
        }

        OAuth2AuthorizationRequest toAuthorizationRequest() {
            if (!StringUtils.hasText(authorizationUri)
                || !StringUtils.hasText(clientId)
                || !StringUtils.hasText(redirectUri)
                || !StringUtils.hasText(state)) {
                throw new IllegalStateException("Invalid OAuth2 authorization request payload");
            }

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(authorizationUri)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .scopes(scopes == null ? Set.of() : scopes)
                .state(state);

            if (StringUtils.hasText(authorizationRequestUri)) {
                builder.authorizationRequestUri(authorizationRequestUri);
            }
            if (additionalParameters != null && !additionalParameters.isEmpty()) {
                builder.additionalParameters(parameters -> parameters.putAll(additionalParameters));
            }
            if (attributes != null && !attributes.isEmpty()) {
                builder.attributes(attrs -> attrs.putAll(attributes));
            }

            return builder.build();
        }
    }
}
