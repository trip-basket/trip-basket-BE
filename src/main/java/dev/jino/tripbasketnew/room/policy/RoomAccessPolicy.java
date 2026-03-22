package dev.jino.tripbasketnew.room.policy;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.member.repository.MemberRepository;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.entity.RoomRole;
import dev.jino.tripbasketnew.room.repository.RoomMemberRepository;
import dev.jino.tripbasketnew.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomAccessPolicy {

    private final RoomMemberRepository roomMemberRepository;
    private final MemberRepository memberRepository;
    private final RoomRepository roomRepository;

    public RoomMember validateParticipantAccess(UUID roomId, UUID memberId) {
        Member member = memberRepository
                .findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return roomMemberRepository
                .findByRoom_IdAndMember_Id(room.getId(), member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_ACCESS_DENIED));
    }

    public RoomMember validateOwnerAccess(UUID roomId, UUID memberId) {
        RoomMember roomMember = validateParticipantAccess(roomId, memberId);

        if (roomMember.getRole() != RoomRole.OWNER) {
            throw new BusinessException(ErrorCode.ROOM_ACCESS_DENIED);
        }

        return roomMember;
    }
}
