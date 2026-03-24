package dev.jino.tripbasketnew.member.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.dto.MyInfoResponseDto;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MyInfoResponseDto getMyInfo(UUID memberId) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return new MyInfoResponseDto(
                member.getId(), member.getEmail(), member.getNickname(), member.getCreatedAt(), member.getUpdatedAt());
    }
}
