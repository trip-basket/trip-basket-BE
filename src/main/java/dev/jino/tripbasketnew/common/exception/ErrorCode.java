package dev.jino.tripbasketnew.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Member 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found", "해당 사용자를 찾을 수 없습니다."),

    // Place 관련
    PLACE_ID_BLANK(HttpStatus.BAD_REQUEST, "placeId must not be blank", "장소 ID는 비어 있을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
    private final String clientMessage;
}
