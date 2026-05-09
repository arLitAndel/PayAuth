package com.payauth.payauth.auth.adapter.in.web;

import com.payauth.payauth.auth.adapter.in.web.dto.ChangePinRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.LoginResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.OtpSendRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.OtpVerifyRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.PinChangeResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.PinVerifyRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.PinVerifyResponse;
import com.payauth.payauth.auth.adapter.in.web.dto.ResetPinRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.UnlockAccountRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.UnlockOtpSendRequest;
import com.payauth.payauth.auth.adapter.in.web.dto.UnlockOtpVerifyRequest;
import com.payauth.payauth.auth.application.port.in.ChangePinUseCase.ChangePinCommand;
import com.payauth.payauth.auth.application.port.in.PinChangeResult;
import com.payauth.payauth.auth.application.port.in.PinVerifyResult;
import com.payauth.payauth.auth.application.port.in.ResetPinUseCase.ResetPinCommand;
import com.payauth.payauth.auth.application.port.in.SendOtpUseCase.SendOtpCommand;
import com.payauth.payauth.auth.application.port.in.SendUnlockOtpUseCase.SendUnlockOtpCommand;
import com.payauth.payauth.auth.application.port.in.TokenPair;
import com.payauth.payauth.auth.application.port.in.UnlockAccountUseCase.UnlockAccountCommand;
import com.payauth.payauth.auth.application.port.in.VerifyOtpUseCase.VerifyOtpCommand;
import com.payauth.payauth.auth.application.port.in.VerifyPinUseCase.VerifyPinCommand;
import com.payauth.payauth.auth.application.port.in.VerifyUnlockOtpUseCase.VerifyUnlockOtpCommand;
import org.springframework.stereotype.Component;

@Component
public class AuthWebMapper {

    public SendOtpCommand toSendOtpCommand(OtpSendRequest request) {
        return new SendOtpCommand(
                request.getPhoneNumber(),
                request.getName(),
                request.getBirthDate()
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

    public PinVerifyResponse toPinVerifyResponse(PinVerifyResult result) {
        return PinVerifyResponse.builder()
                .success(result.isSuccess())
                .locked(result.isLocked())
                .remainingAttempts(result.getRemainingAttempts())
                .message(result.getMessage())
                .lockedUntil(result.getLockedUntil())
                .build();
    }

    /**
     * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
     *             {@link #toSendOtpCommand(OtpSendRequest)} 사용.
     */
    @Deprecated(forRemoval = true)
    public SendUnlockOtpCommand toSendUnlockOtpCommand(UnlockOtpSendRequest request) {
        return new SendUnlockOtpCommand(
                request.getPhoneNumber(),
                request.getName(),
                request.getBirthDate()
        );
    }

    /**
     * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
     *             {@link #toVerifyOtpCommand(OtpVerifyRequest)} 사용.
     */
    @Deprecated(forRemoval = true)
    public VerifyUnlockOtpCommand toVerifyUnlockOtpCommand(UnlockOtpVerifyRequest request) {
        return new VerifyUnlockOtpCommand(
                request.getPhoneNumber(),
                request.getOtp()
        );
    }

    /**
     * @deprecated 잠금 해제 흐름이 {@code /auth/pin/reset} 으로 통합되어 폐기 예정.
     *             {@link #toResetPinCommand(ResetPinRequest)} 사용.
     */
    @Deprecated(forRemoval = true)
    public UnlockAccountCommand toUnlockAccountCommand(UnlockAccountRequest request) {
        return new UnlockAccountCommand(
                request.getVerifiedToken()
        );
    }

    public ChangePinCommand toChangePinCommand(ChangePinRequest request) {
        return new ChangePinCommand(
                request.getVerifiedToken(),
                request.getCurrentPin(),
                request.getNewPin(),
                request.getNewPinConfirm()
        );
    }

    public ResetPinCommand toResetPinCommand(ResetPinRequest request) {
        return new ResetPinCommand(
                request.getVerifiedToken(),
                request.getNewPin(),
                request.getNewPinConfirm()
        );
    }

    public PinChangeResponse toPinChangeResponse(PinChangeResult result) {
        return PinChangeResponse.builder()
                .success(result.isSuccess())
                .locked(result.isLocked())
                .remainingAttempts(result.getRemainingAttempts())
                .message(result.getMessage())
                .build();
    }

    /**
     * 로그인/재발급 결과 {@link TokenPair} 를 클라이언트 응답 DTO 로 변환한다.
     *
     * <p>{@code tokenType} 은 항상 {@code "Bearer"} — 클라이언트가
     * {@code Authorization: Bearer <accessToken>} 형식으로 헤더를 구성할 수 있도록 함께 내려준다.</p>
     */
    public LoginResponse toLoginResponse(TokenPair pair) {
        return LoginResponse.builder()
                .accessToken(pair.accessToken())
                .refreshToken(pair.refreshToken())
                .accessTokenExpiresIn(pair.accessTokenExpiresInSec())
                .refreshTokenExpiresIn(pair.refreshTokenExpiresInSec())
                .tokenType("Bearer")
                .build();
    }

}
