package com.payauth.payauth.common.error;

import lombok.Getter;

/**
 * 비즈니스 로직 처리 중 발생하는 예외의 기본 클래스
 * ErrorCode를 기반으로 예외를 생성하며, 전역 예외 핸들러에서 일관된 응답으로 변환됨
 */
@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, null);
    }

    public BusinessException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, cause);
    }

    /**
     * 최종 생성자: 부모 클래스의 message에 detail을 포함시켜 로그 확인을 용이하게 함
     */
    public BusinessException(ErrorCode errorCode, String detail, Throwable cause) {
        super(formatMessage(errorCode, detail), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    private static String formatMessage(ErrorCode errorCode, String detail) {
        if (detail == null || detail.isBlank()) {
            return errorCode.getMessage();
        }
        return String.format("[%s] %s", errorCode.getMessage(), detail);
    }

    /**
     * 에러 코드의 HTTP 상태 코드 반환
     */
    public int getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    /**
     * 전체 에러 메시지 반환 (기본 메시지 + 상세 정보)
     */
    public String getFullMessage() {
        if (detail != null && !detail.isEmpty()) {
            return errorCode.getMessage() + " - " + detail;
        }
        return errorCode.getMessage();
    }
}
