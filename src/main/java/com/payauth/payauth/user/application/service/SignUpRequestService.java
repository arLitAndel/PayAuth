package com.payauth.payauth.user.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.RegisterPinUseCase;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.identity.IdentityVerificationPort;
import com.payauth.payauth.common.identity.IdentityVerificationRequest;
import com.payauth.payauth.common.identity.VerificationPurpose;
import com.payauth.payauth.common.util.MaskUtil;
import com.payauth.payauth.common.util.OtpUtil;
import com.payauth.payauth.user.adapter.in.web.dto.SignUpResponse;
import com.payauth.payauth.user.application.port.dto.SignUpSession;
import com.payauth.payauth.user.application.port.in.SignUpRequestUseCase;
import com.payauth.payauth.user.application.port.out.CheckUserDuplicationPort;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.application.port.out.SignUpSessionPort;
import com.payauth.payauth.user.domain.User;
import com.payauth.payauth.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpRequestService implements SignUpRequestUseCase {

    private final CheckUserDuplicationPort checkUserDuplicationPort;
    private final IdentityVerificationPort identityVerificationPort;
    private final SignUpSessionPort signUpSessionPort;
    private final LoadUserPort loadUserPort;
    private final CryptoUtil cryptoUtil;
    private final SaveActivityPort saveActivityPort;

    // SecureRandom 인스턴스 재사용
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_RESEND_COUNT = 5;

    @Override
    public SignUpResponse request(SignUpRequestCommand command) {
        try {
            return doRequest(command);
        } catch (BusinessException e) {
            saveActivityPort.record(null, ActivityCategory.SIGN_UP, ActivityAction.SIGN_UP_REQUEST, false, e.getErrorCode().getCode());
            throw e;
        }
    }

    private SignUpResponse doRequest(SignUpRequestCommand command) {
        String phoneNumber = command.phoneNumber();

        // 1. 중복 체크 (가장 먼저 수행하여 불필요한 리소스 방지)
        String phoneHash = cryptoUtil.hash(phoneNumber);
        loadUserPort.findByPhoneNumberHash(phoneHash).ifPresent(user -> {
            switch (user.getUserStatus()) {
                case ACTIVE -> throw new BusinessException(ErrorCode.DUPLICATED_PHONE_NUMBER);
                case LOCKED, BLOCKED, INACTIVE -> throw new BusinessException(ErrorCode.SIGN_UP_NOT_ALLOWED);
                case DELETED -> log.info("[REGISTER_REENTRY] Withdrawn user re-registering. Phone: {}",
                        MaskUtil.maskPhone(phoneNumber));
                default -> log.info("[REGISTER_RETRY] Pending user found, resending verification. Phone: {}",
                        MaskUtil.maskPhone(phoneNumber));
            }
        });

        // 2. 재발송 횟수 체크
        int resendCount = signUpSessionPort.incrementResendCount(phoneNumber);
        if (resendCount > MAX_RESEND_COUNT) {
            log.warn("[REGISTER_BLOCKED] Too many resend attempts. Phone: {}", MaskUtil.maskPhone(phoneNumber));
            throw new BusinessException(ErrorCode.TOO_MANY_RESEND_ATTEMPTS);
        }

        // 3. 인증 코드 + verificationId 생성
        String verificationCode = OtpUtil.generateOtp();
        String verificationId = UUID.randomUUID().toString();

        // 4. 본인인증 요청 (운영: 다날/KCB 어댑터, 로컬: Mock 어댑터)
        try {
            identityVerificationPort.requestVerification(new IdentityVerificationRequest(
                    command.name(),
                    phoneNumber,
                    verificationCode,
                    VerificationPurpose.SIGN_UP
            ));
        } catch (Exception e) {
            log.error("[IDENTITY_VERIFICATION_ERROR] Failed to send sign-up verification to {}",
                    MaskUtil.maskPhone(phoneNumber), e);
            signUpSessionPort.decrementResendCount(phoneNumber);
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }

        // 5. Redis에 세션 정보 저장 (발송 성공 시에만 저장됨)
        LocalDateTime expiredAt = signUpSessionPort.saveVerificationSession(
                verificationId,
                new SignUpSession(command.name(), command.birthDate(), phoneNumber, verificationCode)
        );

        saveActivityPort.record(null, ActivityCategory.SIGN_UP, ActivityAction.SIGN_UP_REQUEST, true, null);
        log.info("[REGISTER_TRY] Verification code sent. Name: {}, Phone: {}",
                MaskUtil.maskName(command.name()), MaskUtil.maskPhone(phoneNumber));

        return new SignUpResponse(verificationId, MaskUtil.maskPhone(phoneNumber), expiredAt);
    }

}
