package dev.jino.tripbasketnew.block.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateBlockTodoRequestDto(
        @Pattern(regexp = ".*\\S.*", message = "text must not be blank")
        @Size(max = 1000, message = "text must be at most 1000 characters")
        @Schema(description = "변경할 투두 내용. 미전송 시 기존 값을 유지합니다.", nullable = true, example = "오디오 가이드 대여")
        String text,

        @Schema(description = "변경할 완료 여부. 미전송 시 기존 값을 유지합니다.", nullable = true, example = "true")
        Boolean completed) {}
