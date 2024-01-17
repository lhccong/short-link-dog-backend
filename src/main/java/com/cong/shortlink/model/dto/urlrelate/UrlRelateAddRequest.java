package com.cong.shortlink.model.dto.urlrelate;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

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

    private static final long serialVersionUID = 1L;
}