package com.payauth.payauth.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PinVerifyRequest {

    @NotBlank(message = "검증 토큰은 필수입니다.")
    private String verifiedToken; // OTP 검증 후 발급된 토큰

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "핀번호는 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "핀번호는 6자리 숫자여야 합니다.")
    private String pin;
}
