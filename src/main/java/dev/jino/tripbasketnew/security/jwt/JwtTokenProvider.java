package dev.jino.tripbasketnew.security.jwt;

import dev.jino.tripbasketnew.security.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        Collection<GrantedAuthority> authorities = extractAuthorities(claims.get("roles"));

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
    }

    private byte[] decodeSecret(String secret) {
        byte[] plainSecret = secret.getBytes(StandardCharsets.UTF_8);
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            return decoded.length >= MIN_SECRET_BYTES ? decoded : plainSecret;
        } catch (RuntimeException ignored) {
            return plainSecret;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Collection<GrantedAuthority> extractAuthorities(Object rolesClaim) {
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return List.of();
        }

        return roles.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
            .toList();
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
            keyBytes = sha256(keyBytes);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
