package com.cong.shortlink.model.dto.urltag;

import lombok.Data;

/**
 * URL 标记添加请求
 *
 * @author cong
 * @date 2024/01/29
 */
@Data
public class UrlTagAddRequest {
    /**
     *父级id
     */
    private Long parentId;
    /**
     *名称
     */
    private String name;
}
