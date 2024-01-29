package com.cong.shortlink.model.dto.urlrelate;

import com.cong.shortlink.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
/**
 * 查询请求
 * # @author <a href="https://github.com/lhccong">程序员聪</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UrlRelateQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 长链
     */
    private String longUrl;

    /**
     * 创建用户 id
     */
    private Long userId;


    private static final long serialVersionUID = 1L;
}