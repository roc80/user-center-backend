DROP TABLE IF EXISTS user;
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                        `user_name` varchar(256) NOT NULL,
                        `avatar_url` varchar(1024) DEFAULT NULL COMMENT '用户头像URL',
                        `user_password` varchar(2048) NOT NULL,
                        `gender` tinyint DEFAULT NULL COMMENT '性别，0是男性，1是女性',
                        `phone` varchar(128) DEFAULT NULL COMMENT '手机号',
                        `email` varchar(256) DEFAULT NULL COMMENT '邮箱',
                        `create_datetime` timestamp NULL COMMENT '记录创建时间',
                        `update_datetime` timestamp NULL COMMENT '记录更新时间',
                        `is_valid` tinyint NOT NULL COMMENT '数据是否有效，0有效，1失效',
                        `is_delete` tinyint NOT NULL COMMENT '数据是否逻辑删除，0未删除，1已删除',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

