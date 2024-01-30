package com.cong.shortlink.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
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
import com.cong.shortlink.model.entity.UrlTag;
import com.cong.shortlink.model.entity.User;
import com.cong.shortlink.model.enums.ShortLinkStatusEnum;
import com.cong.shortlink.model.vo.shortlink.UrlRelateVo;
import com.cong.shortlink.model.vo.urltag.UrlTagVo;
import com.cong.shortlink.service.UrlRelateService;
import com.cong.shortlink.service.UrlTagService;
import com.cong.shortlink.service.UserService;
import com.cong.shortlink.utils.Base62Converter;
import com.cong.shortlink.utils.BeanCopyUtils;
import com.cong.shortlink.utils.NetUtils;
import com.cong.shortlink.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import net.openhft.hashing.LongHashFunction;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.Redisson;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RMap;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static com.cong.shortlink.constant.RedissonConstants.*;

/**
 * @author liuhuaicong
 * @description 针对表【url_relate(链接关系表)】的数据库操作Service实现
 * @createDate 2024-01-17 14:43:37
 */
@Service
@Slf4j
public class UrlRelateServiceImpl extends ServiceImpl<UrlRelateMapper, UrlRelate>
        implements UrlRelateService {
    @Resource
    private UserService userService;

    @Resource
    private Redisson redisson;

    @Resource
    private UrlTagService urlTagService;

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
        //自动解析获取title
        setUrlTitleAndImg(longUrl, urlRelate);
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

        String shortUrlStr64 = getShortUrlBySha256(longUrl);
        //使用布隆过滤器校验
        RBloomFilter<String> bloomFilter = redisson.getBloomFilter(SHORT_LINK_BLOOM_FILTER_KEY);
        //初始化布隆过滤器：预计元素为100000000L,误差率为3%
        bloomFilter.tryInit(100000000L, 0.03);

        if (bloomFilter.contains(shortUrlStr64)) {
            //hash冲突重新生成 在结尾重新添加一个分布式id（暂用62位时间戳）
            shortUrlStr64 = shortUrlStr64 + Base62Converter.toBase62(System.currentTimeMillis());
        }
        bloomFilter.add(shortUrlStr64);
        return shortUrlStr64;
    }

    private static String getShortUrlBySha256(String longUrl) {
        // 获取 MurmurHash3 的实例
        LongHashFunction murmur3 = LongHashFunction.murmur_3();

        // 计算输入字符串的哈希值
        long hash = murmur3.hashChars(longUrl);

        return Base62Converter.toBase62(hash);
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
            ThrowUtils.throwIf(this.getOne(new LambdaQueryWrapper<UrlRelate>().eq(UrlRelate::getLongUrl, longUrl)
                            .eq(UrlRelate::getUserId, StpUtil.getLoginId())) != null
                    , ErrorCode.PARAMS_ERROR, "长链已存在");
            // 校验长链规则 主域名合法性 查询参数域名合法性
            NetUtils.validateLink(longUrl);
        }

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

        //移除缓存
        RMap<String, UrlRelate> shortLinkMap = redisson.getMap(SHORT_LINK_CACHE_MAP_KEY);
        shortLinkMap.remove(oldUrlRelate.getSortUrl());
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

    /**
     * 获取长链接
     *
     * @param shortLink 短链接
     * @return {@link UrlRelate}
     */
    @Override
    public UrlRelate getLongLink(String shortLink) {
        // 获取短链接对应的UrlRelate对象
        RMap<String, UrlRelate> shortLinkMap = redisson.getMap(SHORT_LINK_CACHE_MAP_KEY);
        if (shortLinkMap.containsKey(shortLink)) {
            UrlRelate urlRelate = shortLinkMap.get(shortLink);
            addAndCheck(urlRelate);
            return urlRelate;
        } else {
            // 通过UrlRelate的sortUrl和status查询UrlRelate对象
            UrlRelate urlRelate = this.getOne(new LambdaQueryWrapper<UrlRelate>().eq(UrlRelate::getSortUrl, shortLink)
                    .eq(UrlRelate::getStatus, ShortLinkStatusEnum.PUBLISH.getValue()));
            if (urlRelate == null) {
                throw new BusinessException(ErrorCode.LINK_ERROR, "链接不存在");
            }
            addAndCheck(urlRelate);
            // 将短链接和UrlRelate对象存入shortLinkMap中
            shortLinkMap.put(shortLink, urlRelate);
            return urlRelate;
        }

    }

    private void addAndCheck(UrlRelate urlRelate) {
        //做校验、接口访问次数+1   记录IP
        this.update()
                .eq("id", urlRelate.getId())
                .setSql("ipNums = ipNums + 1")
                .update();
    }

    @Override
    public UrlRelateVo getByShortLink(String shortLink) {

        UrlRelate urlRelate = getLongLink(shortLink);

        return getUrlRelateVo(urlRelate);
    }

    public UrlRelateVo getUrlRelateVo(UrlRelate urlRelate) {
        UrlRelateVo urlRelateVo = BeanCopyUtils.copyBean(urlRelate, UrlRelateVo.class);
        //获取标签列表
        String tagsStr = urlRelate.getTags();
        if (CharSequenceUtil.isNotBlank(tagsStr)) {
            List<String> tagIds = JSONUtil.toBean(tagsStr, new TypeReference<List<String>>() {
            }, true);
            if (CollectionUtil.isNotEmpty(tagIds)){
                List<UrlTag> userTagList = urlTagService.list(new LambdaQueryWrapper<UrlTag>().in(UrlTag::getId, tagIds));
                List<UrlTagVo> tagVos = userTagList.stream().map(item -> BeanCopyUtils.copyBean(item, UrlTagVo.class)).collect(Collectors.toList());
                urlRelateVo.setTags(tagVos);
            }

        }

        return urlRelateVo;
    }

    @Override
    public List<UrlRelateVo> getUrlRelateVo(List<UrlRelate> records) {
        return records.stream().map(this::getUrlRelateVo).collect(Collectors.toList());
    }

    public static void setUrlTitleAndImg(String url, UrlRelate urlRelate) {
        //通过url解析网址title和logo
        String title;
        try {
            //还是一样先从一个URL加载一个Document对象。
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("head");
            Elements titleLinks = links.get(0).select("title");
            title = titleLinks.get(0).text();
            log.info("成功解析网站标题============={}", title);
            if (CharSequenceUtil.isBlank(urlRelate.getTitle())) {
                urlRelate.setTitle(title);
            }
            Element faviconLink = doc.head().select("link[href~=.*\\.(ico|png)]").first();
            String faviconUrl = faviconLink != null ? faviconLink.attr("href") : "";

            // Sometimes the favicon URL is relative, so we might need to make it absolute
            if (!(faviconUrl.toLowerCase().startsWith("https") || faviconUrl.toLowerCase().startsWith("http"))) {
                faviconUrl = url + faviconUrl;
            }
            log.info("网站icon============={}", faviconUrl);
            if (CharSequenceUtil.isBlank(urlRelate.getUrlImg())) {
                urlRelate.setUrlImg(faviconUrl);
            }

        } catch (Exception e) {
            log.error("通过url={} 网址解析title和logo 异常：{}", url, e.getMessage());
            e.printStackTrace();
        }
    }
}




