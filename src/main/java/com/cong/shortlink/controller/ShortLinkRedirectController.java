package com.cong.shortlink.controller;

import com.cong.shortlink.service.UrlRelateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import javax.annotation.Resource;

/**
 * 短链路重定向控制器
 *
 * @author cong
 * @date 2024/01/17
 */
@Controller
@RequestMapping("/link_dog")
@Api(tags = "短链重定向")
public class ShortLinkRedirectController {
    @Resource
    private UrlRelateService urlRelateService;

    @GetMapping("/{shortLink}")
    @ApiOperation(value = "重定向到长链接")
    public ModelAndView redirectToLongLink(@PathVariable String shortLink) {
        // 此处需要实现逻辑将长链接映射到短链接
        // 可以根据你的需求将长链接与短链接关联起来
        String longLink = urlRelateService.getLongLink(shortLink);

        return new ModelAndView(new RedirectView(longLink, true));

    }

}
