package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.SendUnlockOtpUseCase;
import com.payauth.payauth.auth.application.port.out.UnlockOtpStorePort;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.identity.IdentityVerificationPort;
import com.payauth.payauth.common.identity.IdentityVerificationRequest;
import com.payauth.payauth.common.identity.VerificationPurpose;
import com.payauth.payauth.common.util.MaskUtil;
import com.payauth.payauth.common.util.OtpUtil;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 핀번호 5회 실패로 LOCKED 상태가 된 회원이 본인인증(SMS OTP) 을 받기 위한 발송 서비스.
 * <p>
 * 일반 OTP 발송과 달리 LOCKED 상태에서만 호출이 허용되며,
 * BLOCKED / DELETED 상태나 잠금이 아닌 회원에 대해서는 거부한다.
 *
 * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
 */
@Deprecated(forRemoval = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class SendUnlockOtpService implements SendUnlockOtpUseCase {

    private static final long OTP_TTL_SECONDS = 180; // 3분

    private final LoadUserPort loadUserPort;
    private final UnlockOtpStorePort unlockOtpStorePort;
    private final IdentityVerificationPort identityVerificationPort; // 운영: 다날/KCB, 로컬: Mock
    private final CryptoUtil cryptoUtil;

    @Override
    public void sendUnlockOtp(SendUnlockOtpCommand command) {
        String phoneNumber = command.phoneNumber();
        String phoneHash = cryptoUtil.hash(phoneNumber);

        // 1. 폰번호 + 이름 + 생년월일 기반 회원 조회
        User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_AUTH_INFO));
        if (!user.getName().equals(command.name())) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_INFO);
        } else if(!user.getBirthDate().equals(command.birthDate())) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_INFO);
        }

        // 2. 잠금 해제는 LOCKED 상태에서만 허용
        if (user.isBlocked() || user.isDeleted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!user.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_LOCKED);
        }

        // 3. OTP 생성 + Redis 저장 (잠금 해제 전용 키)
        String otp = OtpUtil.generateOtp();
        unlockOtpStorePort.save(phoneNumber, otp, OTP_TTL_SECONDS);

        // 4. 본인인증 요청 (운영: 다날/KCB 어댑터, 로컬: Mock 어댑터)
        try {
            identityVerificationPort.requestVerification(new IdentityVerificationRequest(
                    command.name(),
                    phoneNumber,
                    otp,
                    VerificationPurpose.ACCOUNT_UNLOCK
            ));
        } catch (Exception e) {
            log.error("[UNLOCK_IDENTITY_VERIFICATION_FAIL] Phone: {}, reason: {}",
                    MaskUtil.maskPhone(phoneNumber), e.getMessage());
            unlockOtpStorePort.delete(phoneNumber);
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }

        log.info("[UNLOCK_OTP_SENT] Phone: {}", MaskUtil.maskPhone(phoneNumber));
    }

}
