package com.payauth.payauth.auth.domain;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCredential {

    /** 보관할 PIN 이력(현재 제외) 최대 개수. 초과분은 가장 오래된 것부터 폐기. */
    public static final int PIN_HISTORY_LIMIT = 3;

    private Long userId;
    private String pinCode;
    private LocalDateTime expiresAt;
    private Boolean verified;
    private Integer consecutiveFailCount;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    /** 직전에 사용했던 PIN 의 bcrypt 해시들 (최근 → 과거 순, 최대 PIN_HISTORY_LIMIT 개). */
    @Builder.Default
    private List<String> previousPinHashes = new ArrayList<>();

    // 회원가입 시 최초 생성
    public static AuthCredential createNewCredential(Long userId, String rawPin, PinEncryptor encryptor) {
        AuthCredential credential = new AuthCredential();
        credential.userId = userId;
        credential.pinCode = encryptor.encrypt(rawPin);
        credential.verified = true;
        credential.consecutiveFailCount = 0;
        credential.createdAt = LocalDateTime.now();
        credential.expiresAt = credential.createdAt.plusYears(1); //1년후 만료.
        credential.verifiedAt = LocalDateTime.now();
        credential.previousPinHashes = new ArrayList<>();
        return credential;
    }

    // 인증 성공 시
    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
        this.consecutiveFailCount = 0;
    }

    // 인증 실패 시
    public void incrementFailCount() {
        this.consecutiveFailCount++;
    }

    /**
     * 본인인증(SMS OTP)을 통한 계정 잠금 해제 시 호출.
     * 누적 실패 횟수와 차단 시간을 모두 초기화한다.
     */
    public void resetForUnlock() {
        this.consecutiveFailCount = 0;
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * PIN 변경/재설정 시 호출.
     * <ul>
     *     <li>현재 pinCode 를 이력 맨 앞에 push (중복/blank 는 무시).</li>
     *     <li>이력은 최근 {@link #PIN_HISTORY_LIMIT} 개까지만 유지.</li>
     *     <li>새 PIN 으로 갱신 + 만료 1년 연장 + 실패 카운터/차단 초기화.</li>
     * </ul>
     * <p>새 PIN 자체의 정책 위반(연속/반복/이력 중복 등)은 호출 전에 검증되어야 한다.</p>
     */
    public void changePin(String rawNewPin, PinEncryptor encryptor) {
        if (this.previousPinHashes == null) {
            this.previousPinHashes = new ArrayList<>();
        }
        if (this.pinCode != null && !this.pinCode.isBlank()) {
            this.previousPinHashes.add(0, this.pinCode);
            while (this.previousPinHashes.size() > PIN_HISTORY_LIMIT) {
                this.previousPinHashes.remove(this.previousPinHashes.size() - 1);
            }
        }

        this.pinCode = encryptor.encrypt(rawNewPin);
        LocalDateTime now = LocalDateTime.now();
        this.expiresAt = now.plusYears(1);
        this.verified = true;
        this.verifiedAt = now;
        this.consecutiveFailCount = 0;
    }

    /** 외부 노출용 — 정책 검증기에서 읽기 전용으로 사용. */
    public List<String> getPreviousPinHashesView() {
        return previousPinHashes == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(previousPinHashes);
    }

    // 만료 여부
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }


}
