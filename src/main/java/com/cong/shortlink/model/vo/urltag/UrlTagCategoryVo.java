package com.cong.shortlink.model.vo.urltag;

import lombok.Data;

import java.util.List;

/**
 * URL 标签类别 VO
 *
 * @author cong
 * @date 2024/01/29
 */
@Data
public class UrlTagCategoryVo {
    private Long id;

    private String name;

    private List<UrlTagVo> tags;
}