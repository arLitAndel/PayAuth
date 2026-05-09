package com.payauth.payauth.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReissueRequest {

    @NotBlank(message = "refreshToken 은 필수입니다.")
    private String refreshToken;
}
