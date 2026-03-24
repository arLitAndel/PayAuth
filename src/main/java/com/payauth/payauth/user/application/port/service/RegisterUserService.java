package com.payauth.payauth.user.application.port.service;

import com.payauth.payauth.auth.application.port.in.RegisterPinUseCase;
import com.payauth.payauth.common.crypto.CryptoUtil;
import com.payauth.payauth.common.error.BusinessException;
import com.payauth.payauth.common.error.ErrorCode;
import com.payauth.payauth.common.util.MaskUtil;
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
    private final RegisterPinUseCase registerPinUseCase;
    private final CryptoUtil cryptoUtil;

    @Override
    @Transactional
    public User registerUser(RegisterUserCommand command) {
        // 1. 시도 로그 (입력 데이터 확인)
        log.info("[REGISTER_TRY] Attempting to register user. Name: {}, Phone: {}, birth: {}",
                MaskUtil.maskName(command.name()), MaskUtil.maskPhone(command.phoneNumber()), command.birthDate());

        try {

            // 1. 휴대폰 번호 중복 체크
            // 1. 휴대폰 번호 해싱 (중복 체크용)
            String phoneHash = cryptoUtil.hash(command.phoneNumber());
            if (checkUserDuplicationPort.existsByPhoneNumberHash(phoneHash)){
                log.info("여기에 진입하는지 확인!!");
                throw new BusinessException(ErrorCode.DUPLICATED_PHONE_NUMBER);
            }

            // 2. 도메인 객체 생성 (검증 로직 포함)
            User newUser = User.createNewUser(
                    command.name(),
                    command.phoneNumber(),
                    phoneHash,
                    command.birthDate()
            );

            // 3. 저장
            User savedUser = saveUserPort.saveUser(newUser);
            log.info("[REGISTER_SUCCESS] Successfully registered user. Name: {}, Phone: {}",
                    MaskUtil.maskName(savedUser.getName()), MaskUtil.maskPhone(savedUser.getPhoneNumber()));

            // 4. Auth 도메인 로직: 생성된 userId와 함께 핀번호 저장 명령 전달
            // pinCode는 여기서만 쓰이고 사라짐
            registerPinUseCase.registerPin(savedUser.getId(), command.pinCode());

            return savedUser;

        } catch (BusinessException e) {
            log.warn("[REGISTER_FAIL] Business rule violated. Phone: {}, ErrorCode: {}",
                    MaskUtil.maskPhone(command.phoneNumber()), e.getErrorCode());
            throw e;

        } catch (DataIntegrityViolationException e) {
            log.error("[REGISTER_FAIL] Registration failed due to data conflict. Phone: {}",
                    MaskUtil.maskPhone(command.phoneNumber()), e);
             throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            log.error("[REGISTER_ERROR] Unexpected error during registration. Name: {}",
                    MaskUtil.maskName(command.name()), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }
}
