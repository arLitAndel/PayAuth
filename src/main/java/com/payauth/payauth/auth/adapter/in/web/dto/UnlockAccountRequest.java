package com.payauth.payauth.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
 *             {@link ResetPinRequest} 로 대체.
 */
@Deprecated(forRemoval = true)
@Getter
@NoArgsConstructor
public class UnlockAccountRequest {

    @NotBlank(message = "검증 토큰은 필수입니다.")
    private String verifiedToken;
}
