package com.payauth.payauth.user.adapter.in.web.dto;

public record UserResponse(
        Long id,
        String name,
        String phoneNumber,
        String userStatus
) {}