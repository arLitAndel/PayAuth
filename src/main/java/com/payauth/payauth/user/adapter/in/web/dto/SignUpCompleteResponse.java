package com.payauth.payauth.user.adapter.in.web.dto;

import java.time.LocalDateTime;

public record SignUpCompleteResponse(
        Long userId,
        String name,
        String phoneNumber,        // 마스킹 처리
        String birthDate,
        LocalDateTime createdAt
) { }
