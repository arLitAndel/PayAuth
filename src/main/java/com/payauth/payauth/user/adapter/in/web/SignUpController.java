package com.payauth.payauth.user.adapter.in.web;

import com.payauth.payauth.user.adapter.in.web.dto.*;
import com.payauth.payauth.user.application.port.in.SignUpCompleteUseCase;
import com.payauth.payauth.user.application.port.in.SignUpRequestUseCase;
import com.payauth.payauth.user.application.port.in.SignUpVerifyUseCase;
import com.payauth.payauth.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/signup")
@RequiredArgsConstructor
public class SignUpController {

    private final SignUpRequestUseCase signUpRequestUseCase;
    private final SignUpVerifyUseCase signUpVerifyUseCase;
    private final SignUpCompleteUseCase signUpCompleteUseCase;
    private final UserWebMapper userWebMapper;

    // 1단계: 정보 입력 + SMS 발송
    @PostMapping("/request")
    public ResponseEntity<Void> requestSignUp(@Valid @RequestBody SignUpRequest request) {

        var command = userWebMapper.toRequestCommand(request);
        signUpRequestUseCase.request(command);

        return ResponseEntity.accepted().build();
    }

    // 2단계: SMS 인증 확인 → user 저장
    @PostMapping("/verify")
    public ResponseEntity<SignUpVerifyResponse> verifySignUp(@Valid @RequestBody SignUpVerifyRequest request) {

        var command = userWebMapper.toVerifyCommand(request);
        SignUpVerifyUseCase.Result result  = signUpVerifyUseCase.verify(command);// signUpToken: 핀번호 등록 단계에서 쓸 임시 토큰

        return ResponseEntity.ok(new SignUpVerifyResponse(result.signUpToken(), result.expiredAt()));
    }

    // 3단계: 핀번호 등록
    @PostMapping("/complete")
    public ResponseEntity<UserResponse> registerPin(
            @Valid @RequestBody SignUpCompleteRequest request) {

        var command = userWebMapper.toPinCommand(request);
        User user = signUpCompleteUseCase.registerPin(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userWebMapper.toResponse(user));
    }
}
