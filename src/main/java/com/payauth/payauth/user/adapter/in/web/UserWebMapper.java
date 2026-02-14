package com.payauth.payauth.user.adapter.in.web;

import com.payauth.payauth.user.adapter.in.web.dto.UserResponse;
import com.payauth.payauth.user.adapter.in.web.dto.UserSignUpRequest;
import com.payauth.payauth.user.application.port.in.RegisterUserUseCase;
import com.payauth.payauth.user.domain.User;
import org.springframework.stereotype.Component;


@Component
public class UserWebMapper {

    public RegisterUserUseCase.RegisterUserCommand toCommand(UserSignUpRequest request) {
        return new RegisterUserUseCase.RegisterUserCommand(
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
}
