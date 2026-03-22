package dev.jino.tripbasketnew.room.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.jino.tripbasketnew.room.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {}
