package dev.jino.tripbasketnew.member.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.dto.MyInfoResponseDto;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final UUID MEMBER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void getMyInfo_returnsMemberProfile() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 23, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 3, 23, 12, 30);
        Member member = Member.builder()
                .id(MEMBER_ID)
                .email("me@test.com")
                .nickname("jino")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));

        MyInfoResponseDto response = memberService.getMyInfo(MEMBER_ID);

        assertThat(response.id()).isEqualTo(MEMBER_ID);
        assertThat(response.email()).isEqualTo("me@test.com");
        assertThat(response.nickname()).isEqualTo("jino");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void getMyInfo_throwsWhenMemberDoesNotExist() {
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.getMyInfo(MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }
}
