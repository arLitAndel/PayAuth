package com.payauth.payauth.audit.application.service;

import com.payauth.payauth.audit.adapter.in.web.dto.ActivityLogResponse;
import com.payauth.payauth.audit.adapter.out.persistence.ActivityLogJpaEntity;
import com.payauth.payauth.audit.application.port.in.QueryActivityUseCase;
import com.payauth.payauth.audit.application.port.out.LoadActivityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService implements QueryActivityUseCase {

    private final LoadActivityPort loadActivityPort;

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> query(QueryActivityCommand command) {
        PageRequest pageable = PageRequest.of(command.page(), command.size());

        Page<ActivityLogJpaEntity> page = (command.category() != null)
                ? loadActivityPort.findByUserIdAndCategory(command.userId(), command.category(), pageable)
                : loadActivityPort.findByUserId(command.userId(), pageable);

        return page.map(this::toResponse);
    }

    private ActivityLogResponse toResponse(ActivityLogJpaEntity e) {
        return new ActivityLogResponse(
                e.getId(),
                e.getCategory(),
                e.getAction(),
                e.getRequestUrl(),
                e.getIp(),
                e.getUserAgent(),
                e.isSuccess(),
                e.getFailReason(),
                e.getCreatedAt()
        );
    }
}
