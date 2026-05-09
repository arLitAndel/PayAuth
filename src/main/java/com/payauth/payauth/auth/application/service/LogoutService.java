package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.LogoutUseCase;
import com.payauth.payauth.auth.application.port.out.RefreshTokenStorePort;
import com.payauth.payauth.auth.application.port.out.TokenBlacklistPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort.TokenClaims;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 로그아웃 유스케이스 구현.
 *
 * <ol>
 *   <li>요청에 실린 access token 을 파싱(검증) 한다.</li>
 *   <li>access token 이 access 타입이 맞는지 확인한다 (refresh 토큰 오용 방지).</li>
 *   <li>jti 를 토큰 잔여 만료시간만큼 블랙리스트에 등록한다 — 재사용 방지.</li>
 *   <li>해당 사용자의 refresh token 을 Redis 에서 제거 — 신규 access 발급 차단.</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenIssuerPort tokenIssuerPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final RefreshTokenStorePort refreshTokenStorePort;

    @Override
    public void logout(LogoutCommand command) {
        TokenClaims claims = tokenIssuerPort.parse(command.accessToken());

        if (claims.type() != TokenIssuerPort.TokenType.ACCESS) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        long remaining = claims.expiresAtEpochSec() - Instant.now().getEpochSecond();
        tokenBlacklistPort.blacklist(claims.jti(), remaining);

        refreshTokenStorePort.delete(claims.userId());
    }
}
