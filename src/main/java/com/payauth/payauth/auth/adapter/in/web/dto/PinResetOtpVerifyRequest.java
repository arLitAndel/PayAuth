package com.payauth.payauth.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PinResetOtpVerifyRequest {

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
    private String phoneNumber;

    @NotBlank(message = "OTP는 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "OTP는 6자리 숫자여야 합니다.")
    private String otp;
}
