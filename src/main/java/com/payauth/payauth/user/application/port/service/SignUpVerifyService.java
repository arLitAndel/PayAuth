package com.payauth.payauth.user.application.port.service;

import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.dto.SignUpSession;
import com.payauth.payauth.user.application.port.in.SignUpVerifyUseCase;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.application.port.out.SignUpSessionPort;
import com.payauth.payauth.user.domain.User;

import java.util.UUID;

public class SignUpVerifyService implements SignUpVerifyUseCase {

    private final SignUpSessionPort signUpSessionPort;
    private final SaveUserPort saveUserPort;
    private final CryptoUtil cryptoUtil;

    @Override
    public String verify(SignUpVerifyCommand command) {
        // 1. Redis에서 세션 조회
        SignUpSession session = signUpSessionPort
                .getVerificationSession(command.phoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        // 2. 인증코드 검증
        if (!session.verificationCode().equals(command.verificationCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 3. User 저장 (기존 로직)
        String phoneHash = cryptoUtil.hash(command.phoneNumber());
        User newUser = User.createNewUser(
                session.name(),
                command.phoneNumber(),
                phoneHash,
                session.birthDate()
        );
        User savedUser = saveUserPort.saveUser(newUser);

        // 4. 인증 세션 삭제 + 핀 등록용 임시 토큰 저장
        signUpSessionPort.deleteVerificationSession(command.phoneNumber());
        String signUpToken = UUID.randomUUID().toString();
        signUpSessionPort.savePinRegisterToken(signUpToken, savedUser.getId()); // TTL 10분

        return signUpToken;
    }
}
