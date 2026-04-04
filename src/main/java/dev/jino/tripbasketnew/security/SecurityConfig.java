package dev.jino.tripbasketnew.security;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.jino.tripbasketnew.common.exception.ErrorResponses;
import dev.jino.tripbasketnew.security.config.AuthCookieProperties;
import dev.jino.tripbasketnew.security.config.JwtProperties;
import dev.jino.tripbasketnew.security.jwt.JwtAuthenticationFilter;
import dev.jino.tripbasketnew.security.oauth.CustomOAuth2UserService;
import dev.jino.tripbasketnew.security.oauth.OAuth2AuthorizationRequestCookieRepository;
import dev.jino.tripbasketnew.security.oauth.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, AuthCookieProperties.class})
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2AuthorizationRequestCookieRepository oAuth2AuthorizationRequestCookieRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthCookieProperties authCookieProperties;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:http://localhost:3000,https://test.luts.kr}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context.securityContextRepository(new NullSecurityContextRepository()))
                .requestCache(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                "/",
                                "/changelog",
                                "/error",
                                "/h2-console/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/oauth2/**",
                                "/login/oauth2/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            Object jwtAuthError = request.getAttribute("JWT_AUTH_ERROR");
                            String reason = (jwtAuthError != null) ? jwtAuthError.toString() : "MISSING_AUTHENTICATION";
                            String detail = (jwtAuthError != null) ? "유효하지 않은 형식의 토큰입니다." : "인증이 필요합니다.";
                            ProblemDetail pd = ErrorResponses.of(
                                    HttpStatus.UNAUTHORIZED, detail, URI.create(request.getRequestURI()));

                            logUnauthorized(reason, request.getRequestURI());
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            objectMapper.writeValue(response.getWriter(), pd);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ProblemDetail pd = ErrorResponses.of(
                                    HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", URI.create(request.getRequestURI()));

                            logForbidden(request.getRequestURI());
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                            objectMapper.writeValue(response.getWriter(), pd);
                        }))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2.authorizationEndpoint(
                                authorization -> authorization.authorizationRequestRepository(
                                        oAuth2AuthorizationRequestCookieRepository))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)))
                .logout(logout ->
                        logout.logoutSuccessUrl("/").deleteCookies(authCookieProperties.name(), "JSESSIONID"));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(2590000L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private void logUnauthorized(String reason, String path) {
        log.warn("[SYS-401 UNAUTHORIZED] auth=JWT | reason={} | path={}", reason, path);
    }

    private void logForbidden(String path) {
        log.warn("[SYS-403 FORBIDDEN] auth=JWT | path={}", path);
    }
}
