package com.payauth.payauth.common.sms;

import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.util.MaskUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsAdapter implements SmsPort {

    private final CoolSmsProperties properties;
    private final DefaultMessageService messageService;

    @Override
    public void send(String phoneNumber, String code) {
        try {
            Message message = new Message();
            message.setFrom(properties.getSender());
            message.setTo(phoneNumber);
            message.setText("[서비스명] 인증번호 [" + code + "]를 입력해주세요.");

            messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("[SMS_SENT] Phone: {}", MaskUtil.maskPhone(phoneNumber));

        } catch (Exception e) {
            log.error("[SMS_FAIL] Phone: {}, reason: {}", MaskUtil.maskPhone(phoneNumber), e.getMessage());
            throw new BusinessException(ErrorCode.SMS_SEND_FAILED);
        }
    }
}
