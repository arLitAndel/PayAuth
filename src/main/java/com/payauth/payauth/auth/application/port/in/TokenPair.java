package com.payauth.payauth.auth.application.port.in;

/**
 * 로그인/재발급 결과로 반환되는 Access + Refresh 토큰 쌍.
 *
 * @param accessToken               단기 인증 토큰 (Authorization: Bearer 헤더로 사용)
 * @param refreshToken              장기 재발급 토큰 (Access 만료 시 /auth/reissue 에 사용)
 * @param accessTokenExpiresInSec   accessToken 의 만료까지 남은 초 (클라이언트 만료 타이머용)
 * @param refreshTokenExpiresInSec  refreshToken 의 만료까지 남은 초
 */
public record TokenPair(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSec,
        long refreshTokenExpiresInSec
) {
}
