package dev.jino.tripbasketnew.security.principal;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements OAuth2User, OAuth2AuthenticatedPrincipal, Principal {

    private final UUID memberId;
    private final String email;
    private final String name;
    private final Collection<? extends GrantedAuthority> authorities;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of(
                "memberId", memberId.toString(),
                "email", email,
                "name", name);
    }

    @Override
    public String getName() {
        return memberId.toString();
    }
}
