package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
import java.util.*

/**
 *
 * @author lipeng
 * @since 2025/5/23 10:13
 */
@TableName(value = "team")
data class Team(

        /**
         * id
         */
        @TableId(value = "id", type = IdType.AUTO)
        private var id: Long? = null,

        /**
         * 队伍名称
         */
        @TableField(value = "name")
        private val name: String,

        /**
         * 队伍描述
         */
        @TableField(value = "description")
        private val description: String? = null,

        /**
         * 加入队伍所需的密钥
         */
        @TableField(value = "join_key")
        private val joinKey: String? = null,

        /**
         * 允许加入的最大人数
         */
        @TableField(value = "max_num")
        private val maxNum: Int? = null,

        /**
         * 创建者userId
         */
        @TableField(value = "creator_user_id")
        private val creatorUserId: Long,

        /**
         * 队长userId
         */
        @TableField(value = "owner_user_id")
        private val ownerUserId: Long,

        /**
         * 状态 0-正常
         */
        @TableField(value = "status")
        private val status: Int? = null,

        /**
         * 加入方式 0-任何人都可加入 1-输入密码加入 2-不可加入
         */
        @TableField(value = "join_type")
        private val joinType: Int? = null,

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
        @TableField(value = "is_delete")
        private val isDelete: Int? = null,
) : Serializable