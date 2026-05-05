package com.payauth.payauth.common.identity.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 다날 본인인증 모듈 연동 설정.
 * <p>실제 SDK 도입 시 CP_ID / API Key / Callback URL 등 PG 사 발급 값을 외부 환경변수에서 주입한다.
 * 현재는 어댑터 스켈레톤이므로 구조만 잡아둔다.
 */
@Getter
@ConfigurationProperties(prefix = "identity-verification.danal")
public class DanalProperties {

    /** 다날에서 발급한 가맹점(CP) 식별자 */
    private final String cpId;

    /** 다날 본인인증 API 호출 시 사용하는 인증 키 */
    private final String apiKey;

    /** 본인인증 결과 콜백을 받을 URL */
    private final String callbackUrl;

    public DanalProperties(String cpId, String apiKey, String callbackUrl) {
        this.cpId = cpId;
        this.apiKey = apiKey;
        this.callbackUrl = callbackUrl;
    }
}
