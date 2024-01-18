package com.cong.shortlink.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cong.shortlink.common.DeleteRequest;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateAddRequest;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateQueryRequest;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateUpdateRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cong.shortlink.model.entity.UrlRelate;

/**
* @author liuhuaicong
* @description 针对表【url_relate(链接关系表)】的数据库操作Service
* @createDate 2024-01-17 14:43:37
*/
public interface UrlRelateService extends IService<UrlRelate> {

    /**
     * 添加 URL 关联
     *
     * @param urlRelateAddRequest URL 关联添加请求
     * @return {@link Long}
     */
    Long addUrlRelate(UrlRelateAddRequest urlRelateAddRequest);

    /**
     * 有效 URL 关联
     *
     * @param urlRelate URL 关联
     * @param b         b
     */
    void validUrlRelate(UrlRelate urlRelate, boolean b);

    /**
     * 删除 URL 关联
     *
     * @param deleteRequest 删除请求
     * @return boolean
     */
    boolean deleteUrlRelate(DeleteRequest deleteRequest);

    /**
     * 更新 URL 关联
     *
     * @param urlRelateUpdateRequest URL 关联更新请求
     * @return boolean
     */
    boolean updateUrlRelate(UrlRelateUpdateRequest urlRelateUpdateRequest);

    QueryWrapper<UrlRelate> getQueryWrapper(UrlRelateQueryRequest urlRelateQueryRequest);

    /**
     * 获取长链接
     *
     * @param shortLink 短链接
     * @return {@link String}
     */
    String getLongLink(String shortLink);
}
