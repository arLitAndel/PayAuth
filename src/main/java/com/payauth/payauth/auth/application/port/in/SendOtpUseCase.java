package com.payauth.payauth.auth.application.port.in;

import jakarta.security.auth.message.AuthException;

public interface SendOtpUseCase {
    void sendOtp(sendOtpCommand command);

    record sendOtpCommand(
            String phoneNumber,
            String name
    ) {}
}
