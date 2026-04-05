package dev.jino.tripbasketnew.room.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;
import dev.jino.tripbasketnew.room.dto.CreateRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.IssueInviteCodeResponseDto;
import dev.jino.tripbasketnew.room.dto.MyRoomResponseDto;
import dev.jino.tripbasketnew.room.dto.RoomMemberResponseDto;
import dev.jino.tripbasketnew.room.dto.RoomResponseDto;
import dev.jino.tripbasketnew.room.dto.UpdateRoomRequestDto;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.entity.RoomRole;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;
import dev.jino.tripbasketnew.room.repository.RoomMemberRepository;
import dev.jino.tripbasketnew.room.repository.RoomRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
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
                new CreateRoomRequestDto("  런던 여행  ", LocalDate.of(2026, 3, 16), LocalDate.of(2026, 3, 29));
        RoomMember ownerMembership = RoomMember.owner(newUnsavedRoom("런던 여행"), owner);
        when(memberRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomMemberRepository.save(any(RoomMember.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomMemberRepository.findAllByRoom_IdOrderByCreatedAtAsc(null)).thenReturn(List.of(ownerMembership));

        RoomResponseDto response = roomService.createRoom(request, OWNER_ID);

        assertThat(response.name()).isEqualTo("런던 여행");
        assertThat(response.tripStartDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(response.tripEndDate()).isEqualTo(LocalDate.of(2026, 3, 29));
        assertThat(response.members()).hasSize(1);
        assertThat(response.members().get(0).role()).isEqualTo(RoomRole.OWNER);
        verify(roomMemberRepository).save(any(RoomMember.class));
    }

    @Test
    void getMyRooms_returnsParticipatingRoomsSortedByTripStartDate() {
        Member member = Member.builder()
                .id(MEMBER_ID)
                .email("member@test.com")
                .nickname("member")
                .build();
        List<MyRoomResponseDto> rooms = List.of(
                new MyRoomResponseDto(
                        UUID.fromString("33333333-3333-3333-3333-333333333333"),
                        "오사카 여행",
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 5),
                        dev.jino.tripbasketnew.room.entity.RoomRole.MEMBER,
                        LocalDateTime.of(2026, 3, 20, 12, 0),
                        4),
                new MyRoomResponseDto(
                        UUID.fromString("44444444-4444-4444-4444-444444444444"),
                        "도쿄 여행",
                        LocalDate.of(2026, 4, 10),
                        LocalDate.of(2026, 4, 12),
                        dev.jino.tripbasketnew.room.entity.RoomRole.OWNER,
                        LocalDateTime.of(2026, 3, 21, 12, 0),
                        2));
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.of(member));
        when(roomMemberRepository.findMyRooms(MEMBER_ID)).thenReturn(rooms);

        List<MyRoomResponseDto> response = roomService.getMyRooms(MEMBER_ID);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).name()).isEqualTo("오사카 여행");
        assertThat(response.get(0).memberCount()).isEqualTo(4);
        assertThat(response.get(1).role()).isEqualTo(dev.jino.tripbasketnew.room.entity.RoomRole.OWNER);
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
    void getMyRooms_throwsWhenMemberDoesNotExist() {
        when(memberRepository.findById(MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getMyRooms(MEMBER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    void getRoom_returnsParticipantRoom() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .id(roomId)
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
        when(roomMemberRepository.findAllByRoom_IdOrderByCreatedAtAsc(roomId)).thenReturn(List.of(roomMember));

        RoomResponseDto response = roomService.getRoom(roomId, MEMBER_ID);

        assertThat(response.name()).isEqualTo("런던 여행");
        assertThat(response.members())
                .containsExactly(new RoomMemberResponseDto(MEMBER_ID, "member", RoomRole.MEMBER, null));
    }

    @Test
    void updateRoom_mergesProvidedFields() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .id(roomId)
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .build();
        Member owner = Member.builder()
                .id(OWNER_ID)
                .email("owner@test.com")
                .nickname("owner")
                .build();
        RoomMember ownerMembership = RoomMember.owner(room, owner);
        when(roomAccessPolicy.validateOwnerAccess(roomId, OWNER_ID)).thenReturn(ownerMembership);
        when(roomMemberRepository.findAllByRoom_IdOrderByCreatedAtAsc(roomId)).thenReturn(List.of(ownerMembership));

        RoomResponseDto response =
                roomService.updateRoom(roomId, new UpdateRoomRequestDto("  런던 여행 수정  ", null, null), OWNER_ID);

        assertThat(response.name()).isEqualTo("런던 여행 수정");
        assertThat(response.tripStartDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(response.tripEndDate()).isEqualTo(LocalDate.of(2026, 3, 29));
        assertThat(response.members()).hasSize(1);
    }

    @Test
    void deleteRoom_deletesExistingRoom() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .id(roomId)
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .build();
        Member owner = Member.builder()
                .id(OWNER_ID)
                .email("owner@test.com")
                .nickname("owner")
                .build();
        RoomMember ownerMembership = RoomMember.owner(room, owner);
        when(roomAccessPolicy.validateOwnerAccess(roomId, OWNER_ID)).thenReturn(ownerMembership);
        when(roomMemberRepository.findAllByRoom_IdOrderByCreatedAtAsc(roomId)).thenReturn(List.of(ownerMembership));

        roomService.deleteRoom(roomId, OWNER_ID);

        InOrder deleteOrder = inOrder(roomMemberRepository, roomRepository);
        deleteOrder.verify(roomMemberRepository).deleteAll(List.of(ownerMembership));
        deleteOrder.verify(roomRepository).delete(room);
    }

    @Test
    void issueInviteCode_generatesNewCodeForOwner() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .id(roomId)
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

    private Room newUnsavedRoom(String name) {
        return Room.builder()
                .name(name)
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .build();
    }
}
