package com.payauth.payauth.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 관련 설정값.
 * <pre>
 *   jwt.secret                          : HS256 서명 시크릿 (32바이트 이상 권장)
 *   jwt.access-token-validity-seconds   : Access Token 만료(초)
 *   jwt.refresh-token-validity-seconds  : Refresh Token 만료(초)
 *   jwt.issuer                          : 토큰 issuer 클레임
 * </pre>
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long accessTokenValiditySeconds,
        long refreshTokenValiditySeconds,
        String issuer
) {
}
