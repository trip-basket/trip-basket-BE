package dev.jino.tripbasketnew.security.oauth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberOAuthService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member saveOrUpdate(OAuthAttributes attrs) {
        return memberRepository
                .findByEmail(attrs.getEmail())
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email(attrs.getEmail())
                        .nickname(attrs.getName())
                        .build()));
    }
}
