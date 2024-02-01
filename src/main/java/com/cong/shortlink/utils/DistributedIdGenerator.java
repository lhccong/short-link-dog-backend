package com.cong.shortlink.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.cong.shortlink.constant.RedissonConstants.SHORT_LINK_DISTRIBUTED_ID_KEY;

/**
 * 分布式 ID 生成器
 *
 * @author cong
 * @date 2024/02/01
 */
@Component
public class DistributedIdGenerator {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public Long generateDistributedId(String key) {
        return stringRedisTemplate.opsForValue().increment(SHORT_LINK_DISTRIBUTED_ID_KEY + key);
    }
}