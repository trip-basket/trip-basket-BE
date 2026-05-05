package dev.jino.tripbasketnew.block.service;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jino.tripbasketnew.block.dto.BlockReactionResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockReactionRequestDto;
import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.entity.BlockReaction;
import dev.jino.tripbasketnew.block.repository.BlockReactionRepository;
import dev.jino.tripbasketnew.block.repository.BlockRepository;
import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.room.entity.RoomMember;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockReactionService {

    private final BlockReactionRepository blockReactionRepository;
    private final BlockRepository blockRepository;
    private final RoomAccessPolicy roomAccessPolicy;

    public List<BlockReactionResponseDto> getReactionResponses(UUID blockId) {
        return blockReactionRepository.findAllByBlock_IdOrderByCreatedAtAsc(blockId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BlockReactionResponseDto createReaction(
            UUID roomId, UUID blockId, CreateBlockReactionRequestDto request, UUID memberId) {
        RoomMember roomMember = roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        Block block = findBlock(roomId, blockId);

        if (blockReactionRepository.existsByBlock_IdAndMember_IdAndType(blockId, memberId, request.type())) {
            throw new BusinessException(ErrorCode.BLOCK_REACTION_ALREADY_EXISTS);
        }

        try {
            BlockReaction reaction =
                    blockReactionRepository.save(BlockReaction.create(block, roomMember.getMember(), request.type()));
            return toResponse(reaction);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.BLOCK_REACTION_ALREADY_EXISTS, e);
        }
    }

    @Transactional
    public void deleteReaction(UUID roomId, UUID blockId, UUID reactionId, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        findBlock(roomId, blockId);
        BlockReaction reaction = findReaction(blockId, reactionId);

        if (!reaction.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.BLOCK_REACTION_DELETE_DENIED);
        }

        blockReactionRepository.delete(reaction);
    }

    @Transactional
    public void hardDeleteByBlockId(UUID blockId) {
        blockReactionRepository.deleteAllByBlock_Id(blockId);
    }

    private Block findBlock(UUID roomId, UUID blockId) {
        return blockRepository
                .findByIdAndRoom_Id(blockId, roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLOCK_NOT_FOUND));
    }

    private BlockReaction findReaction(UUID blockId, UUID reactionId) {
        return blockReactionRepository
                .findByIdAndBlock_Id(reactionId, blockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLOCK_REACTION_NOT_FOUND));
    }

    private BlockReactionResponseDto toResponse(BlockReaction reaction) {
        return new BlockReactionResponseDto(
                reaction.getId(),
                reaction.getBlock().getId(),
                reaction.getMember().getId(),
                reaction.getType().value());
    }
}
