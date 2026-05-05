package com.payauth.payauth.common.sms;

import com.payauth.payauth.common.util.MaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class MockSmsAdapter implements SmsPort {

    @Override
    public void send(String phoneNumber, String code) {
        log.info("[MOCK SMS] Phone: {} | OTP: {}", MaskUtil.maskPhone(phoneNumber), code);
    }
}
