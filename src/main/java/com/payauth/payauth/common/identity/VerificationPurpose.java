package com.payauth.payauth.common.identity;

/**
 * 본인인증을 호출하는 도메인 유즈케이스의 목적.
 * <p>
 * 어댑터(다날 / KCB / Nurigo / Mock 등)는 이 값을 보고
 * 발송 메시지 템플릿이나 PG 사 연동 옵션을 분기할 수 있다.
 */
public enum VerificationPurpose {

    /** 회원가입 시 본인인증 */
    SIGN_UP,

    /** OTP 로그인 시 본인인증 */
    OTP_LOGIN,

    /** 잠긴 계정의 잠금 해제용 본인인증 */
    ACCOUNT_UNLOCK
}
