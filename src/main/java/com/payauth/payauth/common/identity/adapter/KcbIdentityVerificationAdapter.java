package com.payauth.payauth.common.identity.adapter;

import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.identity.IdentityVerificationPort;
import com.payauth.payauth.common.identity.IdentityVerificationRequest;
import com.payauth.payauth.common.identity.config.KcbProperties;
import com.payauth.payauth.common.util.MaskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * KCB(코리아크레딧뷰로) 본인인증 어댑터.
 * <p>운영 환경(non-local)에서 identity-verification.provider=kcb 일 때 활성화된다.
 *
 * <p>활성화 조건:
 * <ul>
 *   <li>spring.profiles.active != local</li>
 *   <li>identity-verification.provider = kcb</li>
 * </ul>
 *
 * <p>TODO: KCB SDK 도입 후 requestVerification 본문에 실제 호출 로직 추가.
 */
@Slf4j
@Component
@Profile("!local")
@ConditionalOnProperty(name = "identity-verification.provider", havingValue = "kcb")
@RequiredArgsConstructor
public class KcbIdentityVerificationAdapter implements IdentityVerificationPort {

    private final KcbProperties properties;

    @Override
    public void requestVerification(IdentityVerificationRequest request) {
        try {
            // TODO: KCB 본인인증 SDK 호출
            //  1) properties.getSiteCode() / properties.getLicenseKey() 로 인증
            //  2) properties.getApiUrl() 에 본인인증 요청
            //  3) request.purpose() 에 따라 KCB 트랜잭션 타입 매핑
            //  4) request.phoneNumber() / request.name() 으로 본인인증 트랜잭션 시작
            //  5) KCB 측 자체 OTP 발송 또는 request.verificationCode() 매핑 처리
            log.info(
                    "[KCB_IDENTITY_VERIFICATION] purpose={}, phone={}, siteCode={}",
                    request.purpose(),
                    MaskUtil.maskPhone(request.phoneNumber()),
                    properties.getSiteCode()
            );
        } catch (Exception e) {
            log.error(
                    "[KCB_IDENTITY_VERIFICATION_FAIL] phone={}, reason={}",
                    MaskUtil.maskPhone(request.phoneNumber()),
                    e.getMessage()
            );
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
