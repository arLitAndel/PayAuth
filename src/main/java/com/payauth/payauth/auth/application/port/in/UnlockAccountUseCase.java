package com.payauth.payauth.auth.application.port.in;

/**
 * 잠금 해제 verifiedToken 으로 실제 계정 상태(LOCKED → ACTIVE) 를 활성화하는 UseCase.
 *
 * @deprecated {@code /auth/pin/reset} ({@link ResetPinUseCase}) 흐름이 LOCKED 상태도 받아
 *             PIN 재설정과 함께 잠금 해제까지 처리하므로 폐기 예정.
 */
@Deprecated(forRemoval = true)
public interface UnlockAccountUseCase {

    void unlockAccount(UnlockAccountCommand command);

    record UnlockAccountCommand(
            String verifiedToken
    ) {}
}
