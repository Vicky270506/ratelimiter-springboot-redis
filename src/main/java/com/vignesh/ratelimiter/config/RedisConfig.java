package com.vignesh.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory factory){
        return new StringRedisTemplate((factory));
    }

    @Bean
    public DefaultRedisScript<Long> rateLimitScript(){
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText(
                "local current = redis.call('INCR', KEYS[1]) " +
                        "if current == 1 then " +
                        "  redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                        "end " +
                        "return current"
        );
        return script;
    }
}
