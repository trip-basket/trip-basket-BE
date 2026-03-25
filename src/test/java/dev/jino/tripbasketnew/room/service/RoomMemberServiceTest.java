package dev.jino.tripbasketnew.room.service;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;
import dev.jino.tripbasketnew.room.dto.JoinRoomResponseDto;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.entity.RoomRole;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;
import dev.jino.tripbasketnew.room.repository.RoomMemberRepository;
import dev.jino.tripbasketnew.room.repository.RoomRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomMemberServiceTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MEMBER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private RoomAccessPolicy roomAccessPolicy;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoomRepository roomRepository;

    private RoomMemberService roomMemberService;

    @BeforeEach
    void setUp() {
        roomMemberService =
                new RoomMemberService(roomMemberRepository, roomAccessPolicy, memberRepository, roomRepository);
    }

    @Test
    void joinRoom_createsMemberMembership() {
        Member member = Member.builder()
                .id(MEMBER_ID)
                .email("member@test.com")
                .nickname("member")
                .build();
        Room room = Room.builder()
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .inviteCode("A1B2C3D4")
                .build();

        when(memberRepository.findById(MEMBER_ID)).thenReturn(java.util.Optional.of(member));
        when(roomRepository.findByInviteCode("A1B2C3D4")).thenReturn(java.util.Optional.of(room));
        when(roomMemberRepository.existsByRoom_IdAndMember_Id(room.getId(), member.getId()))
                .thenReturn(false);
        when(roomMemberRepository.save(any(RoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JoinRoomResponseDto response = roomMemberService.joinRoom("A1B2C3D4", MEMBER_ID);

        assertThat(response.roomName()).isEqualTo("런던 여행");
        assertThat(response.role()).isEqualTo(RoomRole.MEMBER);
    }

    @Test
    void joinRoom_throwsWhenAlreadyJoined() {
        Member member = Member.builder()
                .id(MEMBER_ID)
                .email("member@test.com")
                .nickname("member")
                .build();
        Room room = Room.builder()
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .inviteCode("A1B2C3D4")
                .build();

        when(memberRepository.findById(MEMBER_ID)).thenReturn(java.util.Optional.of(member));
        when(roomRepository.findByInviteCode("A1B2C3D4")).thenReturn(java.util.Optional.of(room));
        when(roomMemberRepository.existsByRoom_IdAndMember_Id(room.getId(), member.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> roomMemberService.joinRoom("A1B2C3D4", MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_ALREADY_JOINED);
    }

    @Test
    void leaveRoom_throwsWhenOwnerTriesToLeave() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .build();
        Member owner = Member.builder()
                .id(OWNER_ID)
                .email("owner@test.com")
                .nickname("owner")
                .build();

        when(roomAccessPolicy.validateParticipantAccess(roomId, OWNER_ID)).thenReturn(RoomMember.owner(room, owner));

        assertThatThrownBy(() -> roomMemberService.leaveRoom(roomId, OWNER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_OWNER_CANNOT_LEAVE);
    }

    @Test
    void leaveRoom_deletesMemberMembership() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .build();
        Member member = Member.builder()
                .id(MEMBER_ID)
                .email("member@test.com")
                .nickname("member")
                .build();
        RoomMember roomMember = RoomMember.member(room, member);

        when(roomAccessPolicy.validateParticipantAccess(roomId, MEMBER_ID)).thenReturn(roomMember);

        roomMemberService.leaveRoom(roomId, MEMBER_ID);

        verify(roomMemberRepository).delete(roomMember);
    }
}
