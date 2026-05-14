package com.payauth.payauth.audit.application.port.in;

import com.payauth.payauth.audit.adapter.in.web.dto.ActivityLogResponse;
import com.payauth.payauth.audit.domain.ActivityCategory;
import org.springframework.data.domain.Page;

public interface QueryActivityUseCase {

    Page<ActivityLogResponse> query(QueryActivityCommand command);

    record QueryActivityCommand(
            Long userId,
            ActivityCategory category,
            int page,
            int size
    ) {}
}
