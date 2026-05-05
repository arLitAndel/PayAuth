package com.payauth.payauth.common.identity;

/**
 * 본인인증 서비스에 대한 추상화 포트 (Hexagonal Architecture - Outbound Port).
 *
 * <p>도메인/유즈케이스 레이어는 이 포트만 의존하며, 실제 구현체(다날, KCB, Mock 등)는
 * Infrastructure Layer 의 어댑터로 분리된다.
 *
 * <p>구현체는 다음과 같이 선택된다.
 * <ul>
 *   <li>로컬/개발 환경 (spring.profiles.active=local) → MockIdentityVerificationAdapter</li>
 *   <li>운영 환경 + identity-verification.provider=danal → DanalIdentityVerificationAdapter (default)</li>
 *   <li>운영 환경 + identity-verification.provider=kcb   → KcbIdentityVerificationAdapter</li>
 * </ul>
 *
 * <p>Nurigo SDK 기반 {@link com.payauth.payauth.common.sms.SmsPort} 는 본 포트와 별개의
 * 일반 SMS(알림/공지) 발송 채널로 보존된다.
 */
public interface IdentityVerificationPort {

    /**
     * 본인인증을 요청한다. 실제 구현체에 따라 SMS 발송, 다날 PG 호출, KCB API 호출 등으로 분기된다.
     * 발송 실패 시 {@link com.payauth.payauth.common.error.BusinessException} 을 던져
     * UseCase 가 트랜잭션 보상(예: OTP 삭제, 세션 카운트 롤백) 을 수행할 수 있도록 한다.
     *
     * @param request 본인인증 요청 정보
     */
    void requestVerification(IdentityVerificationRequest request);
}
