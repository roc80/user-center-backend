-- auto-generated definition
create table user
(
    id              bigint auto_increment comment '主键'
        primary key,
    user_name       varchar(256)  not null,
    avatar_url      varchar(1024) null comment '用户头像URL',
    user_password   varchar(2048) not null,
    gender          tinyint       null comment '性别，0是男性，1是女性',
    phone           varchar(128)  null comment '手机号',
    email           varchar(256)  null comment '邮箱',
    create_datetime timestamp     null comment '记录创建时间',
    update_datetime timestamp     null comment '记录更新时间',
    is_valid        tinyint       not null comment '数据是否有效，0有效，1失效',
    is_delete       tinyint       not null comment '数据是否逻辑删除，0未删除，1已删除',
    user_role       int           not null comment '用户角色 0-普通用户 1-管理员'
);
alter table user add column tag_json_list varchar(2048) null comment '标签-JSON列表';

-- 标签表
-- auto-generated definition
create table tag
(
    id              bigint auto_increment comment '主键'
        primary key,
    tag_name        varchar(256)                       not null comment '标签名',
    user_id         bigint                             not null comment '上传者id',
    parent_id       bigint                             not null comment '父标签id',
    is_parent       tinyint                            not null comment '是否是父标签 0 不是父标签 1 是',
    create_datetime datetime default CURRENT_TIMESTAMP not null comment '记录创建时间',
    update_datetime datetime default CURRENT_TIMESTAMP not null comment '记录更新时间',
    is_delete       tinyint  default 0                 not null comment '数据是否逻辑删除，0未删除 1已删除',
    constraint unqidx_tag_name
        unique (tag_name)
)
    comment '标签表';

create index idx_parent_id
    on tag (parent_id);