package dev.jino.tripbasketnew.room.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomAccessPolicy roomAccessPolicy;
    private final MemberRepository memberRepository;

    @Transactional
    public RoomResponseDto createRoom(CreateRoomRequestDto request, String memberId) {
        validateTripPeriod(request.tripStartDate(), request.tripEndDate());
        Member member = memberRepository
                .findById(UUID.fromString(memberId))
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Room room = Room.builder()
                .name(request.name().trim())
                .tripStartDate(request.tripStartDate())
                .tripEndDate(request.tripEndDate())
                .build();

        Room savedRoom = roomRepository.save(room);
        roomMemberRepository.save(RoomMember.owner(savedRoom, member));

        return toResponse(savedRoom);
    }

    public RoomResponseDto getRoom(UUID roomId, String memberId) {
        return toResponse(
                roomAccessPolicy.validateParticipantAccess(roomId, memberId).getRoom());
    }

    @Transactional
    public RoomResponseDto updateRoom(UUID roomId, UpdateRoomRequestDto request, String memberId) {
        Room room = roomAccessPolicy.validateOwnerAccess(roomId, memberId).getRoom();

        String name = StringUtils.hasText(request.name()) ? request.name().trim() : room.getName();
        LocalDate tripStartDate = request.tripStartDate() != null ? request.tripStartDate() : room.getTripStartDate();
        LocalDate tripEndDate = request.tripEndDate() != null ? request.tripEndDate() : room.getTripEndDate();

        validateTripPeriod(tripStartDate, tripEndDate);
        room.update(name, tripStartDate, tripEndDate);

        return toResponse(room);
    }

    @Transactional
    public void deleteRoom(UUID roomId, String memberId) {
        Room room = roomAccessPolicy.validateOwnerAccess(roomId, memberId).getRoom();
        // 추후 방 삭제 시 RoomMember, PlanItem, TravelSegment, Todo, Reaction 등 하위 자원도 함께 소프트 딜리트되어야 한다.
        roomRepository.delete(room);
    }

    @Transactional
    public IssueInviteCodeResponseDto issueInviteCode(UUID roomId, String memberId) {
        Room room = roomAccessPolicy.validateOwnerAccess(roomId, memberId).getRoom();
        String inviteCode = InviteCodeGenerator.generate();
        LocalDateTime issuedAt = LocalDateTime.now();
        room.issueInviteCode(inviteCode, issuedAt);

        return new IssueInviteCodeResponseDto(room.getId(), room.getInviteCode(), room.getInviteCodeIssuedAt());
    }

    private void validateTripPeriod(LocalDate tripStartDate, LocalDate tripEndDate) {
        if (tripEndDate.isBefore(tripStartDate)) {
            throw new BusinessException(ErrorCode.ROOM_INVALID_TRIP_PERIOD);
        }
    }

    private RoomResponseDto toResponse(Room room) {
        return new RoomResponseDto(
                room.getId(), room.getName(), room.getTripStartDate(), room.getTripEndDate(), room.getCreatedAt());
    }
}
