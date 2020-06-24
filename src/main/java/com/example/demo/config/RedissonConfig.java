package com.example.demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    /**
     * Gets client.
     *
     * @return the client
     */
    @Bean
    public RedissonClient redissonClient() {
        return Redisson.create();
    }
}
