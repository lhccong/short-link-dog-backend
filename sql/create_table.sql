# 数据库初始化
# @author <a href="https://github.com/lhccong">程序员聪</a>
#

-- 创建库
create database if not exists short_link_dog;

-- 切换库
use short_link_dog;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 短链表
drop table url_relate;
create table if not exists url_relate
(
   `id` bigint unsigned NOT NULL AUTO_INCREMENT primary key,
   title     varchar(256)                           null comment '链接标题',
   `longUrl` varchar(160) DEFAULT NULL COMMENT '长链',
   `sortUrl` varchar(10) DEFAULT NULL COMMENT '短链',
   userId     bigint                             not null comment '创建用户 id',
   visits      int                            not null default 0 comment '访问次数',
   ipNums      int                            not null default 0 comment 'ip数',
   userNums      int                            not null default 0 comment '访问数',
   createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
   updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
   isDelete   tinyint  default 0                 not null comment '是否删除',
   index idx_sort_url (sortUrl)
) comment '链接关系表' collate = utf8mb4_unicode_ci;
