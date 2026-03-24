package com.payauth.payauth.common.crypto;

import jakarta.persistence.AttributeConverter; // 여기!
import jakarta.persistence.Converter;        // 여기!
import lombok.RequiredArgsConstructor;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class CryptoUtil {

    // 별도 설정 없이도 Jasypt Starter가 생성한 빈이 자동으로 주입됩니다.
    private final StringEncryptor jasyptStringEncryptor;

    // 주의: 이 솔트값이 바뀌면 기존 유저들의 중복 체크가 불가능해지므로 안전하게 관리해야 합니다.
    private static final String HASH_SALT = "i-can-make-it-hash";

    public String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 고정 솔트와 평문을 결합
            String combined = plainText + HASH_SALT;
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    public String encrypt(String value) {
        return (value == null) ? null : jasyptStringEncryptor.encrypt(value);
    }

    public String decrypt(String value) {
        return (value == null) ? null : jasyptStringEncryptor.decrypt(value);
    }
}