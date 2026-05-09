package com.payauth.payauth.config.security;

import com.payauth.payauth.auth.application.port.out.TokenBlacklistPort;
import com.payauth.payauth.auth.application.port.out.TokenIssuerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    TokenIssuerPort tokenIssuerPort,
                                    TokenBlacklistPort tokenBlacklistPort) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(tokenIssuerPort, tokenBlacklistPort);

        http
            .csrf(AbstractHttpConfigurer::disable) // REST API 이므로 CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/users/signup/**").permitAll() // 회원가입은 인증 없이 허용
                    .requestMatchers("/users/login").permitAll()     // (legacy) 로그인 경로
                    // 본인인증/PIN/로그인/재발급 — 미인증 상태에서 호출되는 엔드포인트
                    .requestMatchers(
                            "/auth/otp/send",
                            "/auth/otp/verify",
                            "/auth/pin/verify",
                            "/auth/pin/reset",
                            "/auth/login",
                            "/auth/reissue",
                            "/auth/unlock/**"
                    ).permitAll()
                    // 로그아웃, PIN 변경 등은 access token 을 가진 인증된 사용자만
                    .requestMatchers("/auth/logout", "/auth/pin/change").authenticated()
                    .requestMatchers("/h2-console/**").permitAll()   // H2 콘솔 (개발용)
                    .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers ->
                    headers.frameOptions(frame -> frame.disable())
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
