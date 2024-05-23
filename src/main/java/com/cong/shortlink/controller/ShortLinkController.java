package com.cong.shortlink.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cong.shortlink.annotation.AuthCheck;
import com.cong.shortlink.annotation.BlacklistInterceptor;
import com.cong.shortlink.common.BaseResponse;
import com.cong.shortlink.common.DeleteRequest;
import com.cong.shortlink.common.ErrorCode;
import com.cong.shortlink.common.ResultUtils;
import com.cong.shortlink.constant.SystemConstants;
import com.cong.shortlink.constant.UserConstant;
import com.cong.shortlink.exception.BusinessException;
import com.cong.shortlink.exception.ThrowUtils;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateAddRequest;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateQueryRequest;
import com.cong.shortlink.model.dto.urlrelate.UrlRelateUpdateRequest;
import com.cong.shortlink.model.entity.UrlRelate;
import com.cong.shortlink.model.entity.User;
import com.cong.shortlink.model.vo.shortlink.UrlRelateVo;
import com.cong.shortlink.service.UrlRelateService;
import com.cong.shortlink.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 短链接口
 * # @author <a href="https://github.com/lhccong">程序员聪</a>
 */
@RestController
@RequestMapping("/short_link")
@Slf4j
//@Api(tags = "短链管理")
public class ShortLinkController {

    @Resource
    private UrlRelateService urlRelateService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 添加短链
     *
     * @param urlRelateAddRequest 发布添加请求
     * @return {@link BaseResponse}<{@link Long}>
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加短链")
    @BlacklistInterceptor(fallbackMethod = "loginErr", rageLimit = 2L, protectLimit = 3)
    public BaseResponse<Long> addUrlRelate(@RequestBody UrlRelateAddRequest urlRelateAddRequest) {
        if (urlRelateAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long newUrlRelateId = urlRelateService.addUrlRelate(urlRelateAddRequest);

        return ResultUtils.success(newUrlRelateId);
    }

    /**
     * 删除短链
     *
     * @param deleteRequest 删除请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除短链")
    public BaseResponse<Boolean> deleteUrlRelate(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(urlRelateService.deleteUrlRelate(deleteRequest));
    }

    /**
     * 更新（仅管理员）
     *
     * @param urlRelateUpdateRequest 发布更新请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新")
    public BaseResponse<Boolean> updateUrlRelate(@RequestBody UrlRelateUpdateRequest urlRelateUpdateRequest) {
        if (urlRelateUpdateRequest == null || urlRelateUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(urlRelateService.updateUrlRelate(urlRelateUpdateRequest));
    }

    /**
     * 根据 id 获取
     *
     * @param id 编号
     * @return {@link BaseResponse}<{@link UrlRelate}>
     */
    @GetMapping("/get/vo")
    @ApiOperation(value = "根据 id 获取")
    public BaseResponse<UrlRelate> getUrlRelateVoById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UrlRelate urlRelate = urlRelateService.getById(id);
        if (urlRelate == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(urlRelate);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param urlRelateQueryRequest 发布查询请求
     * @return {@link BaseResponse}<{@link Page}<{@link UrlRelate}>>
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "分页获取列表（仅管理员）")
    public BaseResponse<Page<UrlRelate>> listUrlRelateByPage(@RequestBody UrlRelateQueryRequest urlRelateQueryRequest) {
        long current = urlRelateQueryRequest.getCurrent();
        long size = urlRelateQueryRequest.getPageSize();
        Page<UrlRelate> urlRelatePage = urlRelateService.page(new Page<>(current, size),
                urlRelateService.getQueryWrapper(urlRelateQueryRequest));
        return ResultUtils.success(urlRelatePage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param urlRelateQueryRequest 发布查询请求
     * @return {@link BaseResponse}<{@link Page}<{@link UrlRelate}>>
     */
    @PostMapping("/list/page/vo")
    @ApiOperation(value = "分页获取列表（封装类）")
    public BaseResponse<Page<UrlRelateVo>> listUrlRelateVoByPage(@RequestBody UrlRelateQueryRequest urlRelateQueryRequest) {
        if (!StpUtil.isLogin() || StpUtil.getTokenSession().get(SystemConstants.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        long current = urlRelateQueryRequest.getCurrent();
        long size = urlRelateQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UrlRelate> urlRelatePage = urlRelateService.page(new Page<>(current, size),
                urlRelateService.getQueryWrapper(urlRelateQueryRequest));
        Page<UrlRelateVo> urlRelateVoPage = new Page<>(current, size, urlRelatePage.getTotal());
        List<UrlRelateVo> urlRelateVo = urlRelateService.getUrlRelateVo(urlRelatePage.getRecords());
        urlRelateVoPage.setRecords(urlRelateVo);
        return ResultUtils.success(urlRelateVoPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param urlRelateQueryRequest 发布查询请求
     * @return {@link BaseResponse}<{@link Page}<{@link UrlRelate}>>
     */
    @PostMapping("/my/list/page/vo")
    @ApiOperation(value = "分页获取当前用户创建的资源列表")
    public BaseResponse<Page<UrlRelate>> listMyUrlRelateVoByPage(@RequestBody UrlRelateQueryRequest urlRelateQueryRequest) {
        if (urlRelateQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser();
        urlRelateQueryRequest.setUserId(loginUser.getId());
        long current = urlRelateQueryRequest.getCurrent();
        long size = urlRelateQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<UrlRelate> urlRelatePage = urlRelateService.page(new Page<>(current, size),
                urlRelateService.getQueryWrapper(urlRelateQueryRequest));
        return ResultUtils.success(urlRelatePage);
    }

    // endregion

    /**
     * 根据短链获取长链
     *
     * @param shortLink 短链
     * @return {@link BaseResponse}<{@link UrlRelate}>
     */
    @GetMapping("/getByShort")
    @ApiOperation(value = "根据短链获取长链")
    public BaseResponse<UrlRelateVo> getUrlRelateVoByShortLink(String shortLink) {
        if (CharSequenceUtil.isBlank(shortLink)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UrlRelateVo urlRelateVo = urlRelateService.getByShortLink(shortLink);
        if (urlRelateVo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(urlRelateVo);
    }

    @BlacklistInterceptor(key = "title", fallbackMethod = "loginErr", rageLimit = 1L, protectLimit = 10)
    @PostMapping("/login")
    public String login(@RequestBody UrlRelateAddRequest urlRelateAddRequest) {
        log.info("模拟登录 title:{}", urlRelateAddRequest.getTitle());
        return "模拟登录：登录成功 " + urlRelateAddRequest.getTitle();
    }

    public String loginErr(UrlRelateAddRequest urlRelateAddRequest) {
        return "小黑子！你没有权限访问该接口！";
    }
}
