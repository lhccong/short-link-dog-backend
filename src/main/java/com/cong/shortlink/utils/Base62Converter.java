package com.cong.shortlink.utils;

/**
 * base62 转换器
 *
 * @author cong
 * @date 2024/01/17
 */
public class Base62Converter {

    private Base62Converter() {
        throw new IllegalStateException("Utility class");
    }

    // 定义 Base62 字符表
    static final char[] BASE62_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public static String toBase62(long hash) {
    // 存储转换后的 Base62 字符串
        StringBuilder result = new StringBuilder();

        // 62进制的基数
        int radix = 62;

        // 将哈希值转换为正数处理
        hash = Math.abs(hash);

        // 将哈希值转换为62进制
        while (hash > 0) {
            int remainder = (int) (hash % radix);
            result.insert(0, BASE62_CHARS[remainder]);
            hash = hash / radix;
        }

        return result.toString();
    }
}
