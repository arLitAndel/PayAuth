package com.payauth.payauth.user.domain;

import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String name;
    private String phoneNumber;      //복호화용
    private String phoneNumberHash;  //중복체크용
    private String birthDate;        // YYYYMMDD
    private UserStatus userStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 정적 팩토리 메서드
    public static User createNewUser(String name, String phoneNumber, String phoneNumberHash, String birthDate) {
        validateName(name);
        validatePhoneNumber(phoneNumber);
        validateBirthDate(birthDate);

        User user = new User();
        user.name = name;
        user.phoneNumber = phoneNumber;
        user.phoneNumberHash = phoneNumberHash;
        user.birthDate = birthDate;
        user.userStatus = UserStatus.PENDING;
        user.createdAt = LocalDateTime.now();
        return user;
    }

    // DB 복원용 전용 메서드 (Mapper에서 사용) (찾을 때 사용이었던 걸로 기억함...)
    public static User withId(Long id, String name, String phoneNumber, String phoneNumberHash, String birthDate, UserStatus status, LocalDateTime createdAt, LocalDateTime updatedAt,
                              LocalDateTime deletedAt) {
        return new User(id, name, phoneNumber, phoneNumberHash, birthDate, status,  createdAt, updatedAt, deletedAt);
    }

    // === 비즈니스 로직 ===
    public void updateName(String name) {
        validateName(name);
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePhoneNumber(String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        this.phoneNumber = phoneNumber;
        this.phoneNumberHash = phoneNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.userStatus != UserStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_USER_STATUS);
        }
        this.userStatus = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.userStatus = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void block() {
        this.userStatus = UserStatus.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    public void delete() {
        if (this.userStatus == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_ALREADY_DELETED);
        }
        this.userStatus = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // === 검증 로직 (private) ===
    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_USER_NAME);
        }
        if (name.length() > 50) {
            throw new BusinessException(ErrorCode.USER_NAME_TOO_LONG);
        }
    }

    private static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER);
        }
        // 전화번호 형식 검증 (예: 01012345678)
        if (!phoneNumber.matches("^010\\d{8}$")) {
            throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER_FORMAT);
        }
    }

    private static void validateBirthDate(String birthDate) {
        if (birthDate == null || birthDate.length() != 8) {
            throw new BusinessException(ErrorCode.INVALID_BIRTH_DATE);
        }
        // YYYYMMDD 형식 검증
        if (!birthDate.matches("^\\d{8}$")) {
            throw new BusinessException(ErrorCode.INVALID_BIRTH_DATE_FORMAT);
        }
    }

    // === 상태 체크 ===
    public boolean isActive() {
        return this.userStatus == UserStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.userStatus == UserStatus.DELETED;
    }

    public boolean isBlocked() {
        return this.userStatus == UserStatus.BLOCKED;
    }


}
