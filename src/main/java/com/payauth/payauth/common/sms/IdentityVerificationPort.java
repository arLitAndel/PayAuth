package com.payauth.payauth.common.sms;

public interface IdentityVerificationPort {
    void send(String phoneNumber, String code);
}
