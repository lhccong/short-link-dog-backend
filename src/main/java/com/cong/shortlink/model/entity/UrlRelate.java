package com.cong.shortlink.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 链接关系表
 * @author liuhuaicong
 * @TableName url_relate
 */
@TableName(value ="url_relate")
@Data
public class UrlRelate implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
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
     * 长链
     */
    private String longUrl;

    /**
     * 短链
     */
    private String sortUrl;

    /**
     * 创建用户 id
     */
    private Long userId;

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
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}