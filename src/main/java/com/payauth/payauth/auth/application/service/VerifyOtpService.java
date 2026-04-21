package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.VerifyOtpUseCase;
import com.payauth.payauth.auth.application.port.out.DeleteOtpPort;
import com.payauth.payauth.auth.application.port.out.LoadOtpPort;
import com.payauth.payauth.auth.application.port.out.OtpAttemptPort;
import com.payauth.payauth.auth.application.port.out.OtpBlockPort;
import com.payauth.payauth.auth.application.port.out.SaveVerifiedTokenPort;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyOtpService implements VerifyOtpUseCase {

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long OTP_ATTEMPT_TTL_SECONDS = 180L;
    private static final long OTP_BLOCK_TTL_SECONDS = 1800L;
    private static final long VERIFIED_TOKEN_TTL_SECONDS = 300L;

    private final LoadOtpPort loadOtpPort;
    private final DeleteOtpPort deleteOtpPort;
    private final OtpAttemptPort otpAttemptPort;
    private final OtpBlockPort otpBlockPort;
    private final SaveVerifiedTokenPort saveVerifiedTokenPort;

    @Override
    public String verifyOtp(VerifyOtpCommand command) {
        String phoneNumber = command.phoneNumber();

        if (otpBlockPort.isBlocked(phoneNumber)) {
            throw new BusinessException(ErrorCode.OTP_ATTEMPT_BLOCKED);
        }

        String saved = loadOtpPort.load(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        if (!saved.equals(command.otp())) {
            int count = otpAttemptPort.incrementAndGet(phoneNumber, OTP_ATTEMPT_TTL_SECONDS);
            if (count >= MAX_OTP_ATTEMPTS) {
                otpBlockPort.block(phoneNumber, OTP_BLOCK_TTL_SECONDS);
                otpAttemptPort.delete(phoneNumber);
            }
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        String verifiedToken = UUID.randomUUID().toString();
        saveVerifiedTokenPort.save(verifiedToken, phoneNumber, VERIFIED_TOKEN_TTL_SECONDS);

        deleteOtpPort.delete(phoneNumber);
        otpAttemptPort.delete(phoneNumber);

        return verifiedToken;
    }
}
