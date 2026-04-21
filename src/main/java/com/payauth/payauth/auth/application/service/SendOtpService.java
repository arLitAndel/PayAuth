package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.adapter.in.web.AuthWebMapper;
import com.payauth.payauth.auth.application.port.in.SendOtpUseCase;
import com.payauth.payauth.auth.application.port.out.LoadUserForAuthPort;
import com.payauth.payauth.auth.application.port.out.SaveOtpPort;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.crypto.StringCryptoConverter;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.sms.SmsPort;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SendOtpService implements SendOtpUseCase {

    private static final long OTP_TTL_SECONDS = 180; // 3분

    private final LoadUserPort loadUserPort;
    private final SaveOtpPort saveOtpPort;
    private final SmsPort smsPort;
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

        // 2. OTP 생성 + Redis 저장
        String otp = generateOtp();
        saveOtpPort.save(phoneNumber, otp, OTP_TTL_SECONDS);

        // 3. SMS 발송 (Mock - 콘솔 출력)
        smsPort.send(phoneNumber, "[본인확인] 인증번호 [" + otp + "]를 입력해주세요. (3분 이내 입력)");
    }

    private String generateOtp() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }
}
