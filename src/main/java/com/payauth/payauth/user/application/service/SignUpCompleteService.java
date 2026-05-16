package com.payauth.payauth.user.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.RegisterPinUseCase;
import com.payauth.payauth.auth.application.service.PinPolicy;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.in.SignUpCompleteUseCase;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.application.port.out.SignUpSessionPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignUpCompleteService implements SignUpCompleteUseCase {

    private final SignUpSessionPort signUpSessionPort;
    private final RegisterPinUseCase registerPinUseCase;
    private final LoadUserPort loadUserPort;
    private final SaveActivityPort saveActivityPort;
    private final PinPolicy pinPolicy;

    @Override
    public User registerPin(signUpCompleteCommand command) {
        Long userId = null;
        try {
            // 1. 임시 토큰으로 userId 조회
            userId = signUpSessionPort
                        .getUserIdByPinRegisterToken(command.signUpToken())
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_SIGNUP_TOKEN));

            // 2. PIN 정책 검증 (신규 가입: 현재 PIN 없음, 이력 없음)
            User user = loadUserPort.loadUser(userId);
            pinPolicy.validate(command.pinCode(), user.getBirthDate(), user.getPhoneNumber(), null, List.of());

            // 3. 핀 저장
            registerPinUseCase.registerPin(userId, command.pinCode());

            // 4. 토큰 삭제 & 유저 반환
            signUpSessionPort.deletePinRegisterToken(command.signUpToken());

            saveActivityPort.record(userId, ActivityCategory.SIGN_UP, ActivityAction.SIGN_UP_COMPLETE, true, null);

            return user;
        } catch (BusinessException e) {
            saveActivityPort.record(userId, ActivityCategory.SIGN_UP, ActivityAction.SIGN_UP_COMPLETE, false, e.getErrorCode().getCode());
            throw e;
        }
    }
}
