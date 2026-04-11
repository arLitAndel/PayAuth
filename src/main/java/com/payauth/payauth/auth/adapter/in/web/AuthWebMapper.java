package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.OtpSendRequest;
import com.payauth.payauth.auth.application.port.in.SendOtpUseCase.sendOtpCommand;

public class AuthWebMapper {

    public sendOtpCommand toSendOtpCommand(OtpSendRequest request) {
        return new sendOtpCommand(
                request.getPhoneNumber(),
                request.getName()
        );
    }

    public verifyPinCommand toVerifyOtpCommand(PinVerifyRequest request) {
        return new VerifyPinCommand(
                request.getUserId(),
                request.getPinNumber(),
                request.getTempToken()
        );
    }
}
