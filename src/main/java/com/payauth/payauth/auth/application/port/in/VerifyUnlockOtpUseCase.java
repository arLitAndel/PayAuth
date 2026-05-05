package com.payauth.payauth.auth.application.port.in;

/**
 * 잠금 해제 SMS OTP 검증 UseCase.
 * 검증 성공 시 잠금 해제 전용 verifiedToken 을 발급한다.
 *
 * @deprecated {@code /auth/pin/reset} 흐름으로 통합되었음. 일반 {@link VerifyOtpUseCase} 가
 *             발급한 verifiedToken 으로 ResetPin 을 호출하면 잠금 해제까지 함께 처리된다.
 */
@Deprecated(forRemoval = true)
public interface VerifyUnlockOtpUseCase {

    String verifyUnlockOtp(VerifyUnlockOtpCommand command);

    record VerifyUnlockOtpCommand(
            String phoneNumber,
            String otp
    ) {}
}
