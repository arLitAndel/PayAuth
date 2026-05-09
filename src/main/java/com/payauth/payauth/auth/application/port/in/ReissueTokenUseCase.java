package com.payauth.payauth.auth.application.port.in;

/**
 * Refresh Token 으로 새 Access(+Refresh) 토큰을 재발급 한다.
 *
 * <p>요청 refresh 가 Redis 에 저장된 사용자별 활성 refresh 와 일치할 때만 통과시키며,
 * 재발급과 동시에 새 refresh 로 회전(rotation) 시켜 탈취 시도에 대비한다.</p>
 */
public interface ReissueTokenUseCase {

    TokenPair reissue(ReissueCommand command);

    record ReissueCommand(String refreshToken) {}
}
