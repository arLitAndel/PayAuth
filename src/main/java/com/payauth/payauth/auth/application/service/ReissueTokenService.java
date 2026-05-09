package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.auth.application.port.in.ReissueTokenUseCase;
import com.payauth.payauth.auth.application.port.in.TokenPair;
import com.payauth.payauth.auth.application.port.out.RefreshTokenStorePort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort.TokenClaims;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Refresh Token 으로 신규 Access(+Refresh) 를 발급한다 (Refresh Rotation).
 *
 * <ul>
 *   <li>요청 refresh 의 서명/만료를 검증.</li>
 *   <li>type 클레임이 refresh 인지 확인 (access 토큰 재사용 시도 방지).</li>
 *   <li>Redis 의 활성 refresh 와 1:1 매칭. 일치하지 않으면 탈취 가능성으로 간주해
 *       해당 사용자의 refresh 를 즉시 폐기하고 401 로 거절한다.</li>
 *   <li>일치하면 새 access + 새 refresh 를 발급하고 새 refresh 를 Redis 에 덮어쓴다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ReissueTokenService implements ReissueTokenUseCase {

    private final TokenIssuerPort tokenIssuerPort;
    private final RefreshTokenStorePort refreshTokenStorePort;

    @Override
    public TokenPair reissue(ReissueCommand command) {
        TokenClaims claims = tokenIssuerPort.parse(command.refreshToken());

        if (claims.type() != TokenIssuerPort.TokenType.REFRESH) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = claims.userId();
        String stored = refreshTokenStorePort.load(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!stored.equals(command.refreshToken())) {
            // 저장된 refresh 와 다르면 도용/이중 사용 시나리오 — 즉시 무효화.
            refreshTokenStorePort.delete(userId);
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccess = tokenIssuerPort.createAccessToken(userId);
        String newRefresh = tokenIssuerPort.createRefreshToken(userId);
        refreshTokenStorePort.save(
                userId,
                newRefresh,
                tokenIssuerPort.getRefreshTokenValiditySeconds()
        );

        return new TokenPair(
                newAccess,
                newRefresh,
                tokenIssuerPort.getAccessTokenValiditySeconds(),
                tokenIssuerPort.getRefreshTokenValiditySeconds()
        );
    }
}
