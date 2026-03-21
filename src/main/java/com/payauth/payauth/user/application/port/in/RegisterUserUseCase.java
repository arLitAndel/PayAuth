package com.payauth.payauth.user.application.port.in;

import com.payauth.payauth.user.domain.User;

public interface RegisterUserUseCase {

    User registerUser(RegisterUserCommand command);

    // 입력값을 캡슐화한 내부 클래스 (Command)
    record RegisterUserCommand(
            String phoneNumber,
            String name,
            String birthDate,
            String pinCode
    ) {}
}
