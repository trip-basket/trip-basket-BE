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
import dev.jino.tripbasketnew.room.dto.CreateRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.IssueInviteCodeResponseDto;
import dev.jino.tripbasketnew.room.dto.RoomResponseDto;
import dev.jino.tripbasketnew.room.dto.UpdateRoomRequestDto;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;
import dev.jino.tripbasketnew.room.repository.RoomMemberRepository;
import dev.jino.tripbasketnew.room.repository.RoomRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final UUID OWNER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MEMBER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private RoomAccessPolicy roomAccessPolicy;

    @Mock
    private MemberRepository memberRepository;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository, roomMemberRepository, roomAccessPolicy, memberRepository);
    }

    @Test
    void createRoom_savesRoomAndOwnerMembership() {
        Member owner = Member.builder()
                .id(OWNER_ID)
                .email("owner@test.com")
                .nickname("owner")
                .build();
        CreateRoomRequestDto request =
                new CreateRoomRequestDto("런던 여행", LocalDate.of(2026, 3, 16), LocalDate.of(2026, 3, 29));
        when(memberRepository.findById(OWNER_ID)).thenReturn(java.util.Optional.of(owner));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomMemberRepository.save(any(RoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponseDto response = roomService.createRoom(request, OWNER_ID);

        assertThat(response.name()).isEqualTo("런던 여행");
        assertThat(response.tripStartDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(response.tripEndDate()).isEqualTo(LocalDate.of(2026, 3, 29));
        verify(roomMemberRepository).save(any(RoomMember.class));
    }

    @Test
    void createRoom_throwsWhenTripPeriodIsInvalid() {
        CreateRoomRequestDto request =
                new CreateRoomRequestDto("런던 여행", LocalDate.of(2026, 3, 29), LocalDate.of(2026, 3, 16));

        assertThatThrownBy(() -> roomService.createRoom(request, OWNER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_INVALID_TRIP_PERIOD);
    }

    @Test
    void getRoom_returnsParticipantRoom() {
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
        when(roomAccessPolicy.validateParticipantAccess(roomId, MEMBER_ID)).thenReturn(RoomMember.member(room, member));

        RoomResponseDto response = roomService.getRoom(roomId, MEMBER_ID);

        assertThat(response.name()).isEqualTo("런던 여행");
    }

    @Test
    void updateRoom_mergesProvidedFields() {
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
        when(roomAccessPolicy.validateOwnerAccess(roomId, OWNER_ID)).thenReturn(RoomMember.owner(room, owner));

        RoomResponseDto response =
                roomService.updateRoom(roomId, new UpdateRoomRequestDto("런던 여행 수정", null, null), OWNER_ID);

        assertThat(response.name()).isEqualTo("런던 여행 수정");
        assertThat(response.tripStartDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(response.tripEndDate()).isEqualTo(LocalDate.of(2026, 3, 29));
    }

    @Test
    void deleteRoom_deletesExistingRoom() {
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
        when(roomAccessPolicy.validateOwnerAccess(roomId, OWNER_ID)).thenReturn(RoomMember.owner(room, owner));

        roomService.deleteRoom(roomId, OWNER_ID);

        verify(roomRepository).delete(room);
    }

    @Test
    void issueInviteCode_generatesNewCodeForOwner() {
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
        when(roomAccessPolicy.validateOwnerAccess(roomId, OWNER_ID)).thenReturn(RoomMember.owner(room, owner));

        IssueInviteCodeResponseDto response = roomService.issueInviteCode(roomId, OWNER_ID);

        assertThat(response.inviteCode()).hasSize(6);
        assertThat(response.issuedAt()).isNotNull();
    }
}
