package com.payauth.payauth.auth.domain;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthCredential {

    private Long id;
    private Long userId;
    private String pinCode;
    private LocalDateTime expiresAt;
    private Boolean verified;
    private Integer attemptCount;
    private Integer consecutiveFailCount;
    private LocalDateTime blockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    // 인증 성공 시
    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
        this.consecutiveFailCount = 0;
    }

    // 인증 실패 시
    public void incrementFailCount() {
        this.attemptCount++;
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
