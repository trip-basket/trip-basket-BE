package dev.jino.tripbasketnew.block.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import dev.jino.tripbasketnew.common.entity.SoftDeletableEntity;
import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.room.entity.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blocks")
@SQLDelete(sql = "UPDATE blocks SET deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted = false")
public class Block extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "added_by_member_id", nullable = false)
    private Member addedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BlockStatus status;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @Column(name = "timezone_id", nullable = false)
    private String timezoneId;

    @Column(name = "added_at", nullable = false)
    private OffsetDateTime addedAt;

    public static Block create(
            Room room,
            Place place,
            Member addedBy,
            BlockStatus status,
            String name,
            OffsetDateTime startTime,
            OffsetDateTime endTime,
            String timezoneId,
            OffsetDateTime addedAt) {
        Block block = new Block();
        block.room = room;
        block.place = place;
        block.addedBy = addedBy;
        block.timezoneId = timezoneId;
        block.addedAt = addedAt;
        block.rename(name);
        block.changeSchedule(status, startTime, endTime);
        return block;
    }

    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.BLOCK_NAME_BLANK);
        }
        this.name = name;
    }

    public void changeSchedule(BlockStatus status, OffsetDateTime startTime, OffsetDateTime endTime) {
        validateSchedule(status, startTime, endTime);
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private void validateSchedule(BlockStatus status, OffsetDateTime startTime, OffsetDateTime endTime) {
        if (status == null) {
            throw new BusinessException(ErrorCode.BLOCK_STATUS_REQUIRED);
        }

        if (status == BlockStatus.SCHEDULED) {
            if (startTime == null || endTime == null) {
                throw new BusinessException(ErrorCode.BLOCK_SCHEDULE_REQUIRED);
            }
            if (!endTime.isAfter(startTime)) {
                throw new BusinessException(ErrorCode.BLOCK_INVALID_TIME_RANGE);
            }
            return;
        }

        if (startTime != null || endTime != null) {
            throw new BusinessException(ErrorCode.BLOCK_SCHEDULE_NOT_ALLOWED);
        }
    }
}
