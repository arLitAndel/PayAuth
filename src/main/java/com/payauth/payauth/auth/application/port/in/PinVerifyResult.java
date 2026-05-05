package com.payauth.payauth.auth.application.port.in;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PinVerifyResult {
    private final boolean success;
    private final boolean locked;
    private final int remainingAttempts;
    private final String message;
    private final LocalDateTime lockedUntil;

    public static PinVerifyResult success() {
        return PinVerifyResult.builder()
                .success(true)
                .locked(false)
                .remainingAttempts(0)
                .build();
    }

    public static PinVerifyResult failure(int remainingAttempts) {
        return PinVerifyResult.builder()
                .success(false)
                .locked(false)
                .remainingAttempts(remainingAttempts)
                .message("핀번호가 일치하지 않습니다.")
                .build();
    }

    public static PinVerifyResult failure(int remainingAttempts, String message) {
        return PinVerifyResult.builder()
                .success(false)
                .locked(false)
                .remainingAttempts(remainingAttempts)
                .message(message)
                .build();
    }

    public static PinVerifyResult locked(String message) {
        return PinVerifyResult.builder()
                .success(false)
                .locked(true)
                .remainingAttempts(0)
                .message(message)
                .build();
    }
}
