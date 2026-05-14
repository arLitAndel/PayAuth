package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.ChangePinUseCase;
import com.payauth.payauth.auth.application.port.in.PinChangeResult;
import com.payauth.payauth.auth.application.port.out.DeleteVerifiedTokenPort;
import com.payauth.payauth.auth.application.port.out.LoadAuthCredentialPort;
import com.payauth.payauth.auth.application.port.out.LoadVerifiedTokenPort;
import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import com.payauth.payauth.auth.application.port.out.SaveAuthCredentialPort;
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
 * 현재 PIN 을 검증한 뒤 새 PIN 으로 교체한다.
 *
 * <pre>
 *   1) verifiedToken → phoneNumber 조회 (만료/위조 시 거부)
 *   2) User 조회 + 상태 검증 (ACTIVE 만 허용)
 *   3) AuthCredential 조회 + 차단/만료 검사
 *   4) 현재 PIN bcrypt 검증
 *        - 실패: 누적 실패 카운터++, 5회 누적 시 계정 LOCKED
 *   5) newPin == newPinConfirm 일치 검증
 *   6) PinPolicy 통과 (형식/연속/반복/생일/현재PIN/이력)
 *   7) AuthCredential.changePin() 으로 갱신 (이력 push, 만료 1년 연장, 카운터 reset)
 *   8) 일회용 verifiedToken 즉시 폐기
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePinService implements ChangePinUseCase {

    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final int WARNING_THRESHOLD = 3;

    private final LoadVerifiedTokenPort loadVerifiedTokenPort;
    private final DeleteVerifiedTokenPort deleteVerifiedTokenPort;
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final LoadAuthCredentialPort loadAuthCredentialPort;
    private final SaveAuthCredentialPort saveAuthCredentialPort;
    private final PinEncryptor pinEncryptor;
    private final PinPolicy pinPolicy;
    private final CryptoUtil cryptoUtil;
    private final SaveActivityPort saveActivityPort;

    @Override
    @Transactional
    public PinChangeResult changePin(ChangePinCommand command) {
        // 1. verifiedToken → phone
        String phoneNumber = loadVerifiedTokenPort.load(command.verifiedToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        // 2. User 조회 + 상태 확인
        User user = loadUserByPhone(phoneNumber);
        ensureUserAllowedForPinChange(user);

        // 3. AuthCredential 조회
        AuthCredential credential = loadAuthCredentialPort.loadByUserId(user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CREDENTIAL_NOT_FOUND));

        // 4. 현재 PIN 검증 (실패 시 incrementFailCount + 5회 시 lock)
        if (!pinEncryptor.verify(command.currentPin(), credential.getPinCode())) {
            credential.incrementFailCount();
            saveAuthCredentialPort.saveAuthCredential(credential);

            int failCount = credential.getConsecutiveFailCount();

            if (failCount >= MAX_PIN_ATTEMPTS) {
                user.lock();
                saveUserPort.saveUser(user);
                log.warn("[PIN_CHANGE_LOCKED] userId: {}, phone: {}",
                        user.getUserId(), MaskUtil.maskPhone(phoneNumber));
                saveActivityPort.record(user.getUserId(), ActivityCategory.PIN_CHANGE, ActivityAction.PIN_CHANGE, false, "ACCOUNT_LOCKED");
                return PinChangeResult.locked(
                        "현재 핀번호 5회 인증 실패로 계정이 잠김 상태로 변경되었습니다.");
            }

            int remaining = MAX_PIN_ATTEMPTS - failCount;
            String message = failCount >= WARNING_THRESHOLD
                    ? "현재 핀번호가 일치하지 않습니다. 5회 이상 실패 시 잠김 상태로 변경됩니다."
                    : "현재 핀번호가 일치하지 않습니다.";
            saveActivityPort.record(user.getUserId(), ActivityCategory.PIN_CHANGE, ActivityAction.PIN_CHANGE, false, "PIN_MISMATCH");
            return PinChangeResult.failure(remaining, message);
        }

        // 5. 새 PIN 일치 확인
        if (!command.newPin().equals(command.newPinConfirm())) {
            throw new BusinessException(ErrorCode.PIN_CONFIRM_MISMATCH);
        }

        // 6. 새 PIN 정책 검증
        pinPolicy.validate(
                command.newPin(),
                user.getBirthDate(),
                credential.getPinCode(),
                credential.getPreviousPinHashesView()
        );

        // 7. 변경 (이력 push, 만료 갱신, 카운터 reset)
        credential.changePin(command.newPin(), pinEncryptor);
        saveAuthCredentialPort.saveAuthCredential(credential);

        // 8. 일회용 토큰 폐기
        deleteVerifiedTokenPort.delete(command.verifiedToken());

        saveActivityPort.record(user.getUserId(), ActivityCategory.PIN_CHANGE, ActivityAction.PIN_CHANGE, true, null);
        log.info("[PIN_CHANGED] userId: {}, phone: {}",
                user.getUserId(), MaskUtil.maskPhone(phoneNumber));

        return PinChangeResult.success();
    }

    private User loadUserByPhone(String phoneNumber) {
        String phoneHash = cryptoUtil.hash(phoneNumber);
        return loadUserPort.findByPhoneNumberHash(phoneHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void ensureUserAllowedForPinChange(User user) {
        if (user.isLocked() || user.isBlocked() || user.isDeleted() || !user.isActive()) {
            throw new BusinessException(ErrorCode.PIN_CHANGE_NOT_ALLOWED);
        }
    }
}
