package com.payauth.payauth.common.identity.adapter;

import com.payauth.payauth.common.identity.IdentityVerificationPort;
import com.payauth.payauth.common.identity.IdentityVerificationRequest;
import com.payauth.payauth.common.util.MaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 로컬/개발 환경 전용 본인인증 어댑터.
 * <p>실제 PG 사를 호출하지 않고 콘솔에 OTP 코드를 출력하여 개발자가 직접 확인할 수 있도록 한다.
 *
 * <p>활성화 조건: spring.profiles.active=local
 */
@Slf4j
@Component
@Profile("local")
public class MockIdentityVerificationAdapter implements IdentityVerificationPort {

    @Override
    public void requestVerification(IdentityVerificationRequest request) {
        log.info(
                "[MOCK_IDENTITY_VERIFICATION] purpose={}, name={}, phone={}, code={}",
                request.purpose(),
                MaskUtil.maskName(request.name()),
                MaskUtil.maskPhone(request.phoneNumber()),
                request.verificationCode()
        );
    }
}
