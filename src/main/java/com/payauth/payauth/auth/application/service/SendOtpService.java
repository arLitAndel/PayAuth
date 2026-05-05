package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.SendOtpUseCase;
import com.payauth.payauth.auth.application.port.out.SaveOtpPort;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.identity.IdentityVerificationPort;
import com.payauth.payauth.common.identity.IdentityVerificationRequest;
import com.payauth.payauth.common.identity.VerificationPurpose;
import com.payauth.payauth.common.util.OtpUtil;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendOtpService implements SendOtpUseCase {

    private static final long OTP_TTL_SECONDS = 180; // 3분

    private final LoadUserPort loadUserPort;
    private final SaveOtpPort saveOtpPort;
    private final IdentityVerificationPort identityVerificationPort; // 운영: 다날/KCB, 로컬: Mock
    private final CryptoUtil cryptoUtil;

    @Override
    public void sendOtp(SendOtpCommand command) {

        String phoneNumber = command.phoneNumber();
        String phoneHash = cryptoUtil.hash(command.phoneNumber());

        // 1. 폰번호 + 이름으로 회원 존재 여부 확인
        User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_AUTH_INFO));
        if (!user.getName().equals(command.name())) {
            throw new BusinessException(ErrorCode.INVALID_AUTH_INFO);
        }

        // 2. 회원 상태 검증 (active 상태가 아니면 OTP 발송 불가)
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 3. OTP 생성 + Redis 저장
        String otp = OtpUtil.generateOtp();
        saveOtpPort.save(phoneNumber, otp, OTP_TTL_SECONDS);

        // 4. 본인인증 요청 (운영: 다날/KCB 어댑터, 로컬: Mock 어댑터)
        identityVerificationPort.requestVerification(new IdentityVerificationRequest(
                command.name(),
                phoneNumber,
                otp,
                VerificationPurpose.OTP_LOGIN
        ));
    }

}
