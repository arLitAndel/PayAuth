package com.payauth.payauth.common.identity.adapter;

import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.identity.IdentityVerificationPort;
import com.payauth.payauth.common.identity.IdentityVerificationRequest;
import com.payauth.payauth.common.identity.config.DanalProperties;
import com.payauth.payauth.common.util.MaskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 다날(Danal) 본인인증 어댑터.
 * <p>운영 환경(non-local)에서 identity-verification.provider=danal 일 때 활성화된다.
 *
 * <p>활성화 조건:
 * <ul>
 *   <li>spring.profiles.active != local</li>
 *   <li>identity-verification.provider = danal (또는 미지정 시 기본값)</li>
 * </ul>
 *
 * <p>TODO: 다날 SDK 도입 후 requestVerification 본문에 실제 호출 로직 추가.
 */
@Slf4j
@Component
@Profile("!local")
@ConditionalOnProperty(
        name = "identity-verification.provider",
        havingValue = "danal",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class DanalIdentityVerificationAdapter implements IdentityVerificationPort {

    private final DanalProperties properties;

    @Override
    public void requestVerification(IdentityVerificationRequest request) {
        try {
            // TODO: 다날 본인인증 SDK 호출
            //  1) properties.getCpId() / properties.getApiKey() 로 인증
            //  2) request.purpose() 에 따라 다날 트랜잭션 타입 매핑
            //  3) request.phoneNumber() / request.name() 으로 본인인증 트랜잭션 시작
            //  4) request.verificationCode() 를 다날 측 OTP 와 매핑하거나, 다날이 자체 OTP 를 발송
            //  5) properties.getCallbackUrl() 로 결과 콜백 수신
            log.info(
                    "[DANAL_IDENTITY_VERIFICATION] purpose={}, phone={}, cpId={}",
                    request.purpose(),
                    MaskUtil.maskPhone(request.phoneNumber()),
                    properties.getCpId()
            );
        } catch (Exception e) {
            log.error(
                    "[DANAL_IDENTITY_VERIFICATION_FAIL] phone={}, reason={}",
                    MaskUtil.maskPhone(request.phoneNumber()),
                    e.getMessage()
            );
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
