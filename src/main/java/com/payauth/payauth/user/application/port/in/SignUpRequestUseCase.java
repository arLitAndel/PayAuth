package com.payauth.payauth.user.application.port.in;

import com.payauth.payauth.user.adapter.in.web.dto.SignUpResponse;

public interface SignUpRequestUseCase {
    SignUpResponse request(SignUpRequestCommand command);

    // 입력값을 캡슐화한 내부 클래스 (Command)
    record SignUpRequestCommand(
            String phoneNumber,
            String name,
            String birthDate
    ) {}
}