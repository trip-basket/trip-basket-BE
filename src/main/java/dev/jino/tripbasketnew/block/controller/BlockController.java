package dev.jino.tripbasketnew.block.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.block.controller.api.BlockApi;
import dev.jino.tripbasketnew.block.dto.BlockResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.service.BlockService;
import dev.jino.tripbasketnew.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BlockController implements BlockApi {

    private final BlockService blockService;

    @Override
    public ResponseEntity<BlockResponseDto> createBlock(
            UUID roomId, CreateBlockRequestDto request, UserPrincipal userPrincipal) {
        BlockResponseDto response = blockService.createBlock(roomId, request, userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
