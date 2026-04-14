package dev.jino.tripbasketnew.block.service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import dev.jino.tripbasketnew.block.dto.BlockListItemResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockListPlaceResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockListResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockPlaceResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockReactionResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockTodoResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockRequestDto;
import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.entity.BlockStatus;
import dev.jino.tripbasketnew.block.repository.BlockRepository;
import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.place.entity.PlaceOpeningHour;
import dev.jino.tripbasketnew.place.service.PlaceService;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    private final BlockRepository blockRepository;
    private final RoomAccessPolicy roomAccessPolicy;
    private final PlaceService placeService;
    private final BlockTodoService blockTodoService;

    public BlockResponseDto getBlock(UUID roomId, UUID blockId, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);

        Block block = findBlock(roomId, blockId);

        return toResponse(block);
    }

    public BlockListResponseDto getBlocks(UUID roomId, String status, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);

        BlockStatus blockStatus = BlockStatus.from(status);
        List<Block> blocks = blockStatus == null
                ? blockRepository.findAllByRoom_Id(roomId)
                : blockRepository.findAllByRoom_IdAndStatus(roomId, blockStatus);

        List<BlockListItemResponseDto> items = blocks.stream()
                .sorted(blockComparator())
                .map(this::toListItemResponse)
                .toList();

        return new BlockListResponseDto(items);
    }

    @Transactional
    public BlockResponseDto createBlock(UUID roomId, CreateBlockRequestDto request, UUID memberId) {
        RoomMember roomMember = roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        Place place = placeService.getOrSyncPlace(request.googlePlaceId());
        ZoneId zoneId = resolveZoneId(place.getTimezoneId());

        Block block = Block.create(
                roomMember.getRoom(),
                place,
                roomMember.getMember(),
                request.status(),
                request.name(),
                toUtc(request.startTime(), zoneId),
                toUtc(request.endTime(), zoneId),
                place.getTimezoneId(),
                OffsetDateTime.now(ZoneOffset.UTC));

        return toResponse(blockRepository.save(block));
    }

    @Transactional
    public BlockResponseDto updateBlock(UUID roomId, UUID blockId, UpdateBlockRequestDto request, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        Block block = findBlock(roomId, blockId);

        if (request.name() != null) {
            block.rename(request.name());
        }
        if (request.memo() != null) {
            block.updateMemo(parseMemo(request.memo()));
        }

        BlockStatus targetStatus = request.status() != null ? request.status() : block.getStatus();
        ZoneId zoneId = resolveZoneId(block.getTimezoneId());

        if (targetStatus == BlockStatus.BUCKET) {
            if (request.startTime() != null || request.endTime() != null) {
                throw new BusinessException(ErrorCode.BLOCK_SCHEDULE_NOT_ALLOWED);
            }
            block.changeSchedule(BlockStatus.BUCKET, null, null);
            return toResponse(block);
        }

        OffsetDateTime targetStart =
                request.startTime() != null ? toUtc(request.startTime(), zoneId) : block.getStartTime();
        OffsetDateTime targetEnd = request.endTime() != null ? toUtc(request.endTime(), zoneId) : block.getEndTime();
        block.changeSchedule(BlockStatus.SCHEDULED, targetStart, targetEnd);
        return toResponse(block);
    }

    @Transactional
    public void deleteBlock(UUID roomId, UUID blockId, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        Block block = findBlock(roomId, blockId);
        blockTodoService.softDeleteByBlockId(block.getId());
        blockRepository.delete(block);
    }

    private Block findBlock(UUID roomId, UUID blockId) {
        return blockRepository
                .findByIdAndRoom_Id(blockId, roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLOCK_NOT_FOUND));
    }

    private BlockResponseDto toResponse(Block block) {
        List<BlockTodoResponseDto> todos = blockTodoService.getTodoResponses(block.getId());
        return new BlockResponseDto(
                block.getId(),
                block.getRoom().getId(),
                block.getStatus(),
                toPlaceResponse(block.getPlace()),
                block.getName(),
                toLocalTime(block.getStartTime(), block.getTimezoneId()),
                toLocalTime(block.getEndTime(), block.getTimezoneId()),
                block.getTimezoneId(),
                toOffsetMinutes(block.getStartTime(), block.getTimezoneId()),
                toOffsetMinutes(block.getEndTime(), block.getTimezoneId()),
                null,
                block.getMemo(),
                block.getAddedBy().getId(),
                block.getAddedAt(),
                List.<BlockReactionResponseDto>of(),
                todos);
    }

    private Comparator<Block> blockComparator() {
        return Comparator.comparingInt(this::statusSortOrder)
                .thenComparing(this::scheduledStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(this::scheduledAddedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(this::bucketAddedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int statusSortOrder(Block block) {
        return block.getStatus() == BlockStatus.SCHEDULED ? 0 : 1;
    }

    private OffsetDateTime scheduledStartTime(Block block) {
        return block.getStatus() == BlockStatus.SCHEDULED ? block.getStartTime() : null;
    }

    private OffsetDateTime scheduledAddedAt(Block block) {
        return block.getStatus() == BlockStatus.SCHEDULED ? block.getAddedAt() : null;
    }

    private OffsetDateTime bucketAddedAt(Block block) {
        return block.getStatus() == BlockStatus.BUCKET ? block.getAddedAt() : null;
    }

    private BlockListItemResponseDto toListItemResponse(Block block) {
        return new BlockListItemResponseDto(
                block.getId(),
                block.getRoom().getId(),
                block.getStatus(),
                new BlockListPlaceResponseDto(
                        block.getPlace().getGooglePlaceId(),
                        block.getPlace().getPlaceName(),
                        block.getPlace().getCategory()),
                block.getName(),
                toLocalTime(block.getStartTime(), block.getTimezoneId()),
                toLocalTime(block.getEndTime(), block.getTimezoneId()),
                block.getTimezoneId(),
                toOffsetMinutes(block.getStartTime(), block.getTimezoneId()),
                toOffsetMinutes(block.getEndTime(), block.getTimezoneId()),
                block.getAddedBy().getId(),
                block.getAddedAt(),
                List.<BlockReactionResponseDto>of());
    }

    private ZoneId resolveZoneId(String timezoneId) {
        if (!StringUtils.hasText(timezoneId)) {
            throw new BusinessException(ErrorCode.PLACE_TIMEZONE_UNAVAILABLE);
        }

        try {
            return ZoneId.of(timezoneId);
        } catch (DateTimeException exception) {
            throw new BusinessException(ErrorCode.PLACE_TIMEZONE_UNAVAILABLE);
        }
    }

    private BlockPlaceResponseDto toPlaceResponse(Place place) {
        return new BlockPlaceResponseDto(
                place.getGooglePlaceId(),
                place.getPlaceName(),
                new BlockPlaceResponseDto.Position(place.getLat(), place.getLng()),
                place.getCategory(),
                place.getFormattedAddress(),
                place.getRating(),
                place.getReviewCount(),
                toOpeningHours(place.getOpeningHours()),
                place.getPriceLevel(),
                place.getPhotoUrl());
    }

    private List<BlockPlaceResponseDto.OpeningHour> toOpeningHours(List<PlaceOpeningHour> openingHours) {
        return openingHours.stream()
                .map(openingHour -> new BlockPlaceResponseDto.OpeningHour(
                        openingHour.getDay(),
                        formatTime(openingHour.getOpenAt()),
                        formatTime(openingHour.getCloseAt())))
                .toList();
    }

    private String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    private String parseMemo(JsonNode memoNode) {
        if (memoNode == null || memoNode.isNull()) {
            return null;
        }
        return memoNode.asText();
    }

    private OffsetDateTime toUtc(LocalDateTime localDateTime, ZoneId zoneId) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(zoneId).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    }

    private LocalDateTime toLocalTime(OffsetDateTime utcDateTime, String timezoneId) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime.atZoneSameInstant(resolveZoneId(timezoneId)).toLocalDateTime();
    }

    private Integer toOffsetMinutes(OffsetDateTime utcDateTime, String timezoneId) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime
                        .atZoneSameInstant(resolveZoneId(timezoneId))
                        .getOffset()
                        .getTotalSeconds()
                / 60;
    }
}
