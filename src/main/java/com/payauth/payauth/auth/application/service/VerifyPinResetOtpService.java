package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.VerifyPinResetOtpUseCase;
import com.payauth.payauth.auth.application.port.out.PinResetOtpSecurityPort;
import com.payauth.payauth.auth.application.port.out.PinResetOtpStorePort;
import com.payauth.payauth.auth.application.port.out.PinResetVerifiedTokenPort;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyPinResetOtpService implements VerifyPinResetOtpUseCase {

    private static final int  MAX_OTP_ATTEMPTS          = 5;
    private static final long OTP_ATTEMPT_TTL_SECONDS   = 180L;
    private static final long OTP_BLOCK_TTL_SECONDS     = 1800L;
    private static final long VERIFIED_TOKEN_TTL_SECONDS = 300L;

    private final PinResetOtpStorePort    pinResetOtpStorePort;
    private final PinResetOtpSecurityPort pinResetOtpSecurityPort;
    private final PinResetVerifiedTokenPort pinResetVerifiedTokenPort;
    private final SaveActivityPort saveActivityPort;

    @Override
    public String verifyOtp(VerifyPinResetOtpCommand command) {
        String phoneNumber = command.phoneNumber();

        if (pinResetOtpSecurityPort.isBlocked(phoneNumber)) {
            throw new BusinessException(ErrorCode.OTP_ATTEMPT_BLOCKED);
        }

        String saved = pinResetOtpStorePort.load(phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        if (!saved.equals(command.otp())) {
            int count = pinResetOtpSecurityPort.incrementAttemptAndGet(phoneNumber, OTP_ATTEMPT_TTL_SECONDS);
            if (count >= MAX_OTP_ATTEMPTS) {
                pinResetOtpSecurityPort.block(phoneNumber, OTP_BLOCK_TTL_SECONDS);
                pinResetOtpSecurityPort.deleteAttempt(phoneNumber);
            }
            saveActivityPort.record(null, ActivityCategory.AUTH, ActivityAction.PIN_RESET_OTP_VERIFY, false, ErrorCode.INVALID_VERIFICATION_CODE.getCode());
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        String verifiedToken = UUID.randomUUID().toString();
        pinResetVerifiedTokenPort.save(verifiedToken, phoneNumber, VERIFIED_TOKEN_TTL_SECONDS);

        pinResetOtpStorePort.delete(phoneNumber);
        pinResetOtpSecurityPort.deleteAttempt(phoneNumber);

        saveActivityPort.record(null, ActivityCategory.AUTH, ActivityAction.PIN_RESET_OTP_VERIFY, true, null);
        return verifiedToken;
    }
}
