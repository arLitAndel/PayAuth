package com.payauth.payauth.audit.adapter.in.web;

import com.payauth.payauth.audit.adapter.in.web.dto.ActivityLogResponse;
import com.payauth.payauth.audit.application.port.in.QueryActivityUseCase;
import com.payauth.payauth.audit.application.port.in.QueryActivityUseCase.QueryActivityCommand;
import com.payauth.payauth.audit.domain.ActivityCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 본인의 활동 이력 조회 API.
 *
 * <pre>
 *   GET /activity/access      : 접속 이력 (로그인/로그아웃)
 *   GET /activity/auth        : 인증 이력 (OTP 검증, PIN 인증)
 *   GET /activity/pin-change  : 핀번호 변경 이력
 * </pre>
 */
@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
public class ActivityLogController {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final QueryActivityUseCase queryActivityUseCase;

    @GetMapping("/access")
    public ResponseEntity<Page<ActivityLogResponse>> getAccessHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(queryActivityUseCase.query(
                new QueryActivityCommand(userId, ActivityCategory.ACCESS, page, clampSize(size))));
    }

    @GetMapping("/auth")
    public ResponseEntity<Page<ActivityLogResponse>> getAuthHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(queryActivityUseCase.query(
                new QueryActivityCommand(userId, ActivityCategory.AUTH, page, clampSize(size))));
    }

    @GetMapping("/pin-change")
    public ResponseEntity<Page<ActivityLogResponse>> getPinChangeHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(queryActivityUseCase.query(
                new QueryActivityCommand(userId, ActivityCategory.PIN_CHANGE, page, clampSize(size))));
    }

    private int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }
}
