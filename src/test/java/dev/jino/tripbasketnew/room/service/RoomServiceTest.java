package dev.jino.tripbasketnew.room.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.room.dto.CreateRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.RoomResponseDto;
import dev.jino.tripbasketnew.room.dto.UpdateRoomRequestDto;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.repository.RoomRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository);
    }

    @Test
    void createRoom_savesRoomAndReturnsResponse() {
        CreateRoomRequestDto request =
                new CreateRoomRequestDto("런던 여행", LocalDate.of(2026, 3, 16), LocalDate.of(2026, 3, 29));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomResponseDto response = roomService.createRoom(request);

        assertThat(response.name()).isEqualTo("런던 여행");
        assertThat(response.tripStartDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(response.tripEndDate()).isEqualTo(LocalDate.of(2026, 3, 29));
    }

    @Test
    void createRoom_throwsWhenTripPeriodIsInvalid() {
        CreateRoomRequestDto request =
                new CreateRoomRequestDto("런던 여행", LocalDate.of(2026, 3, 29), LocalDate.of(2026, 3, 16));

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_INVALID_TRIP_PERIOD);
    }

    @Test
    void getRoom_throwsWhenRoomDoesNotExist() {
        UUID roomId = UUID.randomUUID();
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoom(roomId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }

    @Test
    void updateRoom_mergesProvidedFields() {
        UUID roomId = UUID.randomUUID();
        Room room = Room.builder()
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 3, 16))
                .tripEndDate(LocalDate.of(2026, 3, 29))
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        RoomResponseDto response = roomService.updateRoom(roomId, new UpdateRoomRequestDto("런던 여행 수정", null, null));

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
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        roomService.deleteRoom(roomId);

        verify(roomRepository).delete(room);
    }
}
