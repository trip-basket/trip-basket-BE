package dev.jino.tripbasketnew.common.exception;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private URI instance(HttpServletRequest req) {
        return URI.create(req.getRequestURI());
    }

    private void logSystemClientError(HttpStatus status, Exception e, HttpServletRequest req) {
        String message = Optional.ofNullable(e.getMessage()).orElse("-");
        log.warn(
                "[SYS-{} {}] ex={} | msg={} | path={}",
                status.value(),
                status.getReasonPhrase(),
                e.getClass().getSimpleName(),
                message,
                req.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessException(BusinessException e, HttpServletRequest req) {

        ErrorCode errorCode = e.getErrorCode();
        String errorMessage = errorCode.getErrorMessage();
        HttpStatus httpStatus = errorCode.getHttpStatus();

        if (httpStatus.is5xxServerError()) {
            log.error(
                    "[BIZ-{} {}] msg={} | debugInfo={} | path={}",
                    httpStatus.value(),
                    errorCode,
                    errorMessage,
                    e.getDebugInfo(),
                    req.getRequestURI(),
                    e);
        } else {
            log.warn(
                    "[BIZ-{} {}] msg={} | debugInfo={} | path={}",
                    httpStatus.value(),
                    errorCode,
                    errorMessage,
                    e.getDebugInfo(),
                    req.getRequestURI());
        }

        return ErrorResponses.of(e, instance(req));
    }

    // 400: Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", Optional.ofNullable(fe.getDefaultMessage()).orElse("유효하지 않은 값입니다.")))
                .toList();

        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(
                HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다.", instance(req), Map.of("errors", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintValidation(ConstraintViolationException e, HttpServletRequest req) {
        List<Map<String, String>> violations = e.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "property", v.getPropertyPath().toString(),
                        "message", v.getMessage()))
                .toList();

        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(
                HttpStatus.BAD_REQUEST,
                "요청 파라미터가 유효하지 않습니다.",
                instance(req),
                Map.of("violations", violations));
    }

    // 400: 잘못된 요청
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다.", URI.create(req.getRequestURI()));
    }

    // 400: 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(HttpStatus.BAD_REQUEST, "파라미터 타입이 올바르지 않습니다: " + e.getName(), instance(req));
    }

    // 400: JSON 파싱 불가
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(HttpStatus.BAD_REQUEST, "요청 본문을 해석할 수 없습니다.", instance(req));
    }

    // 400: 필수 요청 파라미터가 누락된 경우
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParameter(MissingServletRequestParameterException e, HttpServletRequest req) {
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());

        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(HttpStatus.BAD_REQUEST, message, instance(req));
    }

    // 400: 필수 요청 쿠키가 누락된 경우
    @ExceptionHandler(MissingRequestCookieException.class)
    public ProblemDetail handleMissingRequestCookie(MissingRequestCookieException e, HttpServletRequest req) {
        String message = String.format("필수 쿠키 '%s'가 누락되었습니다.", e.getCookieName());

        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(HttpStatus.BAD_REQUEST, message, instance(req));
    }

    // 400: 필수 요청 헤더가 누락된 경우
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingRequestHeader(MissingRequestHeaderException e, HttpServletRequest req) {
        String message = String.format("필수 헤더 '%s'가 누락되었습니다.", e.getHeaderName());

        logSystemClientError(HttpStatus.BAD_REQUEST, e, req);

        return ErrorResponses.of(HttpStatus.BAD_REQUEST, message, instance(req));
    }

    // 401: 컨트롤러/서비스 단에서 발생하는 JWT 관련 예외 처리
    @ExceptionHandler(JwtException.class)
    public ProblemDetail handleJwtExceptionInController(JwtException e, HttpServletRequest req) {
        logSystemClientError(HttpStatus.UNAUTHORIZED, e, req);

        return ErrorResponses.of(HttpStatus.UNAUTHORIZED, "유효하지 않은 형식의 토큰입니다.", instance(req));
    }

    // 405/415 등: 스펙 위반
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class})
    public ProblemDetail handleMethodMedia(Exception e, HttpServletRequest req) {
        HttpStatus status = (e instanceof HttpRequestMethodNotSupportedException)
                ? HttpStatus.METHOD_NOT_ALLOWED
                : HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        logSystemClientError(status, e, req);

        String detail =
                (status == HttpStatus.METHOD_NOT_ALLOWED) ? "지원하지 않는 HTTP 메서드입니다." : "지원하지 않는 Content-Type 입니다.";
        return ErrorResponses.of(status, detail, instance(req));
    }

    // 500: 그외 모든 예외
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleFallback(Exception e, HttpServletRequest req) {
        log.error(
                "[SYS-500 INTERNAL_SERVER_ERROR] ex={} | msg={} | path={}",
                e.getClass().getSimpleName(),
                Optional.ofNullable(e.getMessage()).orElse("-"),
                req.getRequestURI(),
                e);

        return ErrorResponses.of(HttpStatus.INTERNAL_SERVER_ERROR, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", instance(req));
    }
}
