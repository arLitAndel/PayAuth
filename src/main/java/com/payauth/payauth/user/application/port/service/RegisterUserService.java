package com.payauth.payauth.user.application.port.service;

import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.in.RegisterUserUseCase;
import com.payauth.payauth.user.application.port.out.CheckUserDuplicationPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final CheckUserDuplicationPort checkUserDuplicationPort;
    private final SaveUserPort saveUserPort;

    @Override
    @Transactional
    public User registerUser(RegisterUserCommand command) {

        // 1. 휴대폰 번호 중복 체크
        if (checkUserDuplicationPort.existsByPhoneNumber(command.phoneNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATED_PHONE_NUMBER);
        }

        // 2. 도메인 객체 생성 (검증 로직 포함)
        User newUser = User.createNewUser(
                command.name(),
                command.phoneNumber(),
                command.birthDate()
        );

        // 3. 저장
        return saveUserPort.saveUser(newUser);
    }
}
