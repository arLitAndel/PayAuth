package com.payauth.payauth.user.application.port.in;

public interface WithdrawUseCase {

    void withdraw(WithdrawCommand command);

    record WithdrawCommand(Long userId, String accessToken) {}
}
