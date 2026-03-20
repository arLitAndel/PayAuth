package com.payauth.payauth.user.application.port.service;

import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.user.application.port.in.RegisterUserUseCase;
import com.payauth.payauth.user.application.port.out.CheckUserDuplicationPort;
import com.payauth.payauth.user.application.port.out.SaveUserPort;
import com.payauth.payauth.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final CheckUserDuplicationPort checkUserDuplicationPort;
    private final SaveUserPort saveUserPort;

    @Override
    @Transactional
    public User registerUser(RegisterUserCommand command) {
        // 1. 시도 로그 (입력 데이터 확인)
        log.info("[REGISTER_TRY] Attempting to register user. Name: {}, Phone: {}, birth: {}",
                command.name(), command.phoneNumber(), command.birthDate());

        try {

            // 1. 휴대폰 번호 중복 체크
            if (checkUserDuplicationPort.existsByPhoneNumber(command.phoneNumber())) {
                throw new BusinessException(ErrorCode.DUPLICATED_PHONE_NUMBER);
            }

            // 2. 도메인 객체 생성 (검증 로직 포함)
            User newUser = User.createNewUser(
                    command.name(),
                    command.phoneNumber(),
                    command.birthDate()
            );

            // 3. 저장
            User savedUser = saveUserPort.saveUser(newUser);
            log.info("[REGISTER_SUCCESS] Successfully registered user. Name: {}, Phone: {}",
                    savedUser.getName(), savedUser.getPhoneNumber());

            return savedUser;

        } catch (DataIntegrityViolationException e){
            // 3. 실패 로그 (중복 데이터 등 DB 제약 조건 위반)
            log.error("[REGISTER_FAIL] Registration failed due to data conflict. Phone: {}",
                    command.phoneNumber(), e);
            throw new BusinessException(ErrorCode.DUPLICATED_PHONE_NUMBER);

        } catch (Exception e){
            // 4. 실패 로그 (기타 시스템 에러)
            log.error("[REGISTER_ERROR] Unexpected error during registration. Name: {}",
                    command.name(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }
}
