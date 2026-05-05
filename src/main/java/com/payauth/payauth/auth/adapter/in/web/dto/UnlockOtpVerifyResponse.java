package com.payauth.payauth.auth.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
 *             일반 OTP 검증 응답({@link OtpVerifyResponse}) 으로 대체.
 */
@Deprecated(forRemoval = true)
@Getter
@AllArgsConstructor
public class UnlockOtpVerifyResponse {
    private final String verifiedToken;
}
