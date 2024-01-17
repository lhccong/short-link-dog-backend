package com.cong.shortlink.model.dto.urlrelate;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑请求
 * # @author <a href="https://github.com/lhccong">程序员聪</a>
 */
@Data
public class UrlRelateEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 长链
     */
    private String longUrl;

    private static final long serialVersionUID = 1L;
}