package com.payauth.payauth.user.domain;

public enum UserStatus {
    ACTIVE,      // 활성 (정상)
    INACTIVE,    // 비활성 (휴면 등)
    BLOCKED,     // 차단 (관리자 차단 또는 정책 위반)
    DELETED      // 탈퇴
}