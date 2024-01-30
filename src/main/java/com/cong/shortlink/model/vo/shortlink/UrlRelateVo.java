package com.cong.shortlink.model.vo.shortlink;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cong.shortlink.model.vo.urltag.UrlTagVo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 链接关系表
 *
 * @author cong
 * @date 2024/01/29
 */
@TableName(value ="url_relate")
@Data
public class UrlRelateVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 链接标题
     */
    private String title;

    /**
     * 链接图标
     */
    private String urlImg;

    /**
     * 标签列表
     */
    private List<UrlTagVo> tags;

    /**
     * 长链
     */
    private String longUrl;

    /**
     * 短链
     */
    private String sortUrl;

    /**
     * 访问次数
     */
    private Integer visits;

    /**
     * ip数
     */
    private Integer ipNums;

    /**
     * 访问数
     */
    private Integer userNums;

    /**
     * 允许访问次数
     */
    private Integer allowNum;

    /**
     * 是否私密
     */
    private Integer privateTarget;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 状态：0 草稿 1 发布 2 禁用
     */
    private Integer status;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}