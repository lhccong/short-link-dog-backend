package com.cong.shortlink.controller;

import com.cong.shortlink.common.BaseResponse;
import com.cong.shortlink.common.ResultUtils;
import com.cong.shortlink.model.dto.urltag.UrlTagAddRequest;
import com.cong.shortlink.model.vo.urltag.UrlTagCategoryVo;
import com.cong.shortlink.model.vo.urltag.UrlTagVo;
import com.cong.shortlink.service.UrlTagService;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * URL 标记控制器
 *
 * @author cong
 * @date 2024/01/29
 */
@RestController
@RequestMapping("/member/tags")
//@Api(tags = "短链标签")
public class UrlTagController {
    @Resource
    private UrlTagService urlTagService;

    /**
     * 获取所有标签
     *
     * @return {@link BaseResponse}<{@link List}<{@link UrlTagCategoryVo}>>
     */
    @GetMapping
    @ApiOperation(value = "获取所有短链标签")
    public BaseResponse<List<UrlTagCategoryVo>> getAllTags(){
        List<UrlTagCategoryVo> allTags = urlTagService.getAllTags();
        return ResultUtils.success(allTags);
    }

    /**
     * 添加一个标签
     *
     * @param userTagAddRequest 用户标签添加请求
     * @return {@link BaseResponse}<{@link UrlTagVo}>
     */
    @PostMapping
    @ApiOperation(value = "添加短链标签")
    public BaseResponse<UrlTagVo> addTag(@RequestBody UrlTagAddRequest userTagAddRequest){
        UrlTagVo userTagVo = urlTagService.addTag(userTagAddRequest);
        return ResultUtils.success(userTagVo);
    }
}
