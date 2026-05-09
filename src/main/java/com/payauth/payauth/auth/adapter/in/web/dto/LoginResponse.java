package com.payauth.payauth.auth.adapter.in.web.dto;

import lombok.Builder;

/**
 * 로그인/재발급 성공 응답 DTO.
 *
 * @param accessToken          Authorization 헤더에 {@code Bearer ...} 형식으로 사용
 * @param refreshToken         access 만료 시 /auth/reissue 에 사용
 * @param accessTokenExpiresIn accessToken 만료까지 남은 초 (클라이언트 만료 타이머용)
 * @param refreshTokenExpiresIn refreshToken 만료까지 남은 초
 * @param tokenType            항상 "Bearer"
 */
@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        String tokenType
) {
}
