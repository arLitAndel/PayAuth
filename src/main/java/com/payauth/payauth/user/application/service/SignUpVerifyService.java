package com.payauth.payauth.user.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
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
    private final SaveActivityPort saveActivityPort;

    @Override
    public Result verify(SignUpVerifyCommand command) {
        try {
            return doVerify(command);
        } catch (BusinessException e) {
            saveActivityPort.record(null, ActivityCategory.SIGN_UP, ActivityAction.SIGN_UP_VERIFY, false, e.getErrorCode().getCode());
            throw e;
        }
    }

    private Result doVerify(SignUpVerifyCommand command) {
        // 1. Redis에서 세션 조회
        SignUpSession session = signUpSessionPort
                .getVerificationSession(command.verificationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        // 2. 인증코드 검증
        if (!session.verificationCode().equals(command.verificationCode())) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE);
        }

        // 3. 기존 PENDING 유저인지 조회
        String phoneHash = cryptoUtil.hash(session.phoneNumber());
        Optional<User> existingUser = loadUserPort.findByPhoneNumberHash(phoneHash);

        // 4. 회원저장
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
            User existing = existingUser.get();
            if (existing.isDeleted()) {
                // 탈퇴 후 재가입: 기존 레코드를 PENDING으로 초기화
                existing.resetForReRegistration();
                savedUser = saveUserPort.saveUser(existing);
            } else {
                savedUser = existing; // PENDING 유저면 기존 유저 그대로 사용
            }
        }

        // 5. 인증 세션 삭제 + 핀 등록용 임시 토큰 저장
        signUpSessionPort.deleteVerificationSession(session.phoneNumber());
        String signUpToken = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(10);
        signUpSessionPort.savePinRegisterToken(signUpToken, savedUser.getUserId()); // TTL 10분

        saveActivityPort.record(savedUser.getUserId(), ActivityCategory.SIGN_UP, ActivityAction.SIGN_UP_VERIFY, true, null);

        return new Result(signUpToken, expiredAt);
    }
}
