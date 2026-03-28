package com.payauth.payauth.user.application.port.in;

import com.payauth.payauth.user.domain.User;

public interface RegisterUserUseCase {

    //User registerUser(RegisterUserCommand command);

    record SignUpVerifyCommand(
            String phoneNumber,
            String verificationCode
    ){}

    record SignUpPinCommand(
            String signUpToken,
            String pinCode
    ){}
}
