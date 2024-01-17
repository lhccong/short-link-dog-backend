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

    // 62进制使用的字符集
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String toBase62(long decimalNumber) {
        StringBuilder base62Number = new StringBuilder();

        if (decimalNumber == 0) {
            return "0";
        }

        while (decimalNumber > 0) {
            int remainder = (int) (decimalNumber % 62);
            base62Number.insert(0, BASE62.charAt(remainder));
            decimalNumber /= 62;
        }

        return base62Number.toString();
    }
}
