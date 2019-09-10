package com.yuyuko.idempotent.autoconfigure;

import com.yuyuko.idempotent.annotation.IdempotentScanner;
import com.yuyuko.idempotent.redis.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 自动配置类，依赖于外部的redis配置
 */
@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class IdempotentAutoConfiguration {
    @Bean
    public IdempotentScanner idempotentScanner() {
        return new IdempotentScanner(idempotentRedisUtils());
    }

    @Autowired
    RedisConnectionFactory connectionFactory;

    @Bean
    public RedisUtils idempotentRedisUtils() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return new RedisUtils(redisTemplate);
    }
}