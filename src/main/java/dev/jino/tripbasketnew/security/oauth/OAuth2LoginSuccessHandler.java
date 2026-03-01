package dev.jino.tripbasketnew.security.oauth;

import dev.jino.tripbasketnew.security.config.AuthCookieProperties;
import dev.jino.tripbasketnew.security.jwt.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieProperties authCookieProperties;

    @PostConstruct
    void init() {
        setDefaultTargetUrl(authCookieProperties.redirectUri());
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws ServletException, IOException {
        String token = jwtTokenProvider.createToken(authentication);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
            .from(authCookieProperties.name(), token)
            .httpOnly(true)
            .secure(authCookieProperties.secure())
            .path(authCookieProperties.path())
            .sameSite(authCookieProperties.sameSite())
            .maxAge(Duration.ofSeconds(jwtTokenProvider.getExpirationSeconds()));

        if (StringUtils.hasText(authCookieProperties.domain())) {
            cookieBuilder.domain(authCookieProperties.domain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
