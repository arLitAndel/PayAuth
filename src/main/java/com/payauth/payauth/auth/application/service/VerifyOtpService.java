package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.VerifyOtpUseCase;
import com.payauth.payauth.auth.application.port.out.OtpStorePort;
import com.payauth.payauth.auth.application.port.out.OtpSecurityPort;
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

    private final OtpStorePort otpStorePort;
    private final OtpSecurityPort otpSecurityPort;
    private final SaveVerifiedTokenPort saveVerifiedTokenPort;

    @Override
    public String verifyOtp(VerifyOtpCommand command) {
        String phoneNumber = command.phoneNumber();

        if (otpSecurityPort.isBlocked(phoneNumber)) {
            throw new BusinessException(ErrorCode.OTP_ATTEMPT_BLOCKED);
        }

        String saved = otpStorePort.load(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        if (!saved.equals(command.otp())) {
            int count = otpSecurityPort.incrementAttemptAndGet(phoneNumber, OTP_ATTEMPT_TTL_SECONDS);
            if (count >= MAX_OTP_ATTEMPTS) {
                otpSecurityPort.block(phoneNumber, OTP_BLOCK_TTL_SECONDS);
                otpSecurityPort.deleteAttempt(phoneNumber);
            }
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        String verifiedToken = UUID.randomUUID().toString();
        saveVerifiedTokenPort.save(verifiedToken, phoneNumber, VERIFIED_TOKEN_TTL_SECONDS);

        //두 어댑터가 각기 다른 키를 관리하기 때문에 port 분리
        otpStorePort.delete(phoneNumber);           //otp코드
        otpSecurityPort.deleteAttempt(phoneNumber); //시도횟수

        return verifiedToken;
    }
}
