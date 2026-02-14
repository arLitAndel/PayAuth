package com.payauth.payauth.user.adapter.in.web;

import com.payauth.payauth.user.adapter.in.web.dto.UserResponse;
import com.payauth.payauth.user.adapter.in.web.dto.UserSignUpRequest;
import com.payauth.payauth.user.application.port.in.RegisterUserUseCase;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final UserWebMapper userWebMapper;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody UserSignUpRequest request) {
        // 1. DTO를 Command로 변환
        var command = userWebMapper.toCommand(request);

        // 2. 유스케이스 실행
        User registeredUser = registerUserUseCase.registerUser(command);

        // 3. 결과 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(userWebMapper.toResponse(registeredUser));
    }
}
