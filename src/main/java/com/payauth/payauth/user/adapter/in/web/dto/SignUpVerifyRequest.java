package com.payauth.payauth.user.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SignUpVerifyRequest(

    @NotBlank
    String verificationId,

    @NotBlank
    String verificationCode
){}
