package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.OtpSendRequest;
import com.payauth.payauth.auth.application.port.in.SendOtpUseCase;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/otp")
@RequiredArgsConstructor
public class AuthController {
    private final SendOtpUseCase sendOtpUseCase;
    //private final VerifyOtpUseCase verifyOtpUseCase;
    //private final VerifyPinUseCase verifyPinUseCase;
    private final AuthWebMapper authWebMapper;

    // 1단계: OTP 발송
    @PostMapping("/send")
    public ResponseEntity<String> sendOtp( @RequestBody @Valid OtpSendRequest request) {
        var command = authWebMapper.toSendOtpCommand(request);
        sendOtpUseCase.sendOtp(command);
        return ResponseEntity.ok("인증 번호를 발송하였습니다");
    }
}
