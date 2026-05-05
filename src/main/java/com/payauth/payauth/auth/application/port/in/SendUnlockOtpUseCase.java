package com.payauth.payauth.auth.application.port.in;

/**
 * 핀번호 5회 실패로 잠긴 계정의 잠금 해제를 위한 SMS OTP 발송 UseCase.
 *
 * @deprecated {@code /auth/pin/reset} 흐름이 LOCKED 상태도 흡수하므로 폐기 예정.
 *             신규 코드는 {@link SendOtpUseCase} 를 사용할 것.
 */
@Deprecated(forRemoval = true)
public interface SendUnlockOtpUseCase {

    void sendUnlockOtp(SendUnlockOtpCommand command);

    record SendUnlockOtpCommand(
            String phoneNumber,
            String name,
            String birthDate) {}
}
