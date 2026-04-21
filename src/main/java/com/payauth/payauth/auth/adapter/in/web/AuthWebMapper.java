package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.OtpSendRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.OtpVerifyRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.PinVerifyRequest;
import com.payauth.payauth.auth.application.port.in.SendOtpUseCase.SendOtpCommand;
import com.payauth.payauth.auth.application.port.in.VerifyOtpUseCase.VerifyOtpCommand;
import com.payauth.payauth.auth.application.port.in.VerifyPinUseCase.VerifyPinCommand;
import org.springframework.stereotype.Component;

@Component
public class AuthWebMapper {

    public SendOtpCommand toSendOtpCommand(OtpSendRequest request) {
        return new SendOtpCommand(
                request.getPhoneNumber(),
                request.getName()
        );
    }

    public VerifyOtpCommand toVerifyOtpCommand(OtpVerifyRequest request) {
        return new VerifyOtpCommand(
                request.getPhoneNumber(),
                request.getOtp()
        );
    }

    public VerifyPinCommand toVerifyPinCommand(PinVerifyRequest request) {
        return new VerifyPinCommand(
                request.getVerifiedToken(),
                request.getPin()
        );
    }

}
