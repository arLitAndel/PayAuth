package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.ResetPinUseCase;
import com.payauth.payauth.auth.application.port.out.LoadAuthCredentialPort;
import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import com.payauth.payauth.auth.application.port.out.PinResetVerifiedTokenPort;
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
 * PIN 분실 사용자가 OTP 본인인증만으로 새 PIN 을 등록한다.
 * <p>현재 PIN 검증 단계가 없으므로, 해당 흐름을 의도치 않게 타지 않도록
 * 클라이언트는 별도 엔드포인트({@code /auth/pin/reset}) 로 명시적으로 호출해야 한다.</p>
 *
 * <p><b>잠금 계정 회복까지 흡수한다.</b> PIN 5회 실패로 LOCKED 된 계정은
 * 본 흐름을 통해 새 PIN 등록과 동시에 ACTIVE 로 복구된다.
 * (별도 잠금 해제 흐름을 두지 않음 — 잠금 = PIN 분실의 강한 증거로 해석)</p>
 *
 * <pre>
 *   1) verifiedToken → phoneNumber 조회
 *   2) User 조회, 상태 검증 (ACTIVE 또는 LOCKED 만 허용)
 *   3) AuthCredential 조회
 *   4) newPin == newPinConfirm 일치 검증
 *   5) PinPolicy 검증 (형식/연속/반복/생일/현재PIN/이력)
 *   6) AuthCredential.changePin() — PIN 교체 + 실패카운터 / 차단시간 초기화
 *   7) LOCKED 였다면 User.unlock() 으로 ACTIVE 복구
 *   8) 일회용 verifiedToken 폐기
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPinService implements ResetPinUseCase {

    private final PinResetVerifiedTokenPort pinResetVerifiedTokenPort;
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
    public void resetPin(ResetPinCommand command) {
        // 1. verifiedToken → phone (PIN 재설정 전용 토큰 네임스페이스 사용)
        String phoneNumber = pinResetVerifiedTokenPort.load(command.verifiedToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.VERIFICATION_SESSION_NOT_FOUND));

        // 2. User 조회 + 상태 검증 (ACTIVE / LOCKED 만 허용)
        String phoneHash = cryptoUtil.hash(phoneNumber);
        User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        ensureUserAllowedForReset(user);

        // 3. AuthCredential 조회
        AuthCredential credential = loadAuthCredentialPort.loadByUserId(user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_CREDENTIAL_NOT_FOUND));

        // 4. 확인 PIN 일치
        if (!command.newPin().equals(command.newPinConfirm())) {
            throw new BusinessException(ErrorCode.PIN_CONFIRM_MISMATCH);
        }

        // 5. 정책 검증
        pinPolicy.validate(
                command.newPin(),
                user.getBirthDate(),
                phoneNumber,
                credential.getPinCode(),
                credential.getPreviousPinHashesView()
        );

        // 6. PIN 교체 (+ 실패카운터·차단시간 초기화는 changePin 내부에서 수행)
        credential.changePin(command.newPin(), pinEncryptor);
        saveAuthCredentialPort.saveAuthCredential(credential);

        // 7. LOCKED 였다면 ACTIVE 로 복구
        boolean wasLocked = user.isLocked();
        if (wasLocked) {
            user.unlock();
            saveUserPort.saveUser(user);
        }

        // 8. 일회용 토큰 폐기
        pinResetVerifiedTokenPort.delete(command.verifiedToken());

        saveActivityPort.record(user.getUserId(), ActivityCategory.PIN_CHANGE, ActivityAction.PIN_RESET, true, null);
        log.info("[PIN_RESET] userId: {}, phone: {}, unlockedFromLocked: {}",
                user.getUserId(), MaskUtil.maskPhone(phoneNumber), wasLocked);
    }

    private void ensureUserAllowedForReset(User user) {
        // ACTIVE: PIN 분실(잠기진 않음)
        // LOCKED: PIN 5회 실패로 잠김 → 재설정과 함께 자동 해제
        // 그 외(PENDING / INACTIVE / BLOCKED / DELETED) 는 거부
        if (!user.isActive() && !user.isLocked()) {
            throw new BusinessException(ErrorCode.PIN_CHANGE_NOT_ALLOWED);
        }
    }
}
