package com.cong.shortlink.utils;

import com.cong.shortlink.common.ErrorCode;
import com.cong.shortlink.exception.BusinessException;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 网络工具类
 * # @author <a href="https://github.com/lhccong">程序员聪</a>
 */
public class NetUtils {

    private NetUtils() {
        throw new IllegalStateException("Utility class");
    }
    private static final List<String> VALID_DOMAINS = new ArrayList<>();

    // 初始化合法域名列表
    static {
        VALID_DOMAINS.add("www.baidu.com");
        // 添加其他合法域名
    }

    /**
     * 获取 IP 地址
     * 获取客户端 IP 地址
     *
     * @param request 请求
     * @return {@link String}
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if ("127.0.0.1".equals(ip)) {
                // 根据网卡取本机配置的 IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (inet != null) {
                    ip = inet.getHostAddress();
                }
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if (ip == null) {
            return "127.0.0.1";
        }
        return ip;
    }

    public static void validateLink(String longUrl) {
        try {
            URI uri = new URI(longUrl);
            String host = uri.getHost();

            // 校验主域名合法性
            if (!isValidDomain(host)) {
                // 链接不合法（主域名不合法）
                throw new BusinessException(ErrorCode.LINK_ERROR);
            }
            // 校验查询参数中的域名合法性
            String query = uri.getQuery();
            if (query == null) {
                return;
            }
                String[] queryParams = query.split("&");
                for (String param : queryParams) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        String paramName = keyValue[0];
                        String paramValue = keyValue[1];
                        if ("domain".equals(paramName) && !isValidDomain(paramValue)) {
                            // 查询参数中的域名不合法
                            throw new BusinessException(ErrorCode.LINK_ERROR);
                        }
                    }
                }

        } catch (URISyntaxException e) {
            // 链接不合法（URI解析失败）
            throw new BusinessException(ErrorCode.LINK_ERROR);
        }
        // 链接合法
    }

    private static boolean isValidDomain(String domain) {
        return VALID_DOMAINS.contains(domain);
    }
}
