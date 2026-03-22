package dev.jino.tripbasketnew.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Member 관련
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found", "해당 사용자를 찾을 수 없습니다."),

    // Room 관련
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Room not found", "해당 방을 찾을 수 없습니다."),
    ROOM_INVALID_TRIP_PERIOD(
            HttpStatus.BAD_REQUEST, "tripEndDate must not be before tripStartDate", "여행 종료일은 시작일보다 빠를 수 없습니다."),
    ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Room access denied", "해당 방에 접근할 권한이 없습니다."),
    ROOM_INVITE_CODE_INVALID(HttpStatus.NOT_FOUND, "Room invite code is invalid", "유효한 초대코드가 아닙니다."),
    ROOM_ALREADY_JOINED(HttpStatus.CONFLICT, "Room member already exists", "이미 참여 중인 방입니다."),
    ROOM_OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "Room owner cannot leave", "방장은 나갈 수 없습니다. 방 삭제를 이용해주세요."),

    // Place 관련
    PLACE_ID_BLANK(HttpStatus.BAD_REQUEST, "placeId must not be blank", "장소 ID는 비어 있을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
    private final String clientMessage;
}
