package com.payauth.payauth.auth.application.port.in;

public interface VerifyPinResetOtpUseCase {

    String verifyOtp(VerifyPinResetOtpCommand command);

    record VerifyPinResetOtpCommand(
            String phoneNumber,
            String otp
    ) {}
}
