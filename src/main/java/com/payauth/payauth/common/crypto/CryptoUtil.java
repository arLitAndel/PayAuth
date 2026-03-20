package com.payauth.payauth.common.crypto;

import jakarta.persistence.AttributeConverter; // 여기!
import jakarta.persistence.Converter;        // 여기!
import lombok.RequiredArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CryptoUtil {

    // 별도 설정 없이도 Jasypt Starter가 생성한 빈이 자동으로 주입됩니다.
    private final StringEncryptor jasyptStringEncryptor;

    public String encrypt(String value) {
        return (value == null) ? null : jasyptStringEncryptor.encrypt(value);
    }

    public String decrypt(String value) {
        return (value == null) ? null : jasyptStringEncryptor.decrypt(value);
    }
}