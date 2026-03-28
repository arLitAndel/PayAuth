package com.payauth.payauth.user.application.port.in;

import java.time.LocalDateTime;

public interface SignUpVerifyUseCase {
    Result verify(SignUpVerifyCommand command);

    // 입력값을 캡슐화한 내부 클래스 (Command)
    record SignUpVerifyCommand(
            String phoneNumber,
            String verificationCode
    ) {}
    // 반환값을 캡슐화한 내부 클래스 (Result)
    record Result(
            String signUpToken,
            LocalDateTime expiredAt
    ) {}
}
