package com.payauth.payauth.auth.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PinVerifyResponse {
    private boolean success;
    private boolean locked;
    private int remainingAttempts;
    private String message;
    private LocalDateTime lockedUntil;
}