package dev.jino.tripbasketnew.room.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.room.entity.RoomMember;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, UUID> {

    Optional<RoomMember> findByRoom_IdAndMember_Id(UUID roomId, UUID memberId);

    boolean existsByRoom_IdAndMember_Id(UUID roomId, UUID memberId);

    List<RoomMember> findAllByRoom_IdOrderByCreatedAtAsc(UUID roomId);
}
