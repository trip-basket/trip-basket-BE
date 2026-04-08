package dev.jino.tripbasketnew.block.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
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
import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.place.entity.PlaceOpeningHour;
import dev.jino.tripbasketnew.place.service.PlaceService;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
                LocalDateTime.of(2026, 4, 5, 10, 0),
                LocalDateTime.of(2026, 4, 5, 11, 30));

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(placeService.getOrSyncPlace("google-place-id")).thenReturn(place);
        when(blockRepository.save(any(Block.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlockResponseDto response = blockService.createBlock(room.getId(), request, member.getId());

        assertThat(response.roomId()).isEqualTo(room.getId());
        assertThat(response.status()).isEqualTo(BlockStatus.SCHEDULED);
        assertThat(response.name()).isEqualTo("대영박물관 관람");
        assertThat(response.startTime()).isEqualTo(LocalDateTime.of(2026, 4, 5, 10, 0));
        assertThat(response.endTime()).isEqualTo(LocalDateTime.of(2026, 4, 5, 11, 30));
        assertThat(response.timezoneId()).isEqualTo("Europe/London");
        assertThat(response.startUtcOffsetMinutes()).isEqualTo(60);
        assertThat(response.endUtcOffsetMinutes()).isEqualTo(60);
        assertThat(response.place().googlePlaceId()).isEqualTo("google-place-id");
        assertThat(response.place().placeName()).isEqualTo("대영박물관");
        assertThat(response.place().position().lat()).isEqualTo(51.5194);
        assertThat(response.place().position().lng()).isEqualTo(-0.1270);
        assertThat(response.place().openingHours()).hasSize(2);
        assertThat(response.cost()).isNull();
        assertThat(response.memo()).isNull();
        assertThat(response.reactions()).isEmpty();
        assertThat(response.todos()).isEmpty();
        assertThat(response.addedBy()).isEqualTo(member.getId());
        assertThat(response.addedAt()).isNotNull();
    }

    @Test
    void createBlock_throwsWhenPlaceTimezoneIsBlank() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Place place = place(null);
        CreateBlockRequestDto request = new CreateBlockRequestDto(
                BlockStatus.SCHEDULED,
                "google-place-id",
                "대영박물관 관람",
                LocalDateTime.of(2026, 4, 5, 10, 0),
                LocalDateTime.of(2026, 4, 5, 11, 30));

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(placeService.getOrSyncPlace("google-place-id")).thenReturn(place);

        assertThatThrownBy(() -> blockService.createBlock(room.getId(), request, member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLACE_TIMEZONE_UNAVAILABLE);
    }

    @Test
    void createBlock_throwsWhenPlaceTimezoneIsInvalid() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Place place = place("Invalid/Timezone");
        CreateBlockRequestDto request = new CreateBlockRequestDto(
                BlockStatus.SCHEDULED,
                "google-place-id",
                "대영박물관 관람",
                LocalDateTime.of(2026, 4, 5, 10, 0),
                LocalDateTime.of(2026, 4, 5, 11, 30));

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(placeService.getOrSyncPlace("google-place-id")).thenReturn(place);

        assertThatThrownBy(() -> blockService.createBlock(room.getId(), request, member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PLACE_TIMEZONE_UNAVAILABLE);
    }

    @Test
    void getBlocks_returnsScheduledFirstThenBucketsWithExpectedOrdering() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Place place = place();

        Block scheduledLater = block(
                room,
                place,
                member,
                BlockStatus.SCHEDULED,
                "점심",
                OffsetDateTime.of(2026, 4, 5, 4, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 5, 5, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 4, 10, 0, 0, 0, ZoneOffset.UTC));
        Block scheduledEarlier = block(
                room,
                place,
                member,
                BlockStatus.SCHEDULED,
                "박물관",
                OffsetDateTime.of(2026, 4, 5, 1, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 5, 2, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 4, 9, 0, 0, 0, ZoneOffset.UTC));
        Block bucketOlder = block(
                room,
                place,
                member,
                BlockStatus.BUCKET,
                "카페 후보",
                null,
                null,
                OffsetDateTime.of(2026, 4, 4, 8, 0, 0, 0, ZoneOffset.UTC));
        Block bucketNewer = block(
                room,
                place,
                member,
                BlockStatus.BUCKET,
                "저녁 후보",
                null,
                null,
                OffsetDateTime.of(2026, 4, 4, 11, 0, 0, 0, ZoneOffset.UTC));

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findAllByRoom_Id(room.getId()))
                .thenReturn(List.of(bucketOlder, scheduledLater, bucketNewer, scheduledEarlier));

        var response = blockService.getBlocks(room.getId(), null, member.getId());

        assertThat(response.blocks()).extracting("name").containsExactly("박물관", "점심", "저녁 후보", "카페 후보");
        assertThat(response.blocks())
                .extracting("status")
                .containsExactly(BlockStatus.SCHEDULED, BlockStatus.SCHEDULED, BlockStatus.BUCKET, BlockStatus.BUCKET);
        assertThat(response.blocks().get(0).place().placeId()).isEqualTo("google-place-id");
        assertThat(response.blocks().get(0).place().lat()).isEqualTo(51.5194);
        assertThat(response.blocks().get(0).reactions()).isEmpty();
    }

    @Test
    void getBlocks_filtersByStatusWhenQueryParamIsProvided() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Place place = place();
        Block scheduled = block(
                room,
                place,
                member,
                BlockStatus.SCHEDULED,
                "박물관",
                OffsetDateTime.of(2026, 4, 5, 1, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 5, 2, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 4, 9, 0, 0, 0, ZoneOffset.UTC));

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findAllByRoom_IdAndStatus(room.getId(), BlockStatus.SCHEDULED))
                .thenReturn(List.of(scheduled));

        var response = blockService.getBlocks(room.getId(), "scheduled", member.getId());

        assertThat(response.blocks()).hasSize(1);
        assertThat(response.blocks().get(0).status()).isEqualTo(BlockStatus.SCHEDULED);
        verify(blockRepository).findAllByRoom_IdAndStatus(room.getId(), BlockStatus.SCHEDULED);
    }

    @Test
    void getBlock_returnsDetailedBlockResponse() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Place place = place();
        Block block = block(
                room,
                place,
                member,
                BlockStatus.SCHEDULED,
                "대영박물관 관람",
                OffsetDateTime.of(2026, 4, 5, 1, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 5, 2, 30, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2026, 4, 4, 9, 0, 0, 0, ZoneOffset.UTC));

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));

        BlockResponseDto response = blockService.getBlock(room.getId(), block.getId(), member.getId());

        assertThat(response.id()).isEqualTo(block.getId());
        assertThat(response.roomId()).isEqualTo(room.getId());
        assertThat(response.status()).isEqualTo(BlockStatus.SCHEDULED);
        assertThat(response.place().googlePlaceId()).isEqualTo("google-place-id");
        assertThat(response.place().position().lat()).isEqualTo(51.5194);
        assertThat(response.place().rating()).isEqualTo(4.7);
        assertThat(response.memo()).isNull();
        assertThat(response.todos()).isEmpty();
        assertThat(response.reactions()).isEmpty();
    }

    @Test
    void getBlock_throwsWhenBlockDoesNotExistInRoom() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        UUID missingBlockId = UUID.randomUUID();

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(missingBlockId, room.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blockService.getBlock(room.getId(), missingBlockId, member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_NOT_FOUND);
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
        return place("Europe/London");
    }

    private Place place(String timezoneId) {
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
                .timezoneId(timezoneId)
                .openingHours(List.of(
                        PlaceOpeningHour.of(0, LocalTime.of(10, 0), LocalTime.of(17, 0)),
                        PlaceOpeningHour.of(1, LocalTime.of(10, 0), LocalTime.of(20, 30))))
                .build();
    }

    private Block block(
            Room room,
            Place place,
            Member member,
            BlockStatus status,
            String name,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            OffsetDateTime addedAt) {
        return Block.create(room, place, member, status, name, startTime, endTime, "Europe/London", addedAt);
    }
}
