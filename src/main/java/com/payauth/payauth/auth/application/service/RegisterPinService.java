package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.RegisterPinUseCase;
import com.payauth.payauth.auth.application.port.out.PinEncryptor;
import com.payauth.payauth.auth.application.port.out.SaveAuthCredentialPort;
import com.payauth.payauth.auth.domain.AuthCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterPinService implements RegisterPinUseCase {
    private final SaveAuthCredentialPort saveAuthCredentialPort;
    private final PinEncryptor pinEncryptor;

    @Override
    public void registerPin(Long userId, String rawPin) {
        AuthCredential credential = AuthCredential.createNewCredential(userId, rawPin, pinEncryptor);
        saveAuthCredentialPort.saveAuthCredential(credential);
    }

}
