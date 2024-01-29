package com.cong.shortlink.service;

import com.cong.shortlink.model.dto.urltag.UrlTagAddRequest;
import com.cong.shortlink.model.entity.UrlTag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cong.shortlink.model.vo.urltag.UrlTagCategoryVo;
import com.cong.shortlink.model.vo.urltag.UrlTagVo;

import java.util.List;

/**
* @author liuhuaicong
* @description 针对表【url_tag(短链标签表)】的数据库操作Service
* @createDate 2024-01-29 10:24:43
*/
public interface UrlTagService extends IService<UrlTag> {

    /**
     * 获取所有标签
     *
     * @return {@link List}<{@link UrlTagCategoryVo}>
     */
    List<UrlTagCategoryVo> getAllTags();

    /**
     * 添加标签
     *
     * @param userTagAddRequest 用户标签添加请求
     * @return {@link UrlTagVo}
     */
    UrlTagVo addTag(UrlTagAddRequest userTagAddRequest);
}
