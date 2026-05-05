package com.payauth.payauth.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResetPinRequest {

    @NotBlank(message = "검증 토큰은 필수입니다.")
    private String verifiedToken;

    @NotBlank(message = "새 핀번호는 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "핀번호는 6자리 숫자여야 합니다.")
    private String newPin;

    @NotBlank(message = "새 핀번호 확인은 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "핀번호는 6자리 숫자여야 합니다.")
    private String newPinConfirm;
}
