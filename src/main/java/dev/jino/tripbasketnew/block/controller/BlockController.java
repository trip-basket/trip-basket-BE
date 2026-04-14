package dev.jino.tripbasketnew.block.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.block.controller.api.BlockApi;
import dev.jino.tripbasketnew.block.dto.BlockListResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockTodoResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockTodoRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockTodoRequestDto;
import dev.jino.tripbasketnew.block.service.BlockService;
import dev.jino.tripbasketnew.block.service.BlockTodoService;
import dev.jino.tripbasketnew.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BlockController implements BlockApi {

    private final BlockService blockService;
    private final BlockTodoService blockTodoService;

    @Override
    public ResponseEntity<BlockListResponseDto> getBlocks(UUID roomId, String status, UserPrincipal userPrincipal) {
        BlockListResponseDto response = blockService.getBlocks(roomId, status, userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BlockResponseDto> getBlock(UUID roomId, UUID blockId, UserPrincipal userPrincipal) {
        BlockResponseDto response = blockService.getBlock(roomId, blockId, userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BlockResponseDto> updateBlock(
            UUID roomId, UUID blockId, UpdateBlockRequestDto request, UserPrincipal userPrincipal) {
        BlockResponseDto response = blockService.updateBlock(roomId, blockId, request, userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteBlock(UUID roomId, UUID blockId, UserPrincipal userPrincipal) {
        blockService.deleteBlock(roomId, blockId, userPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BlockTodoResponseDto> createTodo(
            UUID roomId, UUID blockId, CreateBlockTodoRequestDto request, UserPrincipal userPrincipal) {
        BlockTodoResponseDto response =
                blockTodoService.createTodo(roomId, blockId, request, userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<BlockTodoResponseDto> updateTodo(
            UUID roomId, UUID blockId, UUID todoId, UpdateBlockTodoRequestDto request, UserPrincipal userPrincipal) {
        BlockTodoResponseDto response =
                blockTodoService.updateTodo(roomId, blockId, todoId, request, userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteTodo(UUID roomId, UUID blockId, UUID todoId, UserPrincipal userPrincipal) {
        blockTodoService.deleteTodo(roomId, blockId, todoId, userPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BlockResponseDto> createBlock(
            UUID roomId, CreateBlockRequestDto request, UserPrincipal userPrincipal) {
        BlockResponseDto response = blockService.createBlock(roomId, request, userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
