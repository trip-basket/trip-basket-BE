package dev.jino.tripbasketnew.block.controller.api;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import dev.jino.tripbasketnew.block.dto.BlockListResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockReactionResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockResponseDto;
import dev.jino.tripbasketnew.block.dto.BlockTodoResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockReactionRequestDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockTodoRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockTodoRequestDto;
import dev.jino.tripbasketnew.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RequestMapping("/api/rooms/{roomId}/blocks")
@Tag(name = "Block", description = "여행 블록 API")
public interface BlockApi {

    @Operation(summary = "블록 목록 조회", description = "방 참여자가 블록 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = BlockListResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음", content = @Content)
    })
    @GetMapping
    ResponseEntity<BlockListResponseDto> getBlocks(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 상태 필터", example = "scheduled")
                    @RequestParam(value = "status", required = false)
                    String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 상세 조회", description = "방 참여자가 특정 블록의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = BlockResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방 또는 블록을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{blockId}")
    ResponseEntity<BlockResponseDto> getBlock(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 수정", description = "방 참여자가 블록 이름, 상태, 시간, 메모를 수정합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = BlockResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방 또는 블록을 찾을 수 없음", content = @Content)
    })
    @PatchMapping("/{blockId}")
    ResponseEntity<BlockResponseDto> updateBlock(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @Valid @RequestBody UpdateBlockRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 삭제", description = "방 참여자가 특정 블록을 soft delete 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방 또는 블록을 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{blockId}")
    ResponseEntity<Void> deleteBlock(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 투두 생성", description = "방 참여자가 블록에 투두를 추가합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = BlockTodoResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방 또는 블록을 찾을 수 없음", content = @Content)
    })
    @PostMapping("/{blockId}/todos")
    ResponseEntity<BlockTodoResponseDto> createTodo(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @Valid @RequestBody CreateBlockTodoRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 투두 수정", description = "방 참여자가 블록 투두의 내용 또는 완료 상태를 수정합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = BlockTodoResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방, 블록 또는 투두를 찾을 수 없음", content = @Content)
    })
    @PatchMapping("/{blockId}/todos/{todoId}")
    ResponseEntity<BlockTodoResponseDto> updateTodo(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @Parameter(description = "투두 ID") @PathVariable("todoId") UUID todoId,
            @Valid @RequestBody UpdateBlockTodoRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 투두 삭제", description = "방 참여자가 블록 투두를 soft delete 합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방, 블록 또는 투두를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{blockId}/todos/{todoId}")
    ResponseEntity<Void> deleteTodo(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @Parameter(description = "투두 ID") @PathVariable("todoId") UUID todoId,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 리액션 생성", description = "방 참여자가 블록에 리액션을 추가합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = BlockReactionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방 또는 블록을 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "409", description = "이미 같은 리액션이 존재함", content = @Content)
    })
    @PostMapping("/{blockId}/reactions")
    ResponseEntity<BlockReactionResponseDto> createReaction(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @Valid @RequestBody CreateBlockReactionRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 리액션 삭제", description = "방 참여자가 본인이 남긴 블록 리액션을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "본인 리액션만 삭제 가능", content = @Content),
        @ApiResponse(responseCode = "404", description = "방, 블록 또는 리액션을 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{blockId}/reactions/{reactionId}")
    ResponseEntity<Void> deleteReaction(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Parameter(description = "블록 ID") @PathVariable("blockId") UUID blockId,
            @Parameter(description = "리액션 ID") @PathVariable("reactionId") UUID reactionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal);

    @Operation(summary = "블록 생성", description = "방 참여자가 bucket 또는 scheduled 상태의 블록을 생성합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = BlockResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content),
        @ApiResponse(responseCode = "404", description = "방 또는 장소를 찾을 수 없음", content = @Content),
        @ApiResponse(responseCode = "502", description = "Google Places 연동 오류", content = @Content)
    })
    @PostMapping
    ResponseEntity<BlockResponseDto> createBlock(
            @Parameter(description = "방 ID") @PathVariable("roomId") UUID roomId,
            @Valid @RequestBody CreateBlockRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal);
}
