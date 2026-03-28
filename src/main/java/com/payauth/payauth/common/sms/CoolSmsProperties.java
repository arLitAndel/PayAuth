package com.payauth.payauth.common.sms;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@ConfigurationProperties(prefix = "coolsms")
public class CoolSmsProperties {
    private final String apiKey;
    private final String apiSecret;
    private final String sender;

    public CoolSmsProperties(String apiKey, String apiSecret, String sender) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.sender = sender;
    }
}
