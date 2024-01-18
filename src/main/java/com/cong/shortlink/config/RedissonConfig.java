package com.cong.shortlink.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
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
    @Bean
    public Redisson redisson(){
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setDatabase(0);
//                .setPassword("123456");
        return (Redisson) Redisson.create(config);
    }
}
