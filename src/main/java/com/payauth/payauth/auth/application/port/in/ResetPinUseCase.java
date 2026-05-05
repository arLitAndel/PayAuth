package com.payauth.payauth.auth.application.port.in;

/**
 * PIN 분실 시 본인인증(OTP) 만으로 새 PIN 을 등록하는 UseCase.
 * <p>현재 PIN 입력은 받지 않으며, OTP 본인인증으로 본인 확인을 갈음한다.</p>
 */
public interface ResetPinUseCase {

    void resetPin(ResetPinCommand command);

    record ResetPinCommand(
            String verifiedToken,
            String newPin,
            String newPinConfirm
    ) {}
}
