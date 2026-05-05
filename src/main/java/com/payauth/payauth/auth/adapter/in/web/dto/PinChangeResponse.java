package com.payauth.payauth.auth.adapter.in.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PinChangeResponse {
    private final boolean success;
    private final boolean locked;
    private final int remainingAttempts;
    private final String message;
}
