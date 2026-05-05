package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.ChangePinRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.PinChangeResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.ResetPinRequest;
import com.payauth.payauth.auth.application.port.in.ChangePinUseCase;
import com.payauth.payauth.auth.application.port.in.PinChangeResult;
import com.payauth.payauth.auth.application.port.in.ResetPinUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PIN 라이프사이클 관리 전용 컨트롤러.
 *
 * <pre>
 *   1) POST /auth/pin/change : 현재 PIN 을 알고 있을 때 새 PIN 으로 교체
 *   2) POST /auth/pin/reset  : PIN 분실 시 OTP 본인인증만으로 재설정
 * </pre>
 *
 * <p>두 엔드포인트 모두 사전에 {@code /auth/otp/send → /auth/otp/verify} 단계로
 * 발급된 verifiedToken 이 필요하다. 본인 인증(OTP) 흐름은 {@link AuthController} 에 있다.</p>
 */
@RestController
@RequestMapping("/auth/pin")
@RequiredArgsConstructor
public class PinController {

    private final ChangePinUseCase changePinUseCase;
    private final ResetPinUseCase resetPinUseCase;
    private final AuthWebMapper authWebMapper;

    // 핀번호 변경 (현재 PIN 알 때)
    @PostMapping("/change")
    public ResponseEntity<PinChangeResponse> changePin(@RequestBody @Valid ChangePinRequest request) {
        var command = authWebMapper.toChangePinCommand(request);
        PinChangeResult result = changePinUseCase.changePin(command);
        PinChangeResponse response = authWebMapper.toPinChangeResponse(result);

        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        if (result.isLocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 핀번호 재설정 (분실)
    @PostMapping("/reset")
    public ResponseEntity<PinChangeResponse> resetPin(@RequestBody @Valid ResetPinRequest request) {
        var command = authWebMapper.toResetPinCommand(request);
        resetPinUseCase.resetPin(command);
        return ResponseEntity.ok(
                authWebMapper.toPinChangeResponse(PinChangeResult.success())
        );
    }
}
