package com.payauth.payauth.common.redis;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payauth.payauth.user.application.port.dto.SignUpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, SignUpSession> signUpSessionRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, SignUpSession> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. 기본 JSON 시리얼라이저를 먼저 세팅
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(RedisSerializer.json());

        // 2. [우회 방법] 만약 SignUpSession이 record라서 역직렬화 에러가 난다면
        // RedisSerializer.json()이 사용하는 기본 ObjectMapper는
        // 보통 프로젝트의 전역 설정을 따르므로, 별도의 ObjectMapper Bean만 등록해주면 해결.

        return template;
    }

    @Bean
    @Primary
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
