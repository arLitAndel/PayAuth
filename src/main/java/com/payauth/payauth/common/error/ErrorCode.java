package com.payauth.payauth.common.error;

public enum ErrorCode {

    INVALID_USER_NAME("유효하지 않은 사용자 이름입니다."),
    USER_NAME_TOO_LONG("사용자 이름이 너무 깁니다."),
    INVALID_PHONE_NUMBER("유효하지 않은 전화번호입니다."),
    INVALID_PHONE_NUMBER_FORMAT("전화번호 형식이 올바르지 않습니다."),
    INVALID_BIRTH_DATE("유효하지 않은 생년월일입니다."),
    INVALID_BIRTH_DATE_FORMAT("생년월일 형식이 올바르지 않습니다."),
    USER_ALREADY_DELETED("이미 탈퇴한 사용자입니다."),

}
