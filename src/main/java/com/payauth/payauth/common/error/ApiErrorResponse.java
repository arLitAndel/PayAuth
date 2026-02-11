package com.payauth.payauth.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * API 에러 응답 DTO
 * 클라이언트에게 일관된 형식의 에러 정보를 제공
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String detail;
    private final String path;
    private final List<FieldError> errors;

    private ApiErrorResponse(Builder builder) {
        this.timestamp = LocalDateTime.now();
        this.status = builder.status;
        this.code = builder.code;
        this.message = builder.message;
        this.detail = builder.detail;
        this.path = builder.path;
        this.errors = builder.errors;
    }

    /**
     * ErrorCode로부터 에러 응답 생성
     */
    public static ApiErrorResponse of(@NonNull ErrorCode errorCode) {
        return new Builder()
                .status(errorCode.getHttpStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    /**
     * ErrorCode와 요청 경로로 에러 응답 생성
     */
    public static ApiErrorResponse of(ErrorCode errorCode, String path) {
        return new Builder()
                .status(errorCode.getHttpStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .path(path)
                .build();
    }

    /**
     * ErrorCode, 상세 정보, 요청 경로로 에러 응답 생성
     */
    public static ApiErrorResponse of(ErrorCode errorCode, String detail, String path) {
        return new Builder()
                .status(errorCode.getHttpStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .path(path)
                .build();
    }

    /**
     * 필드 유효성 검증 에러를 포함한 응답 생성
     */
    public static ApiErrorResponse of(ErrorCode errorCode, List<FieldError> errors, String path) {
        return new Builder()
                .status(errorCode.getHttpStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .errors(errors)
                .path(path)
                .build();
    }


   /**
     * 필드 유효성 검증 에러 정보
     */
   public record FieldError(String field, String value, String reason) { }

    /**
     * Builder 패턴
     */
    public static class Builder {
        private int status;
        private String code;
        private String message;
        private String detail;
        private String path;
        private List<FieldError> errors = new ArrayList<>();

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder errors(List<FieldError> errors) {
            this.errors = errors;
            return this;
        }

        public Builder addError(String field, String value, String reason) {
            this.errors.add(new FieldError(field, value, reason));
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(this);
        }
    }
}
