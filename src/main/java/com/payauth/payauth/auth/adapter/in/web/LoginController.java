package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.LoginRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.LoginResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.ReissueRequest;
import com.payauth.payauth.auth.application.port.in.LoginUseCase;
import com.payauth.payauth.auth.application.port.in.LoginUseCase.LoginCommand;
import com.payauth.payauth.auth.application.port.in.LogoutUseCase;
import com.payauth.payauth.auth.application.port.in.LogoutUseCase.LogoutCommand;
import com.payauth.payauth.auth.application.port.in.ReissueTokenUseCase;
import com.payauth.payauth.auth.application.port.in.ReissueTokenUseCase.ReissueCommand;
import com.payauth.payauth.auth.application.port.in.TokenPair;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 로그인 / 로그아웃 / 토큰 재발급 엔드포인트.
 *
 * <pre>
 *   POST /auth/login    : verifiedToken + PIN  → access/refresh 발급
 *   POST /auth/logout   : Authorization 헤더 access 토큰 → 블랙리스트 + refresh 폐기
 *   POST /auth/reissue  : refresh 토큰        → 새 access/refresh (rotation)
 * </pre>
 *
 * <p>응답 DTO 매핑은 {@link AuthWebMapper} 에 일임한다.</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;
    private final AuthWebMapper authWebMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        TokenPair pair = loginUseCase.login(new LoginCommand(
                request.getVerifiedToken(),
                request.getPin()
        ));
        return ResponseEntity.ok(authWebMapper.toLoginResponse(pair));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String accessToken = extractBearer(request);
        logoutUseCase.logout(new LogoutCommand(accessToken));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/reissue")
    public ResponseEntity<LoginResponse> reissue(@RequestBody @Valid ReissueRequest request) {
        TokenPair pair = reissueTokenUseCase.reissue(new ReissueCommand(request.getRefreshToken()));
        return ResponseEntity.ok(authWebMapper.toLoginResponse(pair));
    }

    private static String extractBearer(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return token;
    }
}
