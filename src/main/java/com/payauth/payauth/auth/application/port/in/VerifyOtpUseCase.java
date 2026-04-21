package com.payauth.payauth.auth.application.port.in;

public interface VerifyOtpUseCase {
    String verifyOtp(VerifyOtpCommand command);

    record VerifyOtpCommand(
            String phoneNumber,
            String otp
    ) {}
}
