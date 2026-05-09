package com.payauth.payauth.auth.application.port.in;

/**
 * 로그아웃 — Access Token 의 jti 를 블랙리스트에 등록하고 사용자의 Refresh Token 을 폐기한다.
 */
public interface LogoutUseCase {

    void logout(LogoutCommand command);

    record LogoutCommand(String accessToken) {}
}
