package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.PinVerifyResult;
import com.payauth.payauth.auth.application.port.in.VerifyPinUseCase;
import com.payauth.payauth.auth.application.port.out.LoadAuthCredentialPort;
import com.payauth.payauth.auth.application.port.out.LoadVerifiedTokenPort;
import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import com.payauth.payauth.auth.application.port.out.SaveAuthCredentialPort;
import com.payauth.payauth.auth.domain.AuthCredential;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyPinService implements VerifyPinUseCase {

    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final int WARNING_THRESHOLD = 3;

    private final LoadVerifiedTokenPort loadVerifiedTokenPort;
    private final LoadUserPort loadUserPort;
    private final LoadAuthCredentialPort loadAuthCredentialPort;
    private final SaveAuthCredentialPort saveAuthCredentialPort;
    private final SaveUserPort saveUserPort;
    private final PinEncryptor pinEncryptor;
    private final CryptoUtil cryptoUtil;
    private final SaveActivityPort saveActivityPort;

    @Override
    @Transactional
    public PinVerifyResult verifyPin(VerifyPinCommand command) {
        // 1. 검증 토큰으로 전화번호 조회 (만료 시 예외)
        String phoneNumber = loadVerifiedTokenPort.load(command.verifiedToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        // 2. 전화번호로 유저 조회
        String phoneHash = cryptoUtil.hash(phoneNumber);
        User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }

        // 3. AuthCredential 조회
        AuthCredential credential = loadAuthCredentialPort.loadByUserId(user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CREDENTIAL_NOT_FOUND));

        // 4. PIN 검증
        if (!pinEncryptor.verify(command.pin(), credential.getPinCode())) {
            credential.incrementFailCount();
            saveAuthCredentialPort.saveAuthCredential(credential);

            int failCount = credential.getConsecutiveFailCount();

            // 5회 실패 → 유저 잠금
            if (failCount >= MAX_PIN_ATTEMPTS) {
                user.lock();
                saveUserPort.saveUser(user);
                saveActivityPort.record(user.getUserId(), ActivityCategory.AUTH, ActivityAction.PIN_VERIFY, false, "ACCOUNT_LOCKED");
                return PinVerifyResult.locked("핀번호 5회 인증 실패로 계정이 잠김 상태로 변경되었습니다.");
            }

            int remaining = MAX_PIN_ATTEMPTS - failCount;
            String message = failCount >= WARNING_THRESHOLD
                    ? "핀번호가 일치하지 않습니다. 5회 이상 실패 시 잠김 상태로 변경됩니다(남은 횟수:" + remaining +")"
                    : "핀번호가 일치하지 않습니다.";

            saveActivityPort.record(user.getUserId(), ActivityCategory.AUTH, ActivityAction.PIN_VERIFY, false, "PIN_MISMATCH");
            return PinVerifyResult.failure(remaining, message);
        }

        // 6. 성공 → 실패 횟수 초기화
        credential.markAsVerified();
        saveAuthCredentialPort.saveAuthCredential(credential);

        saveActivityPort.record(user.getUserId(), ActivityCategory.AUTH, ActivityAction.PIN_VERIFY, true, null);
        return PinVerifyResult.success();
    }
}
