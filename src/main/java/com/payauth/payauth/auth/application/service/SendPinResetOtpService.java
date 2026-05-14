package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.SendPinResetOtpUseCase;
import com.payauth.payauth.auth.application.port.out.PinResetOtpStorePort;
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
 * PIN 분실 시 재설정 흐름의 OTP 발송 서비스.
 *
 * <p>일반 SMS OTP({@link SendOtpService}) 와 달리 다날/KCB 등 강화 본인인증 수단을 사용한다.
 * ACTIVE 및 LOCKED 계정 모두 허용하며, LOCKED 계정은 PIN 재설정 완료 시 자동으로 활성화된다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SendPinResetOtpService implements SendPinResetOtpUseCase {

    private static final long OTP_TTL_SECONDS = 180L;

    private final LoadUserPort loadUserPort;
    private final PinResetOtpStorePort pinResetOtpStorePort;
    private final IdentityVerificationPort identityVerificationPort;
    private final CryptoUtil cryptoUtil;
    private final SaveActivityPort saveActivityPort;

    @Override
    public void sendOtp(SendPinResetOtpCommand command) {
        Long userId = null;
        try {
            String phoneNumber = command.phoneNumber();
            String phoneHash = cryptoUtil.hash(phoneNumber);

            // 1. 전화번호 + 이름 + 생년월일로 회원 조회
            User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_AUTH_INFO));
            userId = user.getUserId();

            if (!user.getName().equals(command.name()) || !user.getBirthDate().equals(command.birthDate())) {
                throw new BusinessException(ErrorCode.INVALID_AUTH_INFO);
            }

            // 2. 상태 검증 — ACTIVE(PIN 분실) / LOCKED(PIN 5회 실패) 만 허용
            if (!user.isActive() && !user.isLocked()) {
                throw new BusinessException(ErrorCode.PIN_CHANGE_NOT_ALLOWED);
            }

            // 3. OTP 생성 + 강화 본인인증 요청
            String otp = OtpUtil.generateOtp();
            try {
                identityVerificationPort.requestVerification(new IdentityVerificationRequest(
                        command.name(),
                        phoneNumber,
                        otp,
                        VerificationPurpose.PIN_RESET
                ));
            } catch (Exception e) {
                log.error("[PIN_RESET_OTP_SEND_FAIL] Phone: {}, reason: {}",
                        MaskUtil.maskPhone(phoneNumber), e.getMessage());
                throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
            }

            // 4. Redis 저장 (발송 성공 시에만)
            pinResetOtpStorePort.save(phoneNumber, otp, OTP_TTL_SECONDS);

            saveActivityPort.record(userId, ActivityCategory.AUTH, ActivityAction.PIN_RESET_OTP_SEND, true, null);
            log.info("[PIN_RESET_OTP_SENT] Phone: {}", MaskUtil.maskPhone(phoneNumber));

        } catch (BusinessException e) {
            saveActivityPort.record(userId, ActivityCategory.AUTH, ActivityAction.PIN_RESET_OTP_SEND, false, e.getErrorCode().getCode());
            throw e;
        }
    }
}
