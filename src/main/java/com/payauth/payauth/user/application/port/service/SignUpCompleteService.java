package com.payauth.payauth.user.application.port.service;

import com.payauth.payauth.auth.application.port.in.RegisterPinUseCase;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.dto.SignUpSession;
import com.payauth.payauth.user.application.port.in.RegisterUserUseCase;
import com.payauth.payauth.user.application.port.in.SignUpCompleteUseCase;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.application.port.out.SignUpSessionPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignUpCompleteService implements SignUpCompleteUseCase {

    private final SignUpSessionPort signUpSessionPort;
    private final RegisterPinUseCase registerPinUseCase;
    private final LoadUserPort loadUserPort;

    @Override
    public User registerPin(signUpCompleteCommand command) {
        // 1. 임시 토큰으로 userId 조회
        Long userId = signUpSessionPort
                    .getUserIdByPinRegisterToken(command.signUpToken())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_SIGNUP_TOKEN));

        // 2. 핀 저장
        registerPinUseCase.registerPin(userId, command.pinCode());

        // 3. 토큰 삭제 & 유저 반환
        signUpSessionPort.deletePinRegisterToken(command.signUpToken());

        return loadUserPort.loadUser(userId);
    }
}
