### 数据表设计

#### 用户表

| 字段名             | 字段类型          | 备注                     | 
|-----------------|---------------|------------------------|
| id              | bigint        |                        |
| user_name       | varchar(256)  |                        |
| avatar_url      | varchar(1024) |                        |
| user_password   | varchar(2048) |                        |
| gender          | tinyint       | 0 - 男； 1 - 女           |
| phone           | varchar(128)  |                        |
| email           | varchar(256)  |                        |
| is_valid        | tinyint       | 用户状态异常 0 - 正常， 1 - 异常  |
| create_datetime | timestamp     |                        |
| update_datetime | timestamp     |                        |
| is_delete       | tinyint       | 逻辑上删除 0 - 未删除， 1 - 已删除 |
