package com.payauth.payauth.user.adapter.in.web;

import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.in.WithdrawUseCase;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/withdraw")
@RequiredArgsConstructor
public class WithdrawController {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final WithdrawUseCase withdrawUseCase;

    @DeleteMapping
    public ResponseEntity<String> withdraw(HttpServletRequest request) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String accessToken = extractBearer(request);
        withdrawUseCase.withdraw(new WithdrawUseCase.WithdrawCommand(userId, accessToken));
        return ResponseEntity.ok("탈퇴가 완료되었습니다");
    }

    private String extractBearer(HttpServletRequest request) {
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
