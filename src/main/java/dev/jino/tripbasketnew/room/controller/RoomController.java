package dev.jino.tripbasketnew.room.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.room.dto.CreateRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.IssueInviteCodeResponseDto;
import dev.jino.tripbasketnew.room.dto.RoomResponseDto;
import dev.jino.tripbasketnew.room.dto.UpdateRoomRequestDto;
import dev.jino.tripbasketnew.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
@Tag(name = "Room", description = "여행 협업 방 API")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "방 생성", description = "로그인된 사용자가 새 여행 방을 생성합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = RoomResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponseDto createRoom(@Valid @RequestBody CreateRoomRequestDto request, Authentication authentication) {
        return roomService.createRoom(request, authentication.getName());
    }

    @Operation(summary = "방 조회", description = "방 ID로 여행 방 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = RoomResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{roomId}")
    public RoomResponseDto getRoom(
            @Parameter(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable
                    UUID roomId,
            Authentication authentication) {
        return roomService.getRoom(roomId, authentication.getName());
    }

    @Operation(summary = "방 수정", description = "방 이름과 여행 기간을 부분 수정합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "수정 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = RoomResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음", content = @Content)
    })
    @PatchMapping("/{roomId}")
    public RoomResponseDto updateRoom(
            @Parameter(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable
                    UUID roomId,
            @RequestBody UpdateRoomRequestDto request,
            Authentication authentication) {
        return roomService.updateRoom(roomId, request, authentication.getName());
    }

    @Operation(summary = "방 삭제", description = "방을 소프트 딜리트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable
                    UUID roomId,
            Authentication authentication) {
        roomService.deleteRoom(roomId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "방 초대코드 발급", description = "방장이 초대코드를 생성하거나 재발급합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "발급 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = IssueInviteCodeResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방장 권한 필요", content = @Content),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음", content = @Content)
    })
    @PostMapping("/{roomId}/invite-code")
    public IssueInviteCodeResponseDto issueInviteCode(
            @Parameter(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable
                    UUID roomId,
            Authentication authentication) {
        return roomService.issueInviteCode(roomId, authentication.getName());
    }
}
