package com.payauth.payauth.common.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * 현재 HTTP 요청의 메타데이터를 담는 request-scoped 빈.
 * {@link RequestMetaInterceptor} 가 요청 시작 시 채운다.
 * 감사 로그 어댑터에서 IP, User-Agent, URL 를 읽을 때 사용한다.
 */
@Component
@RequestScope
@Getter
@Setter
public class RequestMeta {

    private String ip;
    private String userAgent;
    private String requestUrl;
}
