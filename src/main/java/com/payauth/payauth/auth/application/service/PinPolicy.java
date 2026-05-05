package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * PIN 정책 검증기.
 * <ul>
 *     <li>형식: 6자리 숫자</li>
 *     <li>금지: 연속된 숫자(123456 / 654321 등)</li>
 *     <li>금지: 반복 패턴(111111 / 121212 / 123123 / 동일숫자 4자 이상 연속)</li>
 *     <li>금지: 사용자 생년월일 기반 PIN(YYMMDD / MMDDYY / DDMMYY / YYYYMM / YYYYDD)</li>
 *     <li>금지: 현재 PIN 과 동일</li>
 *     <li>금지: 최근 사용한 PIN 이력과 동일</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class PinPolicy {

    private static final int PIN_LENGTH = 6;

    private final PinEncryptor pinEncryptor;

    /**
     * 새 PIN 종합 검증. 위반 시 {@link BusinessException} 을 던진다.
     *
     * @param newRawPin           검증할 원문 PIN
     * @param birthDate           사용자 생년월일 (YYYYMMDD, null 허용)
     * @param currentPinHash      현재 등록된 PIN 의 bcrypt 해시
     * @param previousPinHashes   최근 사용한 PIN 해시들 (없으면 빈 리스트)
     */
    public void validate(String newRawPin,
                         String birthDate,
                         String currentPinHash,
                         List<String> previousPinHashes) {
        validateFormat(newRawPin);
        validateNotSequential(newRawPin);
        validateNotRepeated(newRawPin);
        validateNotBirthDate(newRawPin, birthDate);
        validateNotSameAsCurrent(newRawPin, currentPinHash);
        validateNotRecentlyUsed(newRawPin, previousPinHashes);
    }

    // === 형식 ===
    private void validateFormat(String pin) {
        if (pin == null || pin.length() != PIN_LENGTH || !pin.matches("\\d{" + PIN_LENGTH + "}")) {
            throw new BusinessException(ErrorCode.INVALID_PIN_FORMAT);
        }
    }

    // === 연속된 숫자 (123456 / 654321) ===
    private void validateNotSequential(String pin) {
        boolean ascending = true;
        boolean descending = true;
        for (int i = 1; i < pin.length(); i++) {
            int diff = pin.charAt(i) - pin.charAt(i - 1);
            if (diff != 1) ascending = false;
            if (diff != -1) descending = false;
        }
        if (ascending || descending) {
            throw new BusinessException(ErrorCode.PIN_CANNOT_CONTAIN_SEQUENTIAL_NUMBERS);
        }
    }

    // === 반복 패턴 ===
    // 1) 모두 동일 (111111)
    // 2) 동일 숫자 4자 이상 연속 (111122, 211112 등)
    // 3) 2자리 반복 (121212)
    // 4) 3자리 반복 (123123)
    private void validateNotRepeated(String pin) {
        // 1) 전부 동일
        if (pin.chars().distinct().count() == 1) {
            throw new BusinessException(ErrorCode.PIN_CANNOT_CONTAIN_REPEATED_NUMBERS);
        }
        // 2) 동일 숫자 4자 이상 연속
        int run = 1;
        for (int i = 1; i < pin.length(); i++) {
            run = (pin.charAt(i) == pin.charAt(i - 1)) ? run + 1 : 1;
            if (run >= 4) {
                throw new BusinessException(ErrorCode.PIN_CANNOT_CONTAIN_REPEATED_NUMBERS);
            }
        }
        // 3) ABABAB
        if (pin.charAt(0) == pin.charAt(2) && pin.charAt(2) == pin.charAt(4)
                && pin.charAt(1) == pin.charAt(3) && pin.charAt(3) == pin.charAt(5)) {
            throw new BusinessException(ErrorCode.PIN_CANNOT_CONTAIN_REPEATED_NUMBERS);
        }
        // 4) ABCABC
        if (pin.substring(0, 3).equals(pin.substring(3, 6))) {
            throw new BusinessException(ErrorCode.PIN_CANNOT_CONTAIN_REPEATED_NUMBERS);
        }
    }

    // === 생년월일 기반 ===
    private void validateNotBirthDate(String pin, String birthDate) {
        if (birthDate == null || birthDate.length() != 8) return;
        String yyyy = birthDate.substring(0, 4);
        String yy   = birthDate.substring(2, 4);
        String mm   = birthDate.substring(4, 6);
        String dd   = birthDate.substring(6, 8);

        Set<String> forbidden = Set.of(
                yy + mm + dd,    // 900315
                mm + dd + yy,    // 031590
                dd + mm + yy,    // 150390
                yyyy + mm,       // 199003
                yyyy + dd        // 199015 (드물지만 차단)
        );
        if (forbidden.contains(pin)) {
            throw new BusinessException(ErrorCode.PIN_CANNOT_BE_BIRTH_DATE);
        }
    }

    // === 현재 PIN 과 동일 ===
    private void validateNotSameAsCurrent(String newPin, String currentPinHash) {
        if (currentPinHash != null && pinEncryptor.verify(newPin, currentPinHash)) {
            throw new BusinessException(ErrorCode.NEW_PIN_SAME_AS_CURRENT);
        }
    }

    // === 최근 사용 이력 ===
    private void validateNotRecentlyUsed(String newPin, List<String> previousPinHashes) {
        if (previousPinHashes == null || previousPinHashes.isEmpty()) return;
        for (String prevHash : previousPinHashes) {
            if (prevHash != null && !prevHash.isBlank() && pinEncryptor.verify(newPin, prevHash)) {
                throw new BusinessException(ErrorCode.PIN_RECENTLY_USED);
            }
        }
    }
}
