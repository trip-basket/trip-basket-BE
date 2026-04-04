package dev.jino.tripbasketnew.room.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import dev.jino.tripbasketnew.room.controller.api.RoomApi;
import dev.jino.tripbasketnew.room.dto.CreateRoomRequestDto;
import dev.jino.tripbasketnew.room.dto.IssueInviteCodeResponseDto;
import dev.jino.tripbasketnew.room.dto.MyRoomResponseDto;
import dev.jino.tripbasketnew.room.dto.RoomResponseDto;
import dev.jino.tripbasketnew.room.dto.UpdateRoomRequestDto;
import dev.jino.tripbasketnew.room.service.RoomService;
import dev.jino.tripbasketnew.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoomController implements RoomApi {

    private final RoomService roomService;

    @Override
    public ResponseEntity<RoomResponseDto> createRoom(CreateRoomRequestDto request, UserPrincipal userPrincipal) {
        RoomResponseDto response = roomService.createRoom(request, userPrincipal.getMemberId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<RoomResponseDto> getRoom(UUID roomId, UserPrincipal userPrincipal) {
        RoomResponseDto response = roomService.getRoom(roomId, userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<RoomResponseDto> updateRoom(
            UUID roomId, UpdateRoomRequestDto request, UserPrincipal userPrincipal) {
        RoomResponseDto response = roomService.updateRoom(roomId, request, userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteRoom(UUID roomId, UserPrincipal userPrincipal) {
        roomService.deleteRoom(roomId, userPrincipal.getMemberId());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<IssueInviteCodeResponseDto> issueInviteCode(UUID roomId, UserPrincipal userPrincipal) {
        IssueInviteCodeResponseDto response = roomService.issueInviteCode(roomId, userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<MyRoomResponseDto>> getMyRooms(UserPrincipal userPrincipal) {
        List<MyRoomResponseDto> response = roomService.getMyRooms(userPrincipal.getMemberId());
        return ResponseEntity.ok(response);
    }
}
