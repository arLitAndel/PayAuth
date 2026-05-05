package com.payauth.payauth.common.identity;

/**
 * 본인인증 요청을 담는 포트 DTO.
 * <p>
 * UseCase 레이어가 어댑터로 전달하는 입력값으로,
 * 어떤 본인인증 모듈(다날 / KCB / Nurigo / Mock)을 사용하더라도
 * 동일한 형태로 전달된다.
 *
 * @param name              본인인증 대상 회원의 실명
 * @param phoneNumber       본인인증 대상 회원의 휴대폰 번호 (E.164 또는 국내 형식)
 * @param verificationCode  내부 시스템이 생성한 6자리 OTP. 어댑터는 이 값을 사용자에게 전달하거나,
 *                          제 3자(다날/KCB) 모듈의 자체 OTP 발송을 트리거하는 트랜잭션 ID로 사용한다.
 * @param purpose           본인인증을 요청한 도메인 유즈케이스의 목적
 */
public record IdentityVerificationRequest(
        String name,
        String phoneNumber,
        String verificationCode,
        VerificationPurpose purpose
) {
}
