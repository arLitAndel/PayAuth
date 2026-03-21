package com.payauth.payauth.auth.application.port.in;

public interface RegisterPinUseCase {
    void registerPin(Long userId, String rawPin);
}
