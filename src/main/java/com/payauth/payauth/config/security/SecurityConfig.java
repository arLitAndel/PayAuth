package com.payauth.payauth.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // REST API이므로 CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/users/signup").permitAll() // 회원가입은 인증 없이 허용
                    .requestMatchers("/users/login").permitAll()  // 로그인도 허용
                    .requestMatchers("/h2-console/**").permitAll()    // H2 콘솔 (개발용)
                    .anyRequest().authenticated()                      // 나머지는 인증 필요
            )
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 사용 시
            )
            .headers(headers ->
                    headers.frameOptions(frame -> frame.disable()) // H2 콘솔용
            );

        return http.build();
    }
}
