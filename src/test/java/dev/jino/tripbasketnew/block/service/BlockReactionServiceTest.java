package dev.jino.tripbasketnew.block.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import dev.jino.tripbasketnew.block.dto.CreateBlockReactionRequestDto;
import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.entity.BlockReaction;
import dev.jino.tripbasketnew.block.entity.BlockReactionType;
import dev.jino.tripbasketnew.block.entity.BlockStatus;
import dev.jino.tripbasketnew.block.repository.BlockReactionRepository;
import dev.jino.tripbasketnew.block.repository.BlockRepository;
import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.member.entity.Member;
import dev.jino.tripbasketnew.place.entity.Place;
import dev.jino.tripbasketnew.room.entity.Room;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockReactionServiceTest {

    @Mock
    private BlockReactionRepository blockReactionRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private RoomAccessPolicy roomAccessPolicy;

    private BlockReactionService blockReactionService;

    @BeforeEach
    void setUp() {
        blockReactionService = new BlockReactionService(blockReactionRepository, blockRepository, roomAccessPolicy);
    }

    @Test
    void getReactionResponses_returnsReactionsInRepositoryOrder() {
        Block block = block();
        Member firstMember = member();
        Member secondMember = member();
        BlockReaction first = BlockReaction.create(block, firstMember, BlockReactionType.LIKE);
        BlockReaction second = BlockReaction.create(block, secondMember, BlockReactionType.LIKE);

        when(blockReactionRepository.findAllByBlock_IdOrderByCreatedAtAsc(block.getId()))
                .thenReturn(List.of(first, second));

        var response = blockReactionService.getReactionResponses(block.getId());

        assertThat(response).extracting("memberId").containsExactly(firstMember.getId(), secondMember.getId());
    }

    @Test
    void createReaction_createsReactionWhenNotDuplicated() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockReactionRepository.existsByBlock_IdAndMember_IdAndType(
                        block.getId(), member.getId(), BlockReactionType.LIKE))
                .thenReturn(false);
        when(blockReactionRepository.save(any(BlockReaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = blockReactionService.createReaction(
                room.getId(), block.getId(), new CreateBlockReactionRequestDto(BlockReactionType.LIKE), member.getId());

        assertThat(response.blockId()).isEqualTo(block.getId());
        assertThat(response.memberId()).isEqualTo(member.getId());
        assertThat(response.type()).isEqualTo("like");
    }

    @Test
    void createReaction_throwsWhenDuplicateExists() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockReactionRepository.existsByBlock_IdAndMember_IdAndType(
                        block.getId(), member.getId(), BlockReactionType.LIKE))
                .thenReturn(true);

        assertThatThrownBy(() -> blockReactionService.createReaction(
                        room.getId(),
                        block.getId(),
                        new CreateBlockReactionRequestDto(BlockReactionType.LIKE),
                        member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_REACTION_ALREADY_EXISTS);
    }

    @Test
    void createReaction_throwsWhenDuplicateIsDetectedOnSave() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockReactionRepository.existsByBlock_IdAndMember_IdAndType(
                        block.getId(), member.getId(), BlockReactionType.LIKE))
                .thenReturn(false);
        when(blockReactionRepository.save(any(BlockReaction.class)))
                .thenThrow(new DataIntegrityViolationException("uk_block_reactions_block_member_type"));

        assertThatThrownBy(() -> blockReactionService.createReaction(
                        room.getId(),
                        block.getId(),
                        new CreateBlockReactionRequestDto(BlockReactionType.LIKE),
                        member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_REACTION_ALREADY_EXISTS);
    }

    @Test
    void deleteReaction_deletesOwnReaction() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);
        BlockReaction reaction = BlockReaction.create(block, member, BlockReactionType.LIKE);
        UUID reactionId = UUID.randomUUID();

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockReactionRepository.findByIdAndBlock_Id(reactionId, block.getId()))
                .thenReturn(Optional.of(reaction));

        blockReactionService.deleteReaction(room.getId(), block.getId(), reactionId, member.getId());

        verify(blockReactionRepository).delete(reaction);
    }

    @Test
    void deleteReaction_throwsWhenReactionBelongsToAnotherMember() {
        Room room = room();
        Member member = member();
        Member otherMember = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);
        BlockReaction reaction = BlockReaction.create(block, otherMember, BlockReactionType.LIKE);
        UUID reactionId = UUID.randomUUID();

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockReactionRepository.findByIdAndBlock_Id(reactionId, block.getId()))
                .thenReturn(Optional.of(reaction));

        assertThatThrownBy(() ->
                        blockReactionService.deleteReaction(room.getId(), block.getId(), reactionId, member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_REACTION_DELETE_DENIED);
    }

    @Test
    void hardDeleteByBlockId_deletesAllReactionsForBlock() {
        Block block = block();

        blockReactionService.hardDeleteByBlockId(block.getId());

        verify(blockReactionRepository).deleteAllByBlock_Id(block.getId());
    }

    @Test
    void deleteReaction_throwsWhenReactionDoesNotExist() {
        Room room = room();
        Member member = member();
        RoomMember roomMember = RoomMember.member(room, member);
        Block block = block(room, member);
        UUID missingReactionId = UUID.randomUUID();

        when(roomAccessPolicy.validateParticipantAccess(room.getId(), member.getId()))
                .thenReturn(roomMember);
        when(blockRepository.findByIdAndRoom_Id(block.getId(), room.getId())).thenReturn(Optional.of(block));
        when(blockReactionRepository.findByIdAndBlock_Id(missingReactionId, block.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> blockReactionService.deleteReaction(
                        room.getId(), block.getId(), missingReactionId, member.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.BLOCK_REACTION_NOT_FOUND);

        verify(blockReactionRepository, never()).delete(any(BlockReaction.class));
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
                .email(UUID.randomUUID() + "@example.com")
                .nickname("jino")
                .build();
    }

    private Block block() {
        return block(room(), member());
    }

    private Block block(Room room, Member member) {
        return Block.create(
                room,
                Place.builder()
                        .googlePlaceId("google-place-id")
                        .placeName("대영박물관")
                        .timezoneId("Europe/London")
                        .build(),
                member,
                BlockStatus.BUCKET,
                "후보",
                null,
                null,
                "Europe/London",
                java.time.OffsetDateTime.now());
    }
}
