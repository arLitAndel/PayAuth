package com.payauth.payauth.auth.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
 *             {@link PinChangeResponse} 로 대체.
 */
@Deprecated(forRemoval = true)
@Getter
@AllArgsConstructor
public class UnlockAccountResponse {
    private final boolean unlocked;
    private final String message;
}
