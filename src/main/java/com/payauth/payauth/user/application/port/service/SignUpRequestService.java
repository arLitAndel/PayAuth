package com.payauth.payauth.user.application.port.service;

import com.payauth.payauth.auth.application.port.in.RegisterPinUseCase;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.sms.SmsPort;
import com.payauth.payauth.common.util.MaskUtil;
import com.payauth.payauth.user.application.port.dto.SignUpSession;
import com.payauth.payauth.user.application.port.in.SignUpRequestUseCase;
import com.payauth.payauth.user.application.port.out.CheckUserDuplicationPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.application.port.out.SignUpSessionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpRequestService implements SignUpRequestUseCase {

    private final CheckUserDuplicationPort checkUserDuplicationPort;
    private final SmsPort smsPort;                      // SMS 발송 포트 (신규)
    private final SignUpSessionPort signUpSessionPort; // Redis 포트 (신규)
    private final CryptoUtil cryptoUtil;

    // SecureRandom 인스턴스 재사용
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RESEND_COUNT = 5;

    @Override
    @Transactional
    public void request(SignUpRequestCommand command) {

        String phoneNumber = command.phoneNumber();

        // 1. 중복 체크 (가장 먼저 수행하여 불필요한 리소스 방지)
        String phoneHash = cryptoUtil.hash(phoneNumber);
        if (checkUserDuplicationPort.existsByPhoneNumberHash(phoneHash)) {
            throw new BusinessException(ErrorCode.DUPLICATED_PHONE_NUMBER);
        }

        // 2. 재발송 횟수 체크 및 증가 (Atomic 연산 권장)
        int resendCount = signUpSessionPort.incrementResendCount(phoneNumber);
        if (resendCount > MAX_RESEND_COUNT) {
            log.warn("[REGISTER_BLOCKED] Too many resend attempts. Phone: {}", MaskUtil.maskPhone(phoneNumber));
            throw new BusinessException(ErrorCode.TOO_MANY_RESEND_ATTEMPTS);
        }

        // 3. 인증 코드 생성
        String verificationCode = generateVerificationCode();

        // 4. SMS 발송 (발송 실패 시 예외 발생 -> 트랜잭션 롤백)
        try {
            smsPort.send(phoneNumber, verificationCode);
        } catch (Exception e) {
            log.error("[SMS_ERROR] Failed to send verification code to {}", MaskUtil.maskPhone(phoneNumber), e);
            // 문자 발송 실패 전용 에러 코드를 던짐
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }

        // 5. Redis에 세션 정보 저장 (발송 성공 시에만 저장됨)
        signUpSessionPort.saveVerificationSession(
                phoneNumber,
                new SignUpSession(command.name(), command.birthDate(), verificationCode)
        );

        log.info("[REGISTER_TRY] Verification code sent. Name: {}, Phone: {}",
                MaskUtil.maskName(command.name()), MaskUtil.maskPhone(phoneNumber));

    }

    /**
     * SMS 발송 로직 (비동기 처리 또는 별도 예외 처리)
     */
    private void sendSmsAsync(String phoneNumber, String code) {
        try {
            smsPort.send(phoneNumber, code);
        } catch (Exception e) {
            // SMS 발송 실패 시 로그를 남기되, 이미 저장된 세션은 유지 (사용자가 '재발송' 클릭 가능)
            log.error("[SMS_ERROR] Failed to send verification code to {}", MaskUtil.maskPhone(phoneNumber), e);
            // 필요 시 "문자 발송에 실패했습니다"라는 별도 예외를 던질 수 있음
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
