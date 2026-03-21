package com.payauth.payauth.user.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserSignUpRequest(

        @NotBlank(message = "이름은 필수입니다")
        String name,

        @NotBlank(message = "휴대폰 번호는 필수입니다")
        @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호 형식이 올바르지 않습니다")
        String phoneNumber,

        @NotBlank(message = "생년월일은 필수입니다")
        @Pattern(regexp = "^\\d{8}$", message = "생년월일은 8자리 숫자여야 합니다 (YYYYMMDD)")
        String birthDate,

        @NotBlank(message = "핀번호는 필수입니다")
        @Pattern(regexp = "^\\d{6}$", message = "핀번호는 6자리 숫자여야 합니다")
        String pinCode
) {}