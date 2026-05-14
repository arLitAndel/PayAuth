package com.payauth.payauth.auth.application.port.in;

public interface SendPinResetOtpUseCase {

    void sendOtp(SendPinResetOtpCommand command);

    record SendPinResetOtpCommand(
            String phoneNumber,
            String name,
            String birthDate
    ) {}
}
