package com.cong.shortlink.model.vo.urltag;

import lombok.Data;

/**
 * URL 标签 vo
 *
 * @author cong
 * @date 2024/01/29
 */
@Data
public class UrlTagVo {
    /**
     * 编号
     */
    private Long id;
    /**
     * 父 ID
     */
    private Long parentId;
    /**
     *名称
     */
    private String name;
    /**
     *颜色
     */
    private String color;
}
