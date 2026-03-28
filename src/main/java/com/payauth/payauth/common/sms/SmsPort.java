package com.payauth.payauth.common.sms;

public interface SmsPort {
    void send(String phoneNumber, String message);
}
