package com.payauth.payauth.user.application.port.service;

import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.dto.SignUpSession;
import com.payauth.payauth.user.application.port.in.SignUpVerifyUseCase;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.application.port.out.SignUpSessionPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignUpVerifyService implements SignUpVerifyUseCase {

    private final SignUpSessionPort signUpSessionPort;
    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;
    private final CryptoUtil cryptoUtil;

    @Override
    public Result verify(SignUpVerifyCommand command) {
        // 1. Redis에서 세션 조회
        SignUpSession session = signUpSessionPort
                .getVerificationSession(command.verificationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        // 2. 인증코드 검증
        if (!session.verificationCode().equals(command.verificationCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 3. [핵심] 기존 PENDING 유저인지 조회
        String phoneHash = cryptoUtil.hash(session.phoneNumber());
        Optional<User> existingUser = loadUserPort.findByPhoneNumberHash(phoneHash);

        // 4. 회원저장 (기존 PENDING 유저가 없을 때만 insert)
        User savedUser;
        if (existingUser.isEmpty()) {
            User newUser = User.createNewUser(
                    session.name(),
                    session.phoneNumber(),
                    phoneHash,
                    session.birthDate()
            );
            savedUser = saveUserPort.saveUser(newUser);
        } else {
            savedUser = existingUser.get(); // PENDING 유저면 기존 유저 그대로 사용
        }

        // 4. 인증 세션 삭제 + 핀 등록용 임시 토큰 저장
        signUpSessionPort.deleteVerificationSession(session.phoneNumber());
        String signUpToken = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(10);
        signUpSessionPort.savePinRegisterToken(signUpToken, savedUser.getId()); // TTL 10분

        return new Result(signUpToken, expiredAt);
    }
}
