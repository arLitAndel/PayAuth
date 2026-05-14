package com.payauth.payauth.audit.adapter.in.web.dto;

import com.payauth.payauth.audit.domain.ActivityAction;
import com.payauth.payauth.audit.domain.ActivityCategory;

import java.time.LocalDateTime;

public record ActivityLogResponse(
        Long id,
        ActivityCategory category,
        ActivityAction action,
        String requestUrl,
        String ip,
        String userAgent,
        boolean success,
        String failReason,
        LocalDateTime createdAt
) {}
