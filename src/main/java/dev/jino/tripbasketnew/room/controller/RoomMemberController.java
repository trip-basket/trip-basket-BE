package dev.jino.tripbasketnew.room.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.room.controller.api.RoomMemberApi;
import dev.jino.tripbasketnew.room.dto.JoinRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.JoinRoomResponseDto;
import dev.jino.tripbasketnew.room.service.RoomMemberService;
import dev.jino.tripbasketnew.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoomMemberController implements RoomMemberApi {

    private final RoomMemberService roomMemberService;

    @Override
    public ResponseEntity<JoinRoomResponseDto> joinRoom(JoinRoomRequestDto request, UserPrincipal userPrincipal) {
        JoinRoomResponseDto response = roomMemberService.joinRoom(request.inviteCode(), userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> leaveRoom(UUID roomId, UserPrincipal userPrincipal) {
        roomMemberService.leaveRoom(roomId, userPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }
}
