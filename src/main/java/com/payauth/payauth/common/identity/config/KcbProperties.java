package com.payauth.payauth.common.identity.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * KCB(코리아크레딧뷰로) 본인인증 모듈 연동 설정.
 * <p>실제 SDK 도입 시 site code / 라이선스 / API endpoint 등 KCB 발급 값을 외부 환경변수에서 주입한다.
 * 현재는 어댑터 스켈레톤이므로 구조만 잡아둔다.
 */
@Getter
@ConfigurationProperties(prefix = "identity-verification.kcb")
public class KcbProperties {

    /** KCB 에서 발급한 사이트 코드 */
    private final String siteCode;

    /** KCB API 라이선스 키 */
    private final String licenseKey;

    /** KCB 본인인증 API endpoint */
    private final String apiUrl;

    public KcbProperties(String siteCode, String licenseKey, String apiUrl) {
        this.siteCode = siteCode;
        this.licenseKey = licenseKey;
        this.apiUrl = apiUrl;
    }
}
