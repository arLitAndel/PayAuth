package com.payauth.payauth.user.domain;

public enum UserStatus {
    ACTIVE,      // 활성 (정상)
    INACTIVE,    // 비활성 (휴면 등)
    PENDING,     // 본인 인증 완료, 핀 번호 설정 대기 중
    LOCKED,      // 잠김 (PIN 5회 실패)
    BLOCKED,     // 차단 (관리자 차단 또는 정책 위반)
    DELETED      // 탈퇴
}