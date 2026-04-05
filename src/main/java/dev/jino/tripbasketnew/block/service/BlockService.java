package dev.jino.tripbasketnew.block.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jino.tripbasketnew.block.dto.BlockPlaceResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockReactionResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockTodoResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.repository.BlockRepository;
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

    @Transactional
    public BlockResponseDto createBlock(UUID roomId, CreateBlockRequestDto request, UUID memberId) {
        RoomMember roomMember = roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        Place place = placeService.getOrSyncPlace(request.googlePlaceId());
        ZoneId zoneId = ZoneId.of(place.getTimezoneId());

        Block block = Block.create(
                roomMember.getRoom(),
                place,
                roomMember.getMember(),
                request.status(),
                request.name().trim(),
                toUtc(request.startTime(), zoneId),
                toUtc(request.endTime(), zoneId),
                place.getTimezoneId(),
                OffsetDateTime.now(ZoneOffset.UTC));

        return toResponse(blockRepository.save(block));
    }

    private BlockResponseDto toResponse(Block block) {
        return new BlockResponseDto(
                block.getId(),
                block.getRoom().getId(),
                block.getStatus(),
                toPlaceResponse(block.getPlace()),
                block.getName(),
                toLocalTime(block.getStartTime(), block.getTimezoneId()),
                toLocalTime(block.getEndTime(), block.getTimezoneId()),
                block.getTimezoneId(),
                null,
                null,
                block.getAddedBy().getId(),
                block.getAddedAt(),
                List.<BlockReactionResponseDto>of(),
                List.<BlockTodoResponseDto>of());
    }

    private BlockPlaceResponseDto toPlaceResponse(Place place) {
        return new BlockPlaceResponseDto(
                place.getGooglePlaceId(),
                place.getPlaceName(),
                place.getLat(),
                place.getLng(),
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
        return utcDateTime.atZoneSameInstant(ZoneId.of(timezoneId)).toLocalDateTime();
    }
}
