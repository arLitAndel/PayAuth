package com.payauth.payauth.user.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SignUpResponse (
        String verificationId,     // 인증 요청 식별자
        String phoneNumber,        // 인증 요청한 휴대폰 번호 (마스킹: 010****1234)
        LocalDateTime expiredAt    // 인증코드 만료 시각
){}
