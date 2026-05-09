package com.payauth.payauth.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

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
    DUPLICATED_PHONE_NUMBER(400, "U015", "이미 사용중인 휴대폰 번호 입니다."),

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
    INVALID_AUTH_INFO(400, "A011", "입력하신 정보가 일치하지 않습니다."),
    ACCOUNT_NOT_LOCKED(400, "A012", "잠긴 상태의 계정이 아닙니다."),
    UNLOCK_VERIFICATION_REQUIRED(401, "A013", "본인인증이 필요합니다."),
    PIN_RECENTLY_USED(400, "A014", "최근에 사용한 핀번호는 다시 사용할 수 없습니다"),
    PIN_CANNOT_BE_BIRTH_DATE(400, "A015", "생년월일과 같은 핀번호는 사용할 수 없습니다"),
    PIN_CONFIRM_MISMATCH(400, "A016", "새 핀번호와 확인 핀번호가 일치하지 않습니다"),

    // Encryption Errors
    ENCRYPTION_FAILED(500, "E001", "암호화 처리 중 오류가 발생했습니다"),
    DECRYPTION_FAILED(500, "E002", "복호화 처리 중 오류가 발생했습니다"),

    // 인증관련
    VERIFICATION_SESSION_NOT_FOUND(404, "S001", "인증 세션이 만료되었거나 존재하지 않습니다."),
    INVALID_VERIFICATION_CODE(400, "S002", "인증 번호가 일치하지 않습니다."),
    INVALID_SIGNUP_TOKEN(401, "S003", "유효하지 않거나 만료된 가입 토큰입니다."),
    TOO_MANY_RESEND_ATTEMPTS(429, "S004", "인증 번호 재발송 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),
    OTP_ATTEMPT_BLOCKED(429, "S005", "OTP 인증 시도 횟수를 초과했습니다. 10분 후 다시 시도해주세요."),
    SMS_SEND_FAILED(503, "S006", "인증 문자 발송에 실패했습니다. 관리자에게 문의하거나 다시 시도해주세요."),

    // Token / Login
    INVALID_TOKEN(401, "T001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "T002", "만료된 토큰입니다."),
    BLACKLISTED_TOKEN(401, "T003", "로그아웃 처리된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(401, "T004", "Refresh 토큰을 찾을 수 없습니다. 다시 로그인 해주세요.");


    private final int httpStatus;
    private final String code;
    private final String message;

    ErrorCode(int httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}
