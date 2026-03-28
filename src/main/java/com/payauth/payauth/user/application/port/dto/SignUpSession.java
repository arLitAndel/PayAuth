package com.payauth.payauth.user.application.port.dto;

import java.time.LocalDate;

public record SignUpSession (
        String name,
        String birthDate,
        String verificationCode
){ }
