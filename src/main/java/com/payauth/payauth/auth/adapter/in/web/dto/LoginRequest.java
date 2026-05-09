package com.payauth.payauth.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * /auth/login 요청 DTO.
 *
 * <p>{@code verifiedToken} 은 {@code /auth/otp/verify} 단계에서 발급받은 1회용 토큰이며,
 * {@code pin} 은 사용자가 가입 시 등록한 6자리 PIN 이다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "verifiedToken 은 필수입니다.")
    private String verifiedToken;

    @NotBlank(message = "PIN 은 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "PIN 은 6자리 숫자여야 합니다.")
    private String pin;
}
