package com.payauth.payauth.auth.application.service;

import com.payauth.payauth.audit.application.port.out.SaveActivityPort;
import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;
import com.payauth.payauth.auth.application.port.in.SendOtpUseCase;
import com.payauth.payauth.auth.application.port.out.SaveOtpPort;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.sms.SmsPort;
import com.payauth.payauth.common.util.OtpUtil;
import com.payauth.payauth.user.application.port.out.LoadUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendOtpService implements SendOtpUseCase {

    private static final long OTP_TTL_SECONDS = 180;

    private final LoadUserPort loadUserPort;
    private final SaveOtpPort saveOtpPort;
    private final SmsPort smsPort;
    private final CryptoUtil cryptoUtil;
    private final SaveActivityPort saveActivityPort;

    @Override
    public void sendOtp(SendOtpCommand command) {
        Long userId = null;
        try {
            String phoneNumber = command.phoneNumber();
            String phoneHash = cryptoUtil.hash(phoneNumber);

            User user = loadUserPort.findByPhoneNumberHash(phoneHash)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_AUTH_INFO));
            userId = user.getUserId();

            if (!user.getName().equals(command.name()) || !user.getBirthDate().equals(command.birthDate())) {
                throw new BusinessException(ErrorCode.INVALID_AUTH_INFO);
            }
            if (!user.isActive()) {
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            }

            String otp = OtpUtil.generateOtp();
            saveOtpPort.save(phoneNumber, otp, OTP_TTL_SECONDS);
            smsPort.send(phoneNumber, otp);

            saveActivityPort.record(userId, ActivityCategory.AUTH, ActivityAction.OTP_SEND, true, null);

        } catch (BusinessException e) {
            saveActivityPort.record(userId, ActivityCategory.AUTH, ActivityAction.OTP_SEND, false, e.getErrorCode().getCode());
            throw e;
        }
    }
}
