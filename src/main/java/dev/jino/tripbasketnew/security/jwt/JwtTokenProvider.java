package dev.jino.tripbasketnew.security.jwt;

import dev.jino.tripbasketnew.security.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final int MIN_SECRET_BYTES = 32;

    private final JwtProperties jwtProperties;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        this.signingKey = createSigningKey();
    }

    public String createToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.expirationSeconds());

        List<String> roles = authentication.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .toList();

        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
            .subject(authentication.getName())
            .claim("roles", roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(getSigningKey(), Jwts.SIG.HS256);

        if (authentication.getPrincipal() instanceof OAuth2AuthenticatedPrincipal principal) {
            Object email = principal.getAttribute("email");
            Object name = principal.getAttribute("name");
            if (email instanceof String emailValue && !emailValue.isBlank()) {
                builder.claim("email", emailValue);
            }
            if (name instanceof String nameValue && !nameValue.isBlank()) {
                builder.claim("name", nameValue);
            }
        }

        return builder.compact();
    }

    public long getExpirationSeconds() {
        return jwtProperties.expirationSeconds();
    }

    private byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (RuntimeException ignored) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            signingKey = createSigningKey();
        }
        return signingKey;
    }

    private SecretKey createSigningKey() {
        byte[] keyBytes = decodeSecret(jwtProperties.secret());
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
