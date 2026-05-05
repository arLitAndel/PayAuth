package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.UnlockAccountRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.UnlockAccountResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.UnlockOtpSendRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.UnlockOtpVerifyRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.UnlockOtpVerifyResponse;
import com.payauth.payauth.auth.application.port.in.SendUnlockOtpUseCase;
import com.payauth.payauth.auth.application.port.in.UnlockAccountUseCase;
import com.payauth.payauth.auth.application.port.in.VerifyUnlockOtpUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>[Deprecated]</b> 핀번호 5회 실패로 잠긴 계정의 잠금 해제 플로우 컨트롤러.
 *
 * <h3>폐기 사유</h3>
 * 잠금 해제 단독 흐름은 PIN 분실 사용자에게 실효성이 낮다(잠금만 풀어도 PIN 을 모르면 다시 잠김).
 * {@code POST /auth/pin/reset} 이 LOCKED 상태도 받아 PIN 재설정과 잠금 해제를 한 번에 처리하므로
 * 본 컨트롤러는 점진적으로 폐기될 예정이다.
 *
 * <h3>마이그레이션</h3>
 * <pre>
 *   AS-IS (Deprecated)
 *     POST /auth/unlock/otp/send
 *     POST /auth/unlock/otp/verify
 *     POST /auth/unlock/account
 *
 *   TO-BE
 *     POST /auth/otp/send
 *     POST /auth/otp/verify
 *     POST /auth/pin/reset       ← LOCKED 사용자도 동일 엔드포인트로 처리됨
 * </pre>
 *
 * <p>본 컨트롤러는 호출 시 응답에 {@code Deprecation: true} 와 {@code Sunset} 헤더를 함께 내려준다.</p>
 */
@Deprecated(forRemoval = true)
@Slf4j
@RestController
@RequestMapping("/auth/unlock")
@RequiredArgsConstructor
public class AccountUnlockController {

    /** RFC 8594 — 단순한 boolean 신호. */
    private static final String DEPRECATION_HEADER = "Deprecation";
    /** RFC 8594 — 폐기 예정 시점. 최종 일정은 운영 정책에 맞게 조정. */
    private static final String SUNSET_HEADER = "Sunset";
    private static final String SUNSET_VALUE = "Wed, 31 Dec 2025 23:59:59 GMT";
    private static final String LINK_HEADER_VALUE =
            "</auth/pin/reset>; rel=\"successor-version\"";

    private final SendUnlockOtpUseCase sendUnlockOtpUseCase;
    private final VerifyUnlockOtpUseCase verifyUnlockOtpUseCase;
    private final UnlockAccountUseCase unlockAccountUseCase;
    private final AuthWebMapper authWebMapper;

    // 1단계: 본인인증 OTP 발송
    @Deprecated(forRemoval = true)
    @PostMapping("/otp/send")
    public ResponseEntity<String> sendUnlockOtp(@RequestBody @Valid UnlockOtpSendRequest request) {
        warnDeprecated("/auth/unlock/otp/send");
        var command = authWebMapper.toSendUnlockOtpCommand(request);
        sendUnlockOtpUseCase.sendUnlockOtp(command);
        return deprecated(ResponseEntity.ok())
                .body("계정 잠금 해제용 인증 번호를 발송하였습니다.");
    }

    // 2단계: OTP 검증 → 잠금 해제 전용 verifiedToken 반환
    @Deprecated(forRemoval = true)
    @PostMapping("/otp/verify")
    public ResponseEntity<UnlockOtpVerifyResponse> verifyUnlockOtp(@RequestBody @Valid UnlockOtpVerifyRequest request) {
        warnDeprecated("/auth/unlock/otp/verify");
        var command = authWebMapper.toVerifyUnlockOtpCommand(request);
        String verifiedToken = verifyUnlockOtpUseCase.verifyUnlockOtp(command);
        return deprecated(ResponseEntity.ok())
                .body(new UnlockOtpVerifyResponse(verifiedToken));
    }

    // 3단계: 계정 잠금 해제 (LOCKED → ACTIVE)
    @Deprecated(forRemoval = true)
    @PostMapping("/account")
    public ResponseEntity<UnlockAccountResponse> unlockAccount(@RequestBody @Valid UnlockAccountRequest request) {
        warnDeprecated("/auth/unlock/account");
        var command = authWebMapper.toUnlockAccountCommand(request);
        unlockAccountUseCase.unlockAccount(command);
        return deprecated(ResponseEntity.ok())
                .body(new UnlockAccountResponse(true, "계정 잠금이 해제되었습니다."));
    }

    private void warnDeprecated(String endpoint) {
        log.warn("[DEPRECATED_ENDPOINT] {} 는 폐기 예정입니다. /auth/pin/reset 사용을 권장합니다.", endpoint);
    }

    private ResponseEntity.BodyBuilder deprecated(ResponseEntity.BodyBuilder builder) {
        return builder
                .header(DEPRECATION_HEADER, "true")
                .header(SUNSET_HEADER, SUNSET_VALUE)
                .header(HttpHeaders.LINK, LINK_HEADER_VALUE);
    }
}
