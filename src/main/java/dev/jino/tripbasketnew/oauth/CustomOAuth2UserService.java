package dev.jino.tripbasketnew.oauth;

import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 유저 객체 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // OAuth porvider 이름 (google 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 사용자 식별 id 이름 가져오기 (sub 등)
        String userNameAttributeName = userRequest.getClientRegistration()
            .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 유저 세부 정보 가져오기
        OAuthAttributes attributes = OAuthAttributes.of(
            registrationId,
            userNameAttributeName,
            oAuth2User.getAttributes()
        );

        Member member = saveOrUpdate(attributes);

        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes.getAttributes(),
            attributes.getNameAttributeKey()
        );
    }

    @Transactional
    protected Member saveOrUpdate(OAuthAttributes attrs) {
        return memberRepository.findByEmail(attrs.getEmail())
            .orElseGet(() -> memberRepository.save(
                Member.builder()
                    .email(attrs.getEmail())
                    .nickname(attrs.getName())
                    .build()
            ));
    }

}
