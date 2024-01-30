package com.cong.shortlink.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 配置
 *
 * @author cong
 * @date 2024/01/18
 */
@Configuration
public class RedissonConfig {
    @Value("${spring.redis.host}")
    private String redissonUrl;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database}")
    private Integer database;

    @Bean
    public Redisson redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redissonUrl + ":" + port)
                .setDatabase(database)
                .setPassword(password);
        return (Redisson) Redisson.create(config);
    }
}
