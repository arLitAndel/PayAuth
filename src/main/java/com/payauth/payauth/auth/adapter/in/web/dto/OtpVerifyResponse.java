package com.payauth.payauth.auth.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpVerifyResponse {
    private final String verifiedToken;
}
