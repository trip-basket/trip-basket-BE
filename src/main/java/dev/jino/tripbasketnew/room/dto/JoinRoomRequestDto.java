package dev.jino.tripbasketnew.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record JoinRoomRequestDto(
        @Schema(description = "방 초대코드", example = "A2C3D4") @NotBlank(message = "inviteCode는 비어 있을 수 없습니다.")
        String inviteCode) {}
