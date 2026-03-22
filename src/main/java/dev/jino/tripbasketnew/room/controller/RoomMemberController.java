package dev.jino.tripbasketnew.room.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.room.dto.JoinRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.JoinRoomResponseDto;
import dev.jino.tripbasketnew.room.dto.RoomMemberResponseDto;
import dev.jino.tripbasketnew.room.service.RoomMemberService;
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
@Tag(name = "RoomMember", description = "여행 방 참여자 API")
public class RoomMemberController {

    private final RoomMemberService roomMemberService;

    @Operation(summary = "방 참여", description = "초대코드로 방에 참여합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "참여 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = JoinRoomResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "404", description = "유효하지 않은 초대코드", content = @Content),
        @ApiResponse(responseCode = "409", description = "이미 참여 중인 방", content = @Content)
    })
    @PostMapping("/join")
    public JoinRoomResponseDto joinRoom(@Valid @RequestBody JoinRoomRequestDto request, Authentication authentication) {
        return roomMemberService.joinRoom(request.inviteCode(), authentication.getName());
    }

    @Operation(summary = "참여자 목록 조회", description = "현재 방의 참여자 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = RoomMemberResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content)
    })
    @GetMapping("/{roomId}/members")
    public List<RoomMemberResponseDto> getRoomMembers(
            @Parameter(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable
                    UUID roomId,
            Authentication authentication) {
        return roomMemberService.getRoomMembers(roomId, authentication.getName());
    }

    @Operation(summary = "방 나가기", description = "현재 로그인한 사용자의 방 참여를 종료합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "나가기 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
        @ApiResponse(responseCode = "400", description = "방장은 나갈 수 없음", content = @Content),
        @ApiResponse(responseCode = "403", description = "방 접근 권한 없음", content = @Content)
    })
    @DeleteMapping("/{roomId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> leaveRoom(
            @Parameter(description = "방 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable
                    UUID roomId,
            Authentication authentication) {
        roomMemberService.leaveRoom(roomId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
