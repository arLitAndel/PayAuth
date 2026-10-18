# PayAuth

간편결제 서비스를 위한 **사용자 인증 서버**입니다. SMS OTP 본인확인, PIN(간편비밀번호) 기반 인증, JWT 토큰 발급/관리, 계정 잠금 정책, 사용자 활동 이력 감사(Audit) 기능을 제공합니다.

## 주요 기능

### 회원가입 (3단계)
1. `POST /users/signup/request` — 가입 정보 입력 + 본인확인 SMS(OTP) 발송
2. `POST /users/signup/verify` — OTP 검증 → 사용자 저장
3. `POST /users/signup/complete` — PIN(간편비밀번호) 등록 후 가입 완료

### 인증 / 토큰
- `POST /auth/login` — 로그인 (Access/Refresh 토큰 발급)
- `POST /auth/logout` — 로그아웃 (Access 토큰 블랙리스트 처리, Refresh 토큰 폐기)
- `POST /auth/reissue` — Refresh 토큰으로 Access 토큰 재발급
- `POST /auth/otp/send` / `POST /auth/otp/verify` — SMS OTP 발송·검증 (검증 토큰 발급)
- `POST /auth/pin/verify` — PIN 검증 (본인확인 완료)

### PIN 관리
- `POST /auth/pin/change` — PIN 변경 (현재 PIN을 알고 있는 경우)
- PIN 재설정(분실, 강화 본인인증 3단계):
  1. `POST /auth/pin/reset/otp/send` — 본인인증 OTP 발송 (다날/KCB)
  2. `POST /auth/pin/reset/otp/verify` — OTP 검증 → verifiedToken 발급
  3. `POST /auth/pin/reset` — 새 PIN 설정

### 계정 잠금 / 해제
- PIN 5회 연속 실패 시 계정 잠금 (3회 실패 시 경고 응답)
- OTP + PIN 재설정 절차를 통한 잠금 해제
- `/auth/unlock/**` 엔드포인트는 **Deprecated** (PIN 재설정 플로우로 통합 예정)

### 사용자 / 감사
- `DELETE /users/withdraw` — 회원 탈퇴
- `GET /activity/access` — 접속(로그인/로그아웃) 이력 조회
- `GET /activity/auth` — 인증 활동 이력 조회
- `GET /activity/pin-change` — PIN 변경 이력 조회
  - 이력 조회 API는 페이지 크기 기본 20, 최대 100
- `GET /healthCheck` — 헬스체크

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language / Runtime | Java 17 |
| Framework | Spring Boot 4.0.2 (Web MVC, Security, Validation) |
| DB | MariaDB (운영), H2 (테스트/콘솔), Spring Data JPA |
| Cache / 토큰 저장소 | Redis (OTP, Refresh 토큰, 토큰 블랙리스트, 검증 토큰) |
| 인증 | JWT (jjwt 0.12.x), BCrypt (PIN 해시) |
| 외부 연동 | CoolSMS(nurigo SDK) — SMS 발송, 다날/KCB — 본인인증 |
| 기타 | Jasypt (설정 암호화), p6spy (SQL 로깅), Lombok |

## 아키텍처

헥사고날 아키텍처(Ports & Adapters)를 따릅니다. 도메인별 패키지(`auth`, `user`, `audit`) 아래에 `application/port/in`(UseCase), `application/port/out`(Port), `application/service`, `adapter/in/web`, `adapter/out/persistence|redis` 구조로 구성되어 있습니다.

```
src/main/java/com/payauth/payauth
├── auth/       # 로그인/로그아웃, 토큰, OTP, PIN 등록·검증·변경·재설정, 계정 잠금 해제
├── user/       # 회원가입(3단계), 탈퇴
├── audit/      # 사용자 접속/인증 활동 이력
├── common/     # 암호화(CryptoUtil), SMS, 본인인증(다날/KCB/Mock), Redis, 에러 처리, 마스킹/OTP 유틸
└── config/     # Spring Security, JWT 필터, Web MVC 설정
```

## 보안 정책

- **PIN**: 6자리 숫자, `PinPolicy`로 취약 패턴(연속/반복 등) 검증, BCrypt 해시 저장, 최근 3개 PIN 재사용 금지
- **계정 잠금**: PIN 5회 실패 시 잠금, 3회 실패부터 경고
- **OTP**: 유효시간 180초, Redis 저장, 5회 검증 실패 시 30분 차단
- **검증 토큰**: OTP 검증 후 발급되는 verifiedToken 유효시간 5분
- **회원가입 세션**: 가입 세션 3분, OTP 재발송 10분 내 최대 5회, PIN 등록 토큰 10분
- **JWT**: Access 토큰 15분 / Refresh 토큰 14일, 로그아웃 시 블랙리스트 처리
- **개인정보**: JPA Converter(`StringCryptoConverter`) 기반 필드 암호화, 응답 마스킹(`MaskUtil`)
- **설정 암호화**: Jasypt (`JASYPT_SECRET_KEY`)

## 시작하기

### 사전 요구사항
- Java 17
- MariaDB (기본: `localhost:3306/PayAuthDB`)
- Redis (OTP/토큰 저장소)

### 환경 변수

| 변수 | 설명 | 필수 |
|---|---|---|
| `DB_PASSWORD` | MariaDB 비밀번호 | O |
| `DB_USERNAME` | MariaDB 사용자명 (기본값: `payAuth`) | X |
| `JASYPT_SECRET_KEY` | Jasypt 설정 암호화 키 | O |
| `JWT_SECRET` | JWT 서명 키 | 운영 시 O |
| `COOLSMS_API_KEY` / `COOLSMS_API_SECRET` / `COOLSMS_SENDER` | CoolSMS 발송 설정 | 운영 시 O |
| `DANAL_CP_ID` / `DANAL_API_KEY` / `DANAL_CALLBACK_URL` | 다날 본인인증 | provider=danal 시 |
| `KCB_SITE_CODE` / `KCB_LICENSE_KEY` / `KCB_API_URL` | KCB 본인인증 | provider=kcb 시 |

본인인증 모듈은 `identity-verification.provider` 설정으로 선택합니다(`danal`/`kcb`/`mock`).

### 실행

```bash
# 로컬 프로필(기본 활성) — 본인인증은 Mock 어댑터 사용
./gradlew bootRun
```

로컬 전용 설정은 `src/main/resources/application-local.properties`에 두며, 이 파일은 `.gitignore`에 포함되어 있습니다(커밋 금지).

기본 포트는 `8080`입니다. 로컬(`local`) 프로필에서는 `MockIdentityVerificationAdapter`가 활성화되어 다날/KCB 연동 없이 동작합니다.

### 테스트

```bash
./gradlew test
```
