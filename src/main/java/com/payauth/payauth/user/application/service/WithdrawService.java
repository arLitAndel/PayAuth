package com.payauth.payauth.user.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.out.RefreshTokenStorePort;
import com.payauth.payauth.auth.application.port.out.TokenBlacklistPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.in.WithdrawUseCase;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WithdrawService implements WithdrawUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final TokenIssuerPort tokenIssuerPort;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final RefreshTokenStorePort refreshTokenStorePort;
    private final SaveActivityPort saveActivityPort;

    @Override
    @Transactional
    public void withdraw(WithdrawCommand command) {
        User user = loadUserPort.loadUserById(command.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.delete();
        saveUserPort.saveUser(user);

        // 탈퇴 즉시 현재 토큰 무효화
        TokenIssuerPort.TokenClaims claims = tokenIssuerPort.parse(command.accessToken());
        long remaining = claims.expiresAtEpochSec() - Instant.now().getEpochSecond();
        if (remaining > 0) {
            tokenBlacklistPort.blacklist(claims.jti(), remaining);
        }
        refreshTokenStorePort.delete(command.userId());

        saveActivityPort.record(command.userId(), ActivityCategory.ACCESS, ActivityAction.WITHDRAW, true, null);
    }
}
