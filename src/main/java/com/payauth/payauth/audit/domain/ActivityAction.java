package com.payauth.payauth.audit.domain;

public enum ActivityAction {
    SIGN_UP_REQUEST,
    SIGN_UP_VERIFY,
    SIGN_UP_COMPLETE,
    OTP_SEND,
    PIN_RESET_OTP_SEND,
    PIN_RESET_OTP_VERIFY,
    LOGIN,
    LOGOUT,
    OTP_VERIFY,
    PIN_VERIFY,
    PIN_CHANGE,
    PIN_RESET
}
