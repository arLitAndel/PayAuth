package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.ChangePinRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.OtpVerifyResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.PinChangeResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.PinResetOtpSendRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.PinResetOtpVerifyRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.ResetPinRequest;
import com.payauth.payauth.auth.application.port.in.ChangePinUseCase;
import com.payauth.payauth.auth.application.port.in.PinChangeResult;
import com.payauth.payauth.auth.application.port.in.ResetPinUseCase;
import com.payauth.payauth.auth.application.port.in.SendPinResetOtpUseCase;
import com.payauth.payauth.auth.application.port.in.VerifyPinResetOtpUseCase;
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
    private final SendPinResetOtpUseCase sendPinResetOtpUseCase;
    private final VerifyPinResetOtpUseCase verifyPinResetOtpUseCase;
    private final AuthWebMapper authWebMapper;

    // PIN 재설정 강화 본인인증 — 1단계: OTP 발송 (다날/KCB)
    @PostMapping("/reset/otp/send")
    public ResponseEntity<String> sendPinResetOtp(@RequestBody @Valid PinResetOtpSendRequest request) {
        var command = authWebMapper.toSendPinResetOtpCommand(request);
        sendPinResetOtpUseCase.sendOtp(command);
        return ResponseEntity.ok("인증 번호를 발송하였습니다");
    }

    // PIN 재설정 강화 본인인증 — 2단계: OTP 검증 → verifiedToken 발급
    @PostMapping("/reset/otp/verify")
    public ResponseEntity<OtpVerifyResponse> verifyPinResetOtp(@RequestBody @Valid PinResetOtpVerifyRequest request) {
        var command = authWebMapper.toVerifyPinResetOtpCommand(request);
        String verifiedToken = verifyPinResetOtpUseCase.verifyOtp(command);
        return ResponseEntity.ok(new OtpVerifyResponse(verifiedToken));
    }

    // PIN 재설정 (분실) - 3단계 : PIN 변경
    @PostMapping("/reset")
    public ResponseEntity<PinChangeResponse> resetPin(@RequestBody @Valid ResetPinRequest request) {
        var command = authWebMapper.toResetPinCommand(request);
        resetPinUseCase.resetPin(command);
        return ResponseEntity.ok(
                authWebMapper.toPinChangeResponse(PinChangeResult.success())
        );
    }

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
}
