package dev.jino.tripbasketnew.room.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.room.dto.MyRoomResponseDto;
import dev.jino.tripbasketnew.room.entity.RoomMember;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, UUID> {

    Optional<RoomMember> findByRoom_IdAndMember_Id(UUID roomId, UUID memberId);

    boolean existsByRoom_IdAndMember_Id(UUID roomId, UUID memberId);

    List<RoomMember> findAllByRoom_IdOrderByCreatedAtAsc(UUID roomId);

    @Query("""
            select new dev.jino.tripbasketnew.room.dto.MyRoomResponseDto(
                roomMember.room.id,
                roomMember.room.name,
                roomMember.room.tripStartDate,
                roomMember.room.tripEndDate,
                roomMember.role,
                roomMember.createdAt,
                (select count(memberCountTarget) from RoomMember memberCountTarget where memberCountTarget.room.id = roomMember.room.id)
            )
            from RoomMember roomMember
            where roomMember.member.id = :memberId
            order by roomMember.room.tripStartDate asc, roomMember.createdAt asc
            """)
    List<MyRoomResponseDto> findMyRooms(UUID memberId);
}
