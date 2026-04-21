package com.payauth.payauth.auth.application.port.in;


public interface VerifyPinUseCase {
    PinVerifyResult verifyPin(VerifyPinCommand command);

    record VerifyPinCommand(
            String verifiedToken,
            String pin
    ) {}
}
