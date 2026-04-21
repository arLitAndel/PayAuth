package com.payauth.payauth.auth.application.port.in;

public interface SendOtpUseCase {
    void sendOtp(SendOtpCommand command);

    record SendOtpCommand(
            String phoneNumber,
            String name
    ) {}
}
