package com.payauth.payauth.user.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpCompleteRequest(
        @NotBlank
        String signUpToken,   // 인증요청에서 받은 임시 토큰

        @NotBlank(message = "핀번호는 필수입니다")
        @Pattern(regexp = "^\\d{6}$", message = "핀번호는 6자리 숫자여야 합니다")
        String pinCode
) {
}
