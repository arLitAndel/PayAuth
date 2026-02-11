package com.payauth.payauth.common.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common Errors
    INVALID_INPUT_VALUE(400, "C001", "입력값이 올바르지 않습니다"),
    INVALID_TYPE_VALUE(400, "C002", "입력 타입이 올바르지 않습니다"),
    ENTITY_NOT_FOUND(404, "C003", "요청한 리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(500, "C004", "서버 내부 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(405, "C005", "허용되지 않은 HTTP 메서드입니다"),
    ACCESS_DENIED(403, "C006", "접근 권한이 없습니다"),

    // User Errors
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(409, "U002", "이미 사용중인 이메일입니다"),
    DUPLICATE_LOGIN_ID(409, "U003", "이미 사용중인 아이디입니다"),
    INVALID_USER_STATUS(400, "U004", "유효하지 않은 사용자 상태입니다"),
    USER_ALREADY_REGISTERED(409, "U005", "이미 가입된 사용자입니다"),
    USER_SUSPENDED(403, "U006", "정지된 사용자입니다"),
    USER_INACTIVE(403, "U007", "비활성화된 사용자입니다"),
    INVALID_USER_NAME(400, "U008", "유효하지 않은 사용자 이름입니다"),
    USER_NAME_TOO_LONG(400, "U009", "사용자 이름이 너무 깁니다"),
    INVALID_PHONE_NUMBER(400, "U010", "유효하지 않은 전화번호입니다"),
    INVALID_PHONE_NUMBER_FORMAT(400, "U011", "전화번호 형식이 올바르지 않습니다"),
    INVALID_BIRTH_DATE(400, "U012", "유효하지 않은 생년월일입니다"),
    INVALID_BIRTH_DATE_FORMAT(400, "U013", "생년월일 형식이 올바르지 않습니다"),
    USER_ALREADY_DELETED(400, "U014", "이미 탈퇴한 사용자입니다"),

    // Auth Errors
    AUTH_CREDENTIAL_NOT_FOUND(404, "A001", "인증 정보를 찾을 수 없습니다"),
    PIN_ALREADY_REGISTERED(409, "A002", "이미 등록된 핀번호가 있습니다"),
    INVALID_PIN_FORMAT(400, "A003", "핀번호 형식이 올바르지 않습니다"),
    PIN_VERIFICATION_FAILED(401, "A004", "핀번호 인증에 실패했습니다"),
    ACCOUNT_LOCKED(403, "A005", "계정이 잠겼습니다. 고객센터에 문의하세요"),
    PIN_CANNOT_CONTAIN_SEQUENTIAL_NUMBERS(400, "A006", "연속된 숫자는 사용할 수 없습니다"),
    PIN_CANNOT_CONTAIN_REPEATED_NUMBERS(400, "A007", "반복된 숫자는 사용할 수 없습니다"),
    CURRENT_PIN_MISMATCH(401, "A008", "현재 핀번호가 일치하지 않습니다"),
    NEW_PIN_SAME_AS_CURRENT(400, "A009", "새 핀번호는 현재 핀번호와 달라야 합니다"),
    PIN_CHANGE_NOT_ALLOWED(403, "A010", "핀번호 변경이 허용되지 않습니다"),

    // Encryption Errors
    ENCRYPTION_FAILED(500, "E001", "암호화 처리 중 오류가 발생했습니다"),
    DECRYPTION_FAILED(500, "E002", "복호화 처리 중 오류가 발생했습니다");

    private final int httpStatus;
    private final String code;
    private final String message;

    ErrorCode(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}
