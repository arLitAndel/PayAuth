package com.payauth.payauth.auth.application.port.in;

/**
 * 본인인증(OTP) 후 발급된 verifiedToken + PIN 으로 로그인을 수행한다.
 *
 * <p>흐름: {@code /auth/otp/send → /auth/otp/verify(=verifiedToken) → /auth/login}</p>
 *
 * <p>성공 시 access/refresh 토큰을 반환하고 verifiedToken 은 1회용으로 즉시 폐기된다.</p>
 */
public interface LoginUseCase {

    TokenPair login(LoginCommand command);

    record LoginCommand(
            String verifiedToken,
            String pin
    ) {}
}
