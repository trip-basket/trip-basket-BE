package dev.jino.tripbasketnew.block.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.jino.tripbasketnew.block.dto.BlockResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.entity.BlockStatus;
import dev.jino.tripbasketnew.block.repository.BlockRepository;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.place.entity.PlaceOpeningHour;
import dev.jino.tripbasketnew.place.service.PlaceService;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private RoomAccessPolicy roomAccessPolicy;

    @Mock
    private PlaceService placeService;

    private BlockService blockService;

    @BeforeEach
    void setUp() {
        blockService = new BlockService(blockRepository, roomAccessPolicy, placeService);
    }

    @Test
    void createBlock_returnsResponseWithPlaceSummaryAndPlaceholders() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Place place = place();
        CreateBlockRequestDto request = new CreateBlockRequestDto(
                BlockStatus.SCHEDULED,
                "google-place-id",
                "대영박물관 관람",
                OffsetDateTime.of(2026, 4, 5, 10, 0, 0, 0, ZoneOffset.ofHours(9)),
                OffsetDateTime.of(2026, 4, 5, 11, 30, 0, 0, ZoneOffset.ofHours(9)));

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(placeService.getOrSyncPlace("google-place-id")).thenReturn(place);
        when(blockRepository.save(any(Block.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlockResponseDto response = blockService.createBlock(room.getId(), request, member.getId());

        assertThat(response.roomId()).isEqualTo(room.getId());
        assertThat(response.status()).isEqualTo(BlockStatus.SCHEDULED);
        assertThat(response.name()).isEqualTo("대영박물관 관람");
        assertThat(response.place().googlePlaceId()).isEqualTo("google-place-id");
        assertThat(response.place().placeName()).isEqualTo("대영박물관");
        assertThat(response.place().openingHours()).hasSize(2);
        assertThat(response.cost()).isNull();
        assertThat(response.memo()).isNull();
        assertThat(response.reactions()).isEmpty();
        assertThat(response.todos()).isEmpty();
        assertThat(response.addedBy()).isEqualTo(member.getId());
        assertThat(response.addedAt()).isNotNull();
    }

    private Room room() {
        return Room.builder()
                .id(UUID.randomUUID())
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 4, 5))
                .tripEndDate(LocalDate.of(2026, 4, 10))
                .build();
    }

    private Member member() {
        return Member.builder()
                .id(UUID.randomUUID())
                .email("jino@example.com")
                .nickname("jino")
                .build();
    }

    private Place place() {
        return Place.builder()
                .googlePlaceId("google-place-id")
                .placeName("대영박물관")
                .lat(51.5194)
                .lng(-0.1270)
                .category("attraction")
                .formattedAddress("Great Russell St, London WC1B 3DG")
                .rating(4.7)
                .reviewCount(120345)
                .priceLevel(0)
                .photoUrl("https://example.com/photo")
                .openingHours(List.of(
                        PlaceOpeningHour.of(0, LocalTime.of(10, 0), LocalTime.of(17, 0)),
                        PlaceOpeningHour.of(1, LocalTime.of(10, 0), LocalTime.of(20, 30))))
                .build();
    }
}
