package dev.jino.tripbasketnew.room.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.room.dto.CreateRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.RoomResponseDto;
import dev.jino.tripbasketnew.room.dto.UpdateRoomRequestDto;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional
    public RoomResponseDto createRoom(CreateRoomRequestDto request) {
        validateTripPeriod(request.tripStartDate(), request.tripEndDate());

        Room room = Room.builder()
                .name(request.name().trim())
                .tripStartDate(request.tripStartDate())
                .tripEndDate(request.tripEndDate())
                .build();

        return toResponse(roomRepository.save(room));
    }

    public RoomResponseDto getRoom(UUID roomId) {
        return toResponse(getRoomEntity(roomId));
    }

    @Transactional
    public RoomResponseDto updateRoom(UUID roomId, UpdateRoomRequestDto request) {
        Room room = getRoomEntity(roomId);

        String name = StringUtils.hasText(request.name()) ? request.name().trim() : room.getName();
        LocalDate tripStartDate = request.tripStartDate() != null ? request.tripStartDate() : room.getTripStartDate();
        LocalDate tripEndDate = request.tripEndDate() != null ? request.tripEndDate() : room.getTripEndDate();

        validateTripPeriod(tripStartDate, tripEndDate);
        room.update(name, tripStartDate, tripEndDate);

        return toResponse(room);
    }

    @Transactional
    public void deleteRoom(UUID roomId) {
        Room room = getRoomEntity(roomId);
        // 추후 방 삭제 시 PlanItem, TravelSegment, Todo, Reaction 등 하위 자원도 함께 소프트 딜리트되어야 한다.
        roomRepository.delete(room);
    }

    private Room getRoomEntity(UUID roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
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
