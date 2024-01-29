package com.cong.shortlink.model.dto.urlrelate;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建请求
 * # @author <a href="https://github.com/lhccong">程序员聪</a>
 */
@Data
public class UrlRelateAddRequest implements Serializable {

    /**
     * 长链
     */
    private String longUrl;
    /**
     * 链接标题
     */
    private String title;

    /**
     * 链接图标
     */
    private String urlImg;
    /**
     * 标签列表（json 数组）
     */
    private String tags;
    /**
     * 允许访问次数
     */
    private Integer allowNum;

    /**
     * 是否私密
     */
    private Integer privateTarget;

    /**
     * 密码
     */
    private String password;

    /**
     * 过期时间
     */
    private Date expireTime;

    private static final long serialVersionUID = 1L;
}