package com.payauth.payauth.user.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SignUpVerifyResponse(
        String signUpToken,        // 다음 단계(PIN 설정)에 사용할 임시 토큰
        LocalDateTime expiredAt    // 토큰 만료 시각
) {}
