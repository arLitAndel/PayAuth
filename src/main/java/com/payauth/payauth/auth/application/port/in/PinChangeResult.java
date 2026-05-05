package com.payauth.payauth.auth.application.port.in;

import lombok.Builder;
import lombok.Getter;

/**
 * 현재 PIN 검증을 거치는 변경 흐름의 결과.
 * <p>5회 누적 실패 시 계정 잠금({@code locked=true}) 상태를 표현하기 위해
 * {@link PinVerifyResult} 와 유사한 구조를 따른다.</p>
 */
@Getter
@Builder
public class PinChangeResult {
    private final boolean success;
    private final boolean locked;
    private final int remainingAttempts;
    private final String message;

    public static PinChangeResult success() {
        return PinChangeResult.builder()
                .success(true)
                .locked(false)
                .remainingAttempts(0)
                .message("핀번호가 정상적으로 변경되었습니다.")
                .build();
    }

    public static PinChangeResult failure(int remainingAttempts, String message) {
        return PinChangeResult.builder()
                .success(false)
                .locked(false)
                .remainingAttempts(remainingAttempts)
                .message(message)
                .build();
    }

    public static PinChangeResult locked(String message) {
        return PinChangeResult.builder()
                .success(false)
                .locked(true)
                .remainingAttempts(0)
                .message(message)
                .build();
    }
}
