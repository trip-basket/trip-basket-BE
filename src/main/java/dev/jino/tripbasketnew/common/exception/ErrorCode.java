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

    // Block 관련
    BLOCK_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "status must not be null", "블록 상태는 필수입니다."),
    BLOCK_STATUS_INVALID(
            HttpStatus.BAD_REQUEST,
            "status must be one of [bucket, scheduled]",
            "지원하지 않는 블록 상태입니다. 허용값은 bucket, scheduled 입니다."),
    BLOCK_SCHEDULE_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "scheduled block requires startTime and endTime",
            "scheduled 상태에서는 시작 시간과 종료 시간이 필요합니다."),
    BLOCK_SCHEDULE_NOT_ALLOWED(
            HttpStatus.BAD_REQUEST,
            "bucket block must not include startTime and endTime",
            "bucket 상태에서는 시작 시간과 종료 시간을 보낼 수 없습니다."),
    BLOCK_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "endTime must be after startTime", "종료 시간은 시작 시간보다 뒤여야 합니다."),

    // Place 관련
    PLACE_ID_BLANK(HttpStatus.BAD_REQUEST, "placeId must not be blank", "장소 ID는 비어 있을 수 없습니다."),
    PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "Place not found", "해당 장소를 찾을 수 없습니다."),
    PLACE_INVALID_ID(HttpStatus.BAD_REQUEST, "Invalid placeId", "유효하지 않은 장소 ID입니다."),
    PLACE_TIMEZONE_UNAVAILABLE(
            HttpStatus.BAD_GATEWAY, "Failed to resolve timezone for place", "장소의 시간대 정보를 확인할 수 없습니다."),
    PLACE_PROVIDER_ERROR(
            HttpStatus.BAD_GATEWAY, "Failed to fetch place detail from Google", "장소 정보를 불러오는 중 오류가 발생했습니다."),
    PLACE_PROVIDER_NOT_CONFIGURED(
            HttpStatus.INTERNAL_SERVER_ERROR, "GOOGLE_MAPS_API_KEY is not configured", "장소 정보 연동 설정이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
    private final String clientMessage;
}
