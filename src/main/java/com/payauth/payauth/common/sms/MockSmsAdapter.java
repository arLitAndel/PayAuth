package com.payauth.payauth.common.sms;

import com.payauth.payauth.common.util.MaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSmsAdapter implements IdentityVerificationPort {

    @Override
    public void send(String phoneNumber, String code) {
        log.info("[otp 인증] Phone: {} | Code: {}", MaskUtil.maskPhone(phoneNumber), code);
    }
}
