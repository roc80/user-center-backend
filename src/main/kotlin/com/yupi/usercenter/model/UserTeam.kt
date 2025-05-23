package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.*
import java.io.Serializable
import java.util.*

/**
 *
 * @author lipeng
 * @since 2025/5/23 10:32
 */
@TableName(value = "user_team")
data class UserTeam(
        /**
         * id
         */
        @TableId(value = "id", type = IdType.AUTO)
        private var id: Long? = null,

        /**
         * userId
         */
        @TableField(value = "user_id")
        private val userId: Long,

        /**
         * 队伍Id
         */
        @TableField(value = "team_id")
        private val teamId: Long,

        /**
         * 用户加入队伍时间
         */
        @TableField(value = "join_datetime")
        private val joinDatetime: Date? = null,

        /**
         * 创建时间
         */
        @TableField(value = "create_datetime")
        private val createDatetime: Date? = null,

        /**
         * 更新时间
         */
        @TableField(value = "update_datetime")
        private val updateDatetime: Date? = null,

        /**
         * 逻辑删除，0未删除，1已删除
         */
        @TableLogic
        @TableField(value = "is_delete")
        private val isDelete: Int? = null,
) : Serializable