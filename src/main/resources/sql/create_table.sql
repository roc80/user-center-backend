-- 用户表
create table user
(
    id              bigint auto_increment comment '主键'
        primary key,
    user_name       varchar(256)                        not null,
    avatar_url      varchar(1024)                       null comment '用户头像URL',
    user_password   varchar(2048)                       not null,
    gender          tinyint                             null comment '性别，0是男性，1是女性',
    phone           varchar(128)                        null comment '手机号',
    email           varchar(256)                        null comment '邮箱',
    create_datetime timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_datetime timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_valid        tinyint                             not null comment '数据是否有效，0有效，1失效',
    is_delete       tinyint                             not null comment '数据是否逻辑删除，0未删除，1已删除',
    user_role       int                                 not null comment '用户角色 0-普通用户 1-管理员'
);

-- 标签表
create table tag
(
    id              bigint auto_increment comment '主键'
        primary key,
    tag_name        varchar(256)                        not null comment '标签名',
    user_id         bigint                              not null comment '上传者id',
    parent_id       bigint                              not null comment '父标签id',
    is_parent       tinyint                             not null comment '是否是父标签 0 不是父标签 1 是',
    create_datetime timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_datetime timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint   default 0                 not null comment '数据是否逻辑删除，0未删除 1已删除',
    constraint unqidx_tag_name
        unique (tag_name)
)
    comment '标签表';

create index idx_parent_id
    on tag (parent_id);

-- 队伍表
create table team
(
    id              bigint auto_increment comment 'id'
        primary key,
    name            varchar(128)                                not null comment '队伍名称',
    description     varchar(1024)                               null comment '队伍描述',
    join_key        varchar(1024) collate utf8mb4_unicode_ci    null,
    max_num         smallint unsigned default '1'               not null comment '允许加入的最大人数',
    creator_user_id bigint                                      not null comment '创建者userId',
    owner_user_id   bigint                                      not null comment '队长userId',
    status          tinyint           default 0                 not null comment '状态 0-正常',
    join_type       tinyint           default 0                 not null comment '加入方式 0-任何人都可加入 1-输入密码加入 2-不可加入',
    create_datetime timestamp         default CURRENT_TIMESTAMP not null comment '创建时间',
    update_datetime timestamp         default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint           default 0                 not null comment '逻辑删除，0未删除，1已删除',
    member_ids      varchar(1024)                               not null comment '当前队伍成员的id'
);

-- 用户 & 标签 关系表
create table user_tag
(
    user_id         bigint                              not null comment '用户id',
    tag_id          bigint                              not null comment '标签id',
    create_datetime timestamp default CURRENT_TIMESTAMP not null comment '创建时间',
    update_datetime timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete       tinyint                             not null comment '逻辑删除，0未删除，1已删除',
    primary key (user_id, tag_id),
    constraint user_tag_ibfk_1
        foreign key (user_id) references user (id),
    constraint user_tag_ibfk_2
        foreign key (tag_id) references tag (id)
);

create index tag_id
    on user_tag (tag_id);

