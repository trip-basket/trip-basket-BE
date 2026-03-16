package dev.jino.tripbasketnew.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String debugInfo;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, "", null);
    }

    public BusinessException(ErrorCode errorCode, String debugInfo) {
        this(errorCode, debugInfo, null);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, "", cause);
    }

    // 모든 생성자는 이 생성자로 위임된다.
    public BusinessException(ErrorCode errorCode, String debugInfo, Throwable cause) {
        super(errorCode.getErrorMessage(), cause);
        this.errorCode = errorCode;
        this.debugInfo = debugInfo;
    }
}
