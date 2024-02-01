package com.cong.shortlink.constant;

/**
 * Redisson 常数
 *
 * @author cong
 * @date 2024/01/18
 */
public interface RedissonConstants {
    /**
     * 短链接 布隆过滤器 键
     */
    String SHORT_LINK_BLOOM_FILTER_KEY = "short_link_bloom_filter_key";

    /**
     * 短链接缓存键
     */
    String SHORT_LINK_CACHE_MAP_KEY = "short_link_cache_map_key";

    String SHORT_LINK_DISTRIBUTED_ID_KEY = "short_link_distributed_id_key:";


}
