package dev.jino.tripbasketnew.block.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.jino.tripbasketnew.block.dto.BlockTodoResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockTodoRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockTodoRequestDto;
import dev.jino.tripbasketnew.block.entity.Block;
import dev.jino.tripbasketnew.block.entity.BlockTodo;
import dev.jino.tripbasketnew.block.repository.BlockRepository;
import dev.jino.tripbasketnew.block.repository.BlockTodoRepository;
import dev.jino.tripbasketnew.common.exception.BusinessException;
import dev.jino.tripbasketnew.common.exception.ErrorCode;
import dev.jino.tripbasketnew.room.policy.RoomAccessPolicy;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockTodoService {

    private final BlockTodoRepository blockTodoRepository;
    private final BlockRepository blockRepository;
    private final RoomAccessPolicy roomAccessPolicy;

    public List<BlockTodoResponseDto> getTodoResponses(UUID blockId) {
        return blockTodoRepository.findAllByBlock_IdOrderByCreatedAtAsc(blockId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BlockTodoResponseDto createTodo(
            UUID roomId, UUID blockId, CreateBlockTodoRequestDto request, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        Block block = findBlock(roomId, blockId);

        BlockTodo todo = blockTodoRepository.save(BlockTodo.create(block, request.text()));
        return toResponse(todo);
    }

    @Transactional
    public BlockTodoResponseDto updateTodo(
            UUID roomId, UUID blockId, UUID todoId, UpdateBlockTodoRequestDto request, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        findBlock(roomId, blockId);
        BlockTodo todo = findTodo(blockId, todoId);

        if (request.text() != null) {
            todo.updateText(request.text());
        }
        if (request.completed() != null) {
            todo.updateCompleted(request.completed());
        }

        return toResponse(todo);
    }

    @Transactional
    public void deleteTodo(UUID roomId, UUID blockId, UUID todoId, UUID memberId) {
        roomAccessPolicy.validateParticipantAccess(roomId, memberId);
        findBlock(roomId, blockId);
        BlockTodo todo = findTodo(blockId, todoId);
        blockTodoRepository.delete(todo);
    }

    @Transactional
    public void softDeleteByBlockId(UUID blockId) {
        List<BlockTodo> todos = blockTodoRepository.findAllByBlock_Id(blockId);
        if (todos.isEmpty()) {
            return;
        }
        blockTodoRepository.deleteAll(todos);
    }

    private Block findBlock(UUID roomId, UUID blockId) {
        return blockRepository
                .findByIdAndRoom_Id(blockId, roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLOCK_NOT_FOUND));
    }

    private BlockTodo findTodo(UUID blockId, UUID todoId) {
        return blockTodoRepository
                .findByIdAndBlock_Id(todoId, blockId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BLOCK_TODO_NOT_FOUND));
    }

    private BlockTodoResponseDto toResponse(BlockTodo todo) {
        return new BlockTodoResponseDto(todo.getId(), todo.getText(), todo.isCompleted());
    }
}
