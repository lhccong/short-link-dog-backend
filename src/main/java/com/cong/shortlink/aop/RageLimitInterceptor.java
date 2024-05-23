package com.cong.shortlink.aop;

import cn.dev33.satoken.stp.StpUtil;
import com.cong.shortlink.annotation.BlacklistInterceptor;
import com.cong.shortlink.common.ErrorCode;
import com.cong.shortlink.exception.BusinessException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
public class RageLimitInterceptor {
    private final Redisson redisson;

    private RMapCache<String, Long> blacklist;
    // 用来存储用户ID与对应的RateLimiter对象
    private final Cache<String, RRateLimiter> userRateLimiters = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    public RageLimitInterceptor(Redisson redisson) {
        this.redisson = redisson;
        if (redisson != null) {
            log.info("Redisson object is not null, using Redisson...");
            // 使用 Redisson 对象执行相关操作
            // 个人限频黑名单24h
            blacklist = redisson.getMapCache("blacklist");
            blacklist.expire(24, TimeUnit.HOURS);// 设置过期时间
        } else {
            log.error("Redisson object is null!");
        }
    }


    @Pointcut("@annotation(com.cong.shortlink.annotation.BlacklistInterceptor)")
    public void aopPoint() {
    }

    @Around("aopPoint() && @annotation(blacklistInterceptor)")
    public Object doRouter(ProceedingJoinPoint jp, BlacklistInterceptor blacklistInterceptor) throws Throwable {
        String key = blacklistInterceptor.key();

        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取 IP
        String remoteHost = httpServletRequest.getRemoteHost();
        if (StringUtils.isBlank(key)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "拦截的 key 不能为空");
        }
        // 获取拦截字段
        String keyTarget;
        if (key.equals("default")) {
            keyTarget = "SystemUid" + StpUtil.getLoginId().toString();
        } else {
            keyTarget = getAttrValue(key, jp.getArgs());
        }

        log.info("标识是 {}", keyTarget);

        // 黑名单拦截
        if (blacklistInterceptor.protectLimit() != 0 && null != blacklist.getOrDefault(keyTarget, null) && (blacklist.getOrDefault(keyTarget, 0L) > blacklistInterceptor.protectLimit()
                ||blacklist.getOrDefault(remoteHost, 0L) > blacklistInterceptor.protectLimit())) {
            log.info("有小黑子被我抓住了！给他 24 小时封禁套餐吧：{}", keyTarget);
            return fallbackMethodResult(jp, blacklistInterceptor.fallbackMethod());
        }

        // 获取限流
        RRateLimiter rateLimiter;
        if (!userRateLimiters.asMap().containsKey(keyTarget)) {
            rateLimiter = redisson.getRateLimiter(keyTarget);
            // 设置RateLimiter的速率，每秒发放10个令牌
            rateLimiter.trySetRate(RateType.OVERALL, blacklistInterceptor.rageLimit(), 1, RateIntervalUnit.SECONDS);
            userRateLimiters.put(keyTarget, rateLimiter);
        } else {
            rateLimiter = userRateLimiters.getIfPresent(keyTarget);
        }

        // 限流拦截
        if (rateLimiter != null && !rateLimiter.tryAcquire()) {
            if (blacklistInterceptor.protectLimit() != 0) {
                //封标识
                blacklist.put(keyTarget, blacklist.getOrDefault(keyTarget, 0L) + 1L);
                //封 IP
                blacklist.put(remoteHost, blacklist.getOrDefault(remoteHost, 0L) + 1L);
            }
            log.info("你刷这么快干嘛黑子：{}", keyTarget);
            return fallbackMethodResult(jp, blacklistInterceptor.fallbackMethod());
        }

        // 返回结果
        return jp.proceed();
    }

    private Object fallbackMethodResult(JoinPoint jp, String fallbackMethod) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Signature sig = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) sig;
        Method method = jp.getTarget().getClass().getMethod(fallbackMethod, methodSignature.getParameterTypes());
        return method.invoke(jp.getThis(), jp.getArgs());
    }

    /**
     * 实际根据自身业务调整，主要是为了获取通过某个值做拦截
     */
    public String getAttrValue(String attr, Object[] args) {
        if (args[0] instanceof String) {
            return args[0].toString();
        }
        String filedValue = null;
        for (Object arg : args) {
            try {
                if (StringUtils.isNotBlank(filedValue)) {
                    break;
                }
                filedValue = String.valueOf(this.getValueByName(arg, attr));
            } catch (Exception e) {
                log.error("获取路由属性值失败 attr：{}", attr, e);
            }
        }
        return filedValue;
    }

    /**
     * 获取对象的特定属性值
     *
     * @param item 对象
     * @param name 属性名
     * @return 属性值
     * @author tang
     */
    private Object getValueByName(Object item, String name) {
        try {
            Field field = getFieldByName(item, name);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(item);
            field.setAccessible(false);
            return o;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 根据名称获取方法，该方法同时兼顾继承类获取父类的属性
     *
     * @param item 对象
     * @param name 属性名
     * @return 该属性对应方法
     * @author tang
     */
    private Field getFieldByName(Object item, String name) {
        try {
            Field field;
            try {
                field = item.getClass().getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                field = item.getClass().getSuperclass().getDeclaredField(name);
            }
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }


}
