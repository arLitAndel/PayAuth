package com.payauth.payauth.auth.application.port.in;

/**
 * 현재 PIN 을 알고 있는 사용자가 새 PIN 으로 교체하는 UseCase.
 * <p>OTP 본인인증 후 발급된 verifiedToken 이 선행되어야 한다.</p>
 */
public interface ChangePinUseCase {

    PinChangeResult changePin(ChangePinCommand command);

    record ChangePinCommand(
            String verifiedToken,
            String currentPin,
            String newPin,
            String newPinConfirm
    ) {}
}
