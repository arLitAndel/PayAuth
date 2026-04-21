package com.payauth.payauth.auth.domain;
import java.time.LocalDateTime;

import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import jdk.jshell.Snippet;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCredential {

    private Long id;
    private Long userId;
    private String pinCode;
    private LocalDateTime expiresAt;
    private Boolean verified;
    private Integer consecutiveFailCount;
    private LocalDateTime blockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    // 회원가입 시 최초 생성
    public static AuthCredential createNewCredential(Long userId, String rawPin, PinEncryptor encryptor) {
        AuthCredential credential = new AuthCredential();
        credential.userId = userId;
        credential.pinCode = encryptor.encrypt(rawPin);
        credential.verified = true;
        credential.consecutiveFailCount = 0;
        credential.blockedUntil = null;
        credential.createdAt = LocalDateTime.now();
        credential.expiresAt = credential.createdAt.plusYears(1); //1년후 만료.
        credential.verifiedAt = LocalDateTime.now();
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

        if (this.consecutiveFailCount >= 5) {
            this.blockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    // 만료 여부
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 차단 여부
    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }

    // 시도 가능 여부
    public boolean canAttempt() {
        return !isExpired() && !isBlocked() && !verified && consecutiveFailCount < 5;
    }

}
