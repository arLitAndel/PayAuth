package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.UnlockAccountUseCase;
import com.payauth.payauth.auth.application.port.out.LoadAuthCredentialPort;
import com.payauth.payauth.auth.application.port.out.SaveAuthCredentialPort;
import com.payauth.payauth.auth.application.port.out.UnlockVerifiedTokenPort;
import com.payauth.payauth.auth.domain.AuthCredential;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.util.MaskUtil;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 본인인증(SMS OTP) 검증을 통과한 회원의 계정 잠금을 해제한다.
 * <ul>
 *     <li>User 상태: LOCKED → ACTIVE</li>
 *     <li>AuthCredential: 누적 실패 횟수 / 차단 시각 초기화</li>
 *     <li>잠금 해제 전용 verifiedToken 즉시 폐기</li>
 * </ul>
 *
 * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
 */
@Deprecated(forRemoval = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class UnlockAccountService implements UnlockAccountUseCase {

    private final UnlockVerifiedTokenPort unlockVerifiedTokenPort;
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final LoadAuthCredentialPort loadAuthCredentialPort;
    private final SaveAuthCredentialPort saveAuthCredentialPort;
    private final CryptoUtil cryptoUtil;

    @Override
    @Transactional
    public void unlockAccount(UnlockAccountCommand command) {
        // 1. verifiedToken 으로 전화번호 조회 (만료/위조 시 거부)
        String phoneNumber = unlockVerifiedTokenPort.load(command.verifiedToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNLOCK_VERIFICATION_REQUIRED));

        // 2. 회원 조회
        String phoneHash = cryptoUtil.hash(phoneNumber);
        User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3. 도메인 규칙: LOCKED 상태에서만 unlock 허용 (User.unlock() 내부 검증)
        user.unlock();
        saveUserPort.saveUser(user);

        // 4. AuthCredential 의 누적 실패 / 차단 시각 초기화
        AuthCredential credential = loadAuthCredentialPort.loadByUserId(user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CREDENTIAL_NOT_FOUND));
        credential.resetForUnlock();
        saveAuthCredentialPort.saveAuthCredential(credential);

        // 5. 일회용 토큰 폐기
        unlockVerifiedTokenPort.delete(command.verifiedToken());

        log.info("[ACCOUNT_UNLOCKED] userId: {}, phone: {}",
                user.getUserId(), MaskUtil.maskPhone(phoneNumber));
    }
}
