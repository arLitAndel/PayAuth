package com.payauth.payauth.user.application.port.in;

import com.payauth.payauth.user.domain.User;

public interface SignUpCompleteUseCase {
    User registerPin(signUpCompleteCommand command);

    record signUpCompleteCommand(
            String signUpToken,   // 2단계에서 받은 임시 토큰
            String pinCode        // 등록할 핀번호
    ){}
}
