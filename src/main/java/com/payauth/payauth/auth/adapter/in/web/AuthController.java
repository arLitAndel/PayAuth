package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.*;
import com.payauth.payauth.auth.application.port.in.PinVerifyResult;
import com.payauth.payauth.auth.application.port.in.SendOtpUseCase;
import com.payauth.payauth.auth.application.port.in.VerifyOtpUseCase;
import com.payauth.payauth.auth.application.port.in.VerifyPinUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 본인 인증 흐름 전용 컨트롤러.
 *
 * <pre>
 *   1) POST /auth/otp/send    : SMS OTP 발송
 *   2) POST /auth/otp/verify  : OTP 검증 → verifiedToken 발급
 *   3) POST /auth/pin/verify  : verifiedToken + PIN 으로 본인 확인
 * </pre>
 *
 * <p>PIN 변경/재설정은 {@link PinController}, 계정 잠금 해제는 {@code AccountUnlockController} 에 있다.</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SendOtpUseCase sendOtpUseCase;
    private final VerifyOtpUseCase verifyOtpUseCase;
    private final VerifyPinUseCase verifyPinUseCase;
    private final AuthWebMapper authWebMapper;

    // 1단계: OTP 발송
    @PostMapping("/otp/send")
    public ResponseEntity<String> sendOtp( @RequestBody @Valid OtpSendRequest request) {
        var command = authWebMapper.toSendOtpCommand(request);
        sendOtpUseCase.sendOtp(command);
        return ResponseEntity.ok("인증 번호를 발송하였습니다");
    }

    // 2단계: OTP 검증 → 검증 토큰 반환
    @PostMapping("/otp/verify")
    public ResponseEntity<OtpVerifyResponse> verifyOtp(@RequestBody @Valid OtpVerifyRequest request) {
        var command = authWebMapper.toVerifyOtpCommand(request);
        String verifiedToken = verifyOtpUseCase.verifyOtp(command);
        return ResponseEntity.ok(new OtpVerifyResponse(verifiedToken));
    }

    // 3단계: 핀번호 검증 (본인확인 완료)
    @PostMapping("/pin/verify")
    public ResponseEntity<PinVerifyResponse> verifyPin(@RequestBody @Valid PinVerifyRequest request) {
        var command = authWebMapper.toVerifyPinCommand(request);
        PinVerifyResult result = verifyPinUseCase.verifyPin(command);
        PinVerifyResponse response = authWebMapper.toPinVerifyResponse(result);

        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
