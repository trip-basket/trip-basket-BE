package dev.jino.tripbasketnew.room.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;
import dev.jino.tripbasketnew.room.dto.JoinRoomResponseDto;
import dev.jino.tripbasketnew.room.dto.RoomMemberResponseDto;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.entity.RoomRole;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;
import dev.jino.tripbasketnew.room.repository.RoomMemberRepository;
import dev.jino.tripbasketnew.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomMemberService {

    private final RoomMemberRepository roomMemberRepository;
    private final RoomAccessPolicy roomAccessPolicy;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;

    public List<RoomMemberResponseDto> getRoomMembers(UUID roomId, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);

        return roomMemberRepository.findAllByRoom_IdOrderByCreatedAtAsc(roomId).stream()
                .map(roomMember -> new RoomMemberResponseDto(
                        roomMember.getMember().getId(),
                        roomMember.getMember().getNickname(),
                        roomMember.getRole(),
                        roomMember.getCreatedAt()))
                .toList();
    }

    @Transactional
    public JoinRoomResponseDto joinRoom(String inviteCode, UUID memberId) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Room room = roomRepository
                .findByInviteCode(inviteCode.trim())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_INVITE_CODE_INVALID));

        if (roomMemberRepository.existsByRoom_IdAndMember_Id(room.getId(), member.getId())) {
            throw new BusinessException(ErrorCode.ROOM_ALREADY_JOINED);
        }

        RoomMember roomMember = roomMemberRepository.save(RoomMember.member(room, member));

        return new JoinRoomResponseDto(
                room.getId(),
                room.getName(),
                room.getTripStartDate(),
                room.getTripEndDate(),
                roomMember.getRole(),
                roomMember.getCreatedAt());
    }

    @Transactional
    public void leaveRoom(UUID roomId, UUID memberId) {
        RoomMember roomMember = roomAccessPolicy.validateParticipantAccess(roomId, memberId);

        if (roomMember.getRole() == RoomRole.OWNER) {
            throw new BusinessException(ErrorCode.ROOM_OWNER_CANNOT_LEAVE);
        }

        roomMemberRepository.delete(roomMember);
    }
}
