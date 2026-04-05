package dev.jino.tripbasketnew.block.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.room.entity.Room;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlockTest {

    @Test
    void createScheduledBlock_succeeds() {
        OffsetDateTime startTime = OffsetDateTime.of(2026, 4, 5, 10, 0, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime endTime = OffsetDateTime.of(2026, 4, 5, 11, 30, 0, 0, ZoneOffset.ofHours(9));
        OffsetDateTime addedAt = OffsetDateTime.of(2026, 4, 5, 9, 30, 0, 0, ZoneOffset.UTC);

        Block block = Block.create(
                room(),
                place(),
                member(),
                BlockStatus.SCHEDULED,
                "대영박물관 관람",
                startTime,
                endTime,
                "Europe/London",
                addedAt);

        assertThat(block.getStatus()).isEqualTo(BlockStatus.SCHEDULED);
        assertThat(block.getName()).isEqualTo("대영박물관 관람");
        assertThat(block.getStartTime()).isEqualTo(startTime);
        assertThat(block.getEndTime()).isEqualTo(endTime);
        assertThat(block.getTimezoneId()).isEqualTo("Europe/London");
        assertThat(block.getAddedAt()).isEqualTo(addedAt);
    }

    @Test
    void createBucketBlock_succeedsWithoutSchedule() {
        Block block = Block.create(
                room(),
                place(),
                member(),
                BlockStatus.BUCKET,
                "대영박물관 관람",
                null,
                null,
                "Europe/London",
                OffsetDateTime.now(ZoneOffset.UTC));

        assertThat(block.getStatus()).isEqualTo(BlockStatus.BUCKET);
        assertThat(block.getStartTime()).isNull();
        assertThat(block.getEndTime()).isNull();
    }

    @Test
    void createScheduledBlock_throwsWhenTimeIsMissing() {
        assertThatThrownBy(() -> Block.create(
                        room(),
                        place(),
                        member(),
                        BlockStatus.SCHEDULED,
                        "대영박물관 관람",
                        OffsetDateTime.now(ZoneOffset.UTC),
                        null,
                        "Europe/London",
                        OffsetDateTime.now(ZoneOffset.UTC)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_SCHEDULE_REQUIRED);
    }

    @Test
    void createBucketBlock_throwsWhenScheduleProvided() {
        assertThatThrownBy(() -> Block.create(
                        room(),
                        place(),
                        member(),
                        BlockStatus.BUCKET,
                        "대영박물관 관람",
                        OffsetDateTime.now(ZoneOffset.UTC),
                        OffsetDateTime.now(ZoneOffset.UTC).plusHours(1),
                        "Europe/London",
                        OffsetDateTime.now(ZoneOffset.UTC)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_SCHEDULE_NOT_ALLOWED);
    }

    @Test
    void createScheduledBlock_throwsWhenEndTimeIsNotAfterStartTime() {
        OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);

        assertThatThrownBy(() -> Block.create(
                        room(),
                        place(),
                        member(),
                        BlockStatus.SCHEDULED,
                        "대영박물관 관람",
                        startTime,
                        startTime,
                        "Europe/London",
                        OffsetDateTime.now(ZoneOffset.UTC)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_INVALID_TIME_RANGE);
    }

    private Room room() {
        return Room.builder()
                .name("런던 여행")
                .tripStartDate(LocalDate.of(2026, 4, 5))
                .tripEndDate(LocalDate.of(2026, 4, 10))
                .build();
    }

    private Place place() {
        return Place.builder()
                .googlePlaceId("google-place-id")
                .placeName("대영박물관")
                .timezoneId("Europe/London")
                .build();
    }

    private Member member() {
        return Member.builder().email("test@example.com").nickname("jino").build();
    }
}
