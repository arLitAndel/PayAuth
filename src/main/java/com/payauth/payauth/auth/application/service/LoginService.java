package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.LoginUseCase;
import com.payauth.payauth.auth.application.port.in.TokenPair;
import com.payauth.payauth.auth.application.port.out.DeleteVerifiedTokenPort;
import com.payauth.payauth.auth.application.port.out.LoadAuthCredentialPort;
import com.payauth.payauth.auth.application.port.out.LoadVerifiedTokenPort;
import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import com.payauth.payauth.auth.application.port.out.RefreshTokenStorePort;
import com.payauth.payauth.auth.application.port.out.SaveAuthCredentialPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort;
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

/**
 * 로그인 유스케이스 구현.
 *
 * <p>본인인증(OTP) 단계에서 받은 verifiedToken 을 다시 검증해 phoneNumber 를 얻고,
 * 그 phoneNumber 의 사용자에 대해 PIN 을 검증한 뒤 JWT 토큰 쌍을 발급한다.</p>
 *
 * <p>PIN 실패/잠금 정책은 {@link VerifyPinService} 와 동일하게 5회 누적 실패 시 계정 잠금이다.</p>
 */
@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private static final int MAX_PIN_ATTEMPTS = 5;

    private final LoadVerifiedTokenPort loadVerifiedTokenPort;
    private final DeleteVerifiedTokenPort deleteVerifiedTokenPort;
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final LoadAuthCredentialPort loadAuthCredentialPort;
    private final SaveAuthCredentialPort saveAuthCredentialPort;
    private final PinEncryptor pinEncryptor;
    private final CryptoUtil cryptoUtil;
    private final TokenIssuerPort tokenIssuerPort;
    private final RefreshTokenStorePort refreshTokenStorePort;
    private final SaveActivityPort saveActivityPort;

    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public TokenPair login(LoginCommand command) {
        // 1) verifiedToken 유효성 확인 → phoneNumber 복원
        String phoneNumber = loadVerifiedTokenPort.load(command.verifiedToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        // 2) 사용자 조회
        String phoneHash = cryptoUtil.hash(phoneNumber);
        User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }

        // 3) PIN 검증
        AuthCredential credential = loadAuthCredentialPort.loadByUserId(user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CREDENTIAL_NOT_FOUND));

        if (!pinEncryptor.verify(command.pin(), credential.getPinCode())) {
            credential.incrementFailCount();
            saveAuthCredentialPort.saveAuthCredential(credential);

            if (credential.getConsecutiveFailCount() >= MAX_PIN_ATTEMPTS) {
                user.lock();
                saveUserPort.saveUser(user);
                saveActivityPort.record(user.getUserId(), ActivityCategory.ACCESS, ActivityAction.LOGIN, false, ErrorCode.ACCOUNT_LOCKED.getCode());
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }
            saveActivityPort.record(user.getUserId(), ActivityCategory.ACCESS, ActivityAction.LOGIN, false, ErrorCode.PIN_VERIFICATION_FAILED.getCode());
            throw new BusinessException(ErrorCode.PIN_VERIFICATION_FAILED);
        }

        // 4) PIN 성공 — 실패 카운트 초기화
        credential.markAsVerified();
        saveAuthCredentialPort.saveAuthCredential(credential);

        // 5) verifiedToken 1회 사용 후 폐기
        deleteVerifiedTokenPort.delete(command.verifiedToken());

        // 6) JWT 발급 + refresh 토큰 저장
        String accessToken = tokenIssuerPort.createAccessToken(user.getUserId());
        String refreshToken = tokenIssuerPort.createRefreshToken(user.getUserId());
        refreshTokenStorePort.save(
                user.getUserId(),
                refreshToken,
                tokenIssuerPort.getRefreshTokenValiditySeconds()
        );

        saveActivityPort.record(user.getUserId(), ActivityCategory.ACCESS, ActivityAction.LOGIN, true, null);

        return new TokenPair(
                accessToken,
                refreshToken,
                tokenIssuerPort.getAccessTokenValiditySeconds(),
                tokenIssuerPort.getRefreshTokenValiditySeconds()
        );
    }
}
