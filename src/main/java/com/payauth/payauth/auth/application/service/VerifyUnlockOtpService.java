package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.VerifyUnlockOtpUseCase;
import com.payauth.payauth.auth.application.port.out.OtpSecurityPort;
import com.payauth.payauth.auth.application.port.out.UnlockOtpStorePort;
import com.payauth.payauth.auth.application.port.out.UnlockVerifiedTokenPort;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 잠금 해제 SMS OTP 검증 서비스.
 * 검증 성공 시 잠금 해제 전용 verifiedToken 을 발급한다.
 *
 * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
 */
@Deprecated(forRemoval = true)
@Service
@RequiredArgsConstructor
public class VerifyUnlockOtpService implements VerifyUnlockOtpUseCase {

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long OTP_ATTEMPT_TTL_SECONDS = 180L;
    private static final long OTP_BLOCK_TTL_SECONDS = 1800L;
    private static final long VERIFIED_TOKEN_TTL_SECONDS = 300L;

    private final UnlockOtpStorePort unlockOtpStorePort;
    private final UnlockVerifiedTokenPort unlockVerifiedTokenPort;
    private final OtpSecurityPort otpSecurityPort;

    @Override
    public String verifyUnlockOtp(VerifyUnlockOtpCommand command) {
        String phoneNumber = command.phoneNumber();

        if (otpSecurityPort.isBlocked(phoneNumber)) {
            throw new BusinessException(ErrorCode.OTP_ATTEMPT_BLOCKED);
        }

        String saved = unlockOtpStorePort.load(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        if (!saved.equals(command.otp())) {
            int count = otpSecurityPort.incrementAttemptAndGet(phoneNumber, OTP_ATTEMPT_TTL_SECONDS);
            if (count >= MAX_OTP_ATTEMPTS) {
                otpSecurityPort.block(phoneNumber, OTP_BLOCK_TTL_SECONDS);
                otpSecurityPort.deleteAttempt(phoneNumber);
            }
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 검증 성공 → 잠금 해제 전용 토큰 발급
        String verifiedToken = UUID.randomUUID().toString();
        unlockVerifiedTokenPort.save(verifiedToken, phoneNumber, VERIFIED_TOKEN_TTL_SECONDS);

        unlockOtpStorePort.delete(phoneNumber);
        otpSecurityPort.deleteAttempt(phoneNumber);

        return verifiedToken;
    }
}
