package com.cong.shortlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cong.shortlink.common.ErrorCode;
import com.cong.shortlink.exception.BusinessException;
import com.cong.shortlink.model.dto.urltag.UrlTagAddRequest;
import com.cong.shortlink.model.entity.UrlTag;
import com.cong.shortlink.model.vo.urltag.UrlTagCategoryVo;
import com.cong.shortlink.model.vo.urltag.UrlTagVo;
import com.cong.shortlink.service.UrlTagService;
import com.cong.shortlink.mapper.UrlTagMapper;
import com.cong.shortlink.utils.BeanCopyUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.cong.shortlink.constant.CommonConstant.TAG_COLORS;

/**
 * @author liuhuaicong
 * @description 针对表【url_tag(短链标签表)】的数据库操作Service实现
 * @createDate 2024-01-29 10:24:43
 */
@Service
public class UrlTagServiceImpl extends ServiceImpl<UrlTagMapper, UrlTag>
        implements UrlTagService {

    Random random = new Random();

    @Override
    public List<UrlTagCategoryVo> getAllTags() {
        //获取大类 后面改成Redis获取
        List<UrlTag> parentTagList = this.list(new LambdaQueryWrapper<UrlTag>().eq(UrlTag::getParentId, 0));
        //获取标签
        return parentTagList.stream().map(item -> {
            UrlTagCategoryVo userTagCategoryVo = BeanCopyUtils.copyBean(item, UrlTagCategoryVo.class);
            List<UrlTag> tags = this.list(new LambdaQueryWrapper<UrlTag>().eq(UrlTag::getParentId, item.getId()));
            List<UrlTagVo> userTagVoList = tags.stream().map(item1 -> BeanCopyUtils.copyBean(item1, UrlTagVo.class)).collect(Collectors.toList());
            userTagCategoryVo.setTags(userTagVoList);
            return userTagCategoryVo;
        }).collect(Collectors.toList());
    }

    @Override
    public UrlTagVo addTag(UrlTagAddRequest userTagAddRequest) {
        //获取这个分类下的所有标签
        List<UrlTag> tags = this.list(new LambdaQueryWrapper<UrlTag>().eq(UrlTag::getParentId, userTagAddRequest.getParentId()));

        //判断这个标签是否出现过
        for (UrlTag tag : tags) {
            if (tag.getName().equals(userTagAddRequest.getName().toLowerCase())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签已存在");
            }
        }

        //标签不存在则将其插入到mysql和redis中（随机生成一个颜色）
        UrlTag userTag = BeanCopyUtils.copyBean(userTagAddRequest, UrlTag.class);
        userTag.setCreatedBy(Long.valueOf((String) StpUtil.getLoginId()));
        userTag.setColor(TAG_COLORS[random.nextInt(TAG_COLORS.length)]);
        save(userTag);

        return BeanCopyUtils.copyBean(userTag, UrlTagVo.class);
    }
}




