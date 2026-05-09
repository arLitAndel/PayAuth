package com.payauth.payauth.config.security;

import com.payauth.payauth.auth.application.port.out.TokenBlacklistPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort.TokenClaims;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authorization: Bearer 헤더의 access token 을 검증해 SecurityContext 에 인증 정보를 채워주는 필터.
 *
 * <p>여기서 토큰을 거절하는 정책은 다음과 같다.</p>
 * <ul>
 *   <li>토큰이 없으면 그대로 통과 — 미인증 요청은 SecurityConfig 의 authorizeHttpRequests 가 판단.</li>
 *   <li>토큰이 있는데 서명/만료가 잘못되면 401 응답으로 즉시 차단.</li>
 *   <li>refresh 타입 토큰을 access 처럼 쓰려고 하면 차단.</li>
 *   <li>블랙리스트(로그아웃 처리)된 토큰이면 차단.</li>
 * </ul>
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenIssuerPort tokenIssuerPort;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearer(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TokenClaims claims = tokenIssuerPort.parse(token);

            if (claims.type() != TokenIssuerPort.TokenType.ACCESS) {
                writeUnauthorized(response, ErrorCode.INVALID_TOKEN);
                return;
            }
            if (tokenBlacklistPort.isBlacklisted(claims.jti())) {
                writeUnauthorized(response, ErrorCode.BLACKLISTED_TOKEN);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            claims.userId(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (BusinessException e) {
            writeUnauthorized(response, e.getErrorCode());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearer(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":\"" + code.getCode() + "\",\"message\":\"" + code.getMessage() + "\"}"
        );
    }
}
