package com.payauth.payauth.user.application.port.in;

public interface SignUpRequestUseCase {
    void request(SignUpRequestCommand command);

    // 입력값을 캡슐화한 내부 클래스 (Command)
    record SignUpRequestCommand(
            String phoneNumber,
            String name,
            String birthDate
    ) {}
}