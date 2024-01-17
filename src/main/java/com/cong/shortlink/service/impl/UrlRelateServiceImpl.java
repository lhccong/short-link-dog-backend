package com.cong.shortlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cong.shortlink.common.DeleteRequest;
import com.cong.shortlink.common.ErrorCode;
import com.cong.shortlink.constant.CommonConstant;
import com.cong.shortlink.exception.BusinessException;
import com.cong.shortlink.exception.ThrowUtils;
import com.cong.shortlink.mapper.UrlRelateMapper;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateAddRequest;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateQueryRequest;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateUpdateRequest;
import com.cong.shortlink.model.entity.UrlRelate;
import com.cong.shortlink.model.entity.User;
import com.cong.shortlink.service.UrlRelateService;
import com.cong.shortlink.service.UserService;
import com.cong.shortlink.utils.Base62Converter;
import com.cong.shortlink.utils.NetUtils;
import com.cong.shortlink.utils.SqlUtils;
import com.google.common.hash.Hashing;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author liuhuaicong
 * @description 针对表【url_relate(链接关系表)】的数据库操作Service实现
 * @createDate 2024-01-17 14:43:37
 */
@Service
public class UrlRelateServiceImpl extends ServiceImpl<UrlRelateMapper, UrlRelate>
        implements UrlRelateService {
    @Resource
    private UserService userService;

    @Override
    public Long addUrlRelate(UrlRelateAddRequest urlRelateAddRequest) {
        //获取长链
        String longUrl = urlRelateAddRequest.getLongUrl();
        UrlRelate urlRelate = new UrlRelate();
        urlRelate.setLongUrl(longUrl);

        //校验
        this.validUrlRelate(urlRelate, true);

        //生成短链(后续考虑hash冲突)
        String shortUrl = this.createShortUrl(longUrl);

        urlRelate.setSortUrl(shortUrl);
        //TODO 自动解析获取title
        urlRelate.setTitle(null);

        //获取登录用户
        User loginUser = userService.getLoginUser();
        urlRelate.setUserId(loginUser.getId());

        //保存urlRelate到数据库
        boolean result = this.save(urlRelate);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        //返回urlRelate的id
        return urlRelate.getId();

    }

    private String createShortUrl(String longUrl) {
        long shortUrl = Hashing.murmur3_32().hashUnencodedChars(longUrl).padToLong();
        String shortUrlStr64 = Base62Converter.toBase62(shortUrl);
        UrlRelate urlRelate = this.getOne(new LambdaQueryWrapper<UrlRelate>().eq(UrlRelate::getSortUrl, shortUrlStr64));
        if (urlRelate != null) {
            //hash冲突重新生成 在结尾重新添加一个分布式id（暂用62位时间戳）
            shortUrlStr64 = shortUrlStr64 + Base62Converter.toBase62(System.currentTimeMillis());
        }
        return shortUrlStr64;
    }

    @Override
    public void validUrlRelate(UrlRelate urlRelate, boolean add) {
        if (urlRelate == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String longUrl = urlRelate.getLongUrl();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(longUrl), ErrorCode.PARAMS_ERROR);
            //校验长链是否存在
            ThrowUtils.throwIf(this.getOne(new LambdaQueryWrapper<UrlRelate>().eq(UrlRelate::getLongUrl, longUrl)) != null
                    , ErrorCode.PARAMS_ERROR,"长链已存在");
        }
        // 校验长链规则 主域名合法性 查询参数域名合法性
        NetUtils.validateLink(longUrl);
    }

    @Override
    public boolean deleteUrlRelate(DeleteRequest deleteRequest) {
        // 获取当前登录用户
        User user = userService.getLoginUser();
        // 获取要删除的UrlRelate对象的id
        long id = deleteRequest.getId();

        // 判断是否存在
        UrlRelate oldUrlRelate = this.getById(id);
        // 如果UrlRelate对象不存在，则抛出异常
        ThrowUtils.throwIf(oldUrlRelate == null, ErrorCode.NOT_FOUND_ERROR);

        // 仅本人或管理员可删除
        if (!oldUrlRelate.getUserId().equals(user.getId()) && !userService.isAdmin()) {
            // 如果不是本人或管理员，则抛出没有权限的异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 删除指定id的UrlRelate对象，并返回结果
        return this.removeById(id);

    }

    @Override
    public boolean updateUrlRelate(UrlRelateUpdateRequest urlRelateUpdateRequest) {

        // 创建UrlRelate对象
        UrlRelate urlRelate = new UrlRelate();

        // 将urlRelateUpdateRequest对象的属性复制给urlRelate对象
        BeanUtils.copyProperties(urlRelateUpdateRequest, urlRelate);

        // 参数校验
        this.validUrlRelate(urlRelate, false);

        // 获取id属性的值
        long id = urlRelateUpdateRequest.getId();

        // 根据id获取UrlRelate对象
        UrlRelate oldUrlRelate = this.getById(id);

        // 如果获取到的UrlRelate对象为空，则抛出异常，错误码为NOT_FOUND_ERROR
        ThrowUtils.throwIf(oldUrlRelate == null, ErrorCode.NOT_FOUND_ERROR);

        // 更新urlRelate对象到数据库
        return this.updateById(urlRelate);


    }

    @Override
    public QueryWrapper<UrlRelate> getQueryWrapper(UrlRelateQueryRequest urlRelateQueryRequest) {

        // 创建查询封装类
        QueryWrapper<UrlRelate> queryWrapper = new QueryWrapper<>();
        // 如果urlRelateQueryRequest为空，则直接返回queryWrapper
        if (urlRelateQueryRequest == null) {
            return queryWrapper;
        }
        // 获取id字段的值
        Long id = urlRelateQueryRequest.getId();

        // 获取title字段的值
        String title = urlRelateQueryRequest.getTitle();

        // 获取longUrl字段的值
        String longUrl = urlRelateQueryRequest.getLongUrl();

        // 获取userId字段的值
        Long userId = urlRelateQueryRequest.getUserId();

        // 获取sortField字段的值
        String sortField = urlRelateQueryRequest.getSortField();

        // 获取sortOrder字段的值
        String sortOrder = urlRelateQueryRequest.getSortOrder();

        // 拼接查询条件
        // 如果title不为空，则在queryWrapper中like条件查询title字段
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);

        // 如果longUrl不为空，则在queryWrapper中like条件查询longUrl字段
        queryWrapper.like(StringUtils.isNotBlank(longUrl), "longUrl", longUrl);

        // 如果id不为空，则在queryWrapper中eq条件查询id字段
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);

        // 如果userId不为空，则在queryWrapper中eq条件查询userId字段
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);

        // 根据sortField和sortOrder进行排序
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        // 返回queryWrapper
        return queryWrapper;

    }

}




