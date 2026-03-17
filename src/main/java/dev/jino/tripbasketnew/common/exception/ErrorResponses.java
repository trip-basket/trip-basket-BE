package dev.jino.tripbasketnew.common.exception;

import java.net.URI;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class ErrorResponses {

    private ErrorResponses() {}

    /**
     * 기본 ProblemDetail을 생성
     *
     * @param status   상태 코드
     * @param detail   세부 정보
     * @param instance uri
     * @return problemDetail 객체
     */
    public static ProblemDetail of(HttpStatus status, String detail, URI instance) {
        return of(status, detail, instance, null);
    }

    /**
     * property가 포함된 ProblemDetail을 생성
     *
     * @param status   상태 코드
     * @param detail   세부 정보
     * @param instance uri
     * @param props    기타 정보
     * @return problemDetail 객체
     */
    public static ProblemDetail of(HttpStatus status, String detail, URI instance, Map<String, ?> props) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setInstance(instance);
        // RFC 9457의 권고에 따라 type이 `about:blank`일 때 title을 HTTP status phrase와 동일하게 지정
        pd.setTitle(status.getReasonPhrase());
        if (props != null) {
            props.forEach(pd::setProperty);
        }
        return pd;
    }

    /**
     * 비즈니스 예외를 위한 ProblemDetail을 생성.
     * ErrorCode의 name을 type으로 설정하여 클라이언트에서 오류 타입을 식별할 수 있도록 함.
     *
     * @param e        비즈니스 예외
     * @param instance uri
     * @return problemDetail 객체
     */
    public static ProblemDetail of(BusinessException e, URI instance) {
        return of(e, instance, null);
    }

    /**
     * 추가 property가 포함된 비즈니스 예외용 ProblemDetail을 생성.
     * debugInfo/cause는 로깅 전용이며 응답에는 노출하지 않음.
     *
     * @param e        비즈니스 예외
     * @param instance uri
     * @param props    기타 정보
     * @return problemDetail 객체
     */
    public static ProblemDetail of(BusinessException e, URI instance, Map<String, ?> props) {
        ErrorCode errorCode = e.getErrorCode();
        ProblemDetail pd = of(errorCode.getHttpStatus(), errorCode.getClientMessage(), instance, props);
        pd.setType(URI.create("urn:problem:" + errorCode.name().toLowerCase(Locale.ROOT)));
        return pd;
    }
}
