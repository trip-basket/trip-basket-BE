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
import dev.jino.tripbasketnew.block.dto.BlockResponseDto;
import dev.jino.tripbasketnew.block.dto.CreateBlockRequestDto;
import dev.jino.tripbasketnew.block.dto.UpdateBlockRequestDto;
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

    @Operation(summary = "블록 수정", description = "방 참여자가 블록 이름, 상태, 시간을 수정합니다.")
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
