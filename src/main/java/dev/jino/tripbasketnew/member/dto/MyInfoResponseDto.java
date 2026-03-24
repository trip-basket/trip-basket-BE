package dev.jino.tripbasketnew.member.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MyInfoResponseDto(
        UUID id, String email, String nickname, LocalDateTime createdAt, LocalDateTime updatedAt) {}
