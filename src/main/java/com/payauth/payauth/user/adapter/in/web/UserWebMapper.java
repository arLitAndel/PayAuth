package com.payauth.payauth.user.adapter.in.web;

import com.payauth.payauth.user.adapter.in.web.dto.*;
import com.payauth.payauth.user.application.port.in.SignUpCompleteUseCase;
import com.payauth.payauth.user.application.port.in.SignUpRequestUseCase.SignUpRequestCommand;
import com.payauth.payauth.user.application.port.in.SignUpVerifyUseCase.SignUpVerifyCommand;
import com.payauth.payauth.user.application.port.in.SignUpCompleteUseCase.signUpCompleteCommand;
import com.payauth.payauth.user.domain.User;
import org.springframework.stereotype.Component;


@Component
public class UserWebMapper {

    public SignUpRequestCommand toRequestCommand(SignUpRequest request) {
        return new SignUpRequestCommand(
                request.phoneNumber(),
                request.name(),
                request.birthDate()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getUserStatus().name()
        );
    }

    public SignUpVerifyCommand toVerifyCommand(SignUpVerifyRequest request) {
        return new SignUpVerifyCommand(
                request.verificationId(),
                request.verificationCode()
        );
    }

    public signUpCompleteCommand toPinCommand(SignUpCompleteRequest request) {
        return new signUpCompleteCommand(
                request.signUpToken(),
                request.pinCode()
        );
    }

}
