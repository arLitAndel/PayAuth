package com.payauth.payauth.user.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SignUpVerifyRequest(

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    String phoneNumber,

    @NotBlank
    String verificationCode
){}
