package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.*
import com.yupi.usercenter.model.request.TeamCreateRequest
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
        var id: Long?,

        /**
         * 队伍名称
         */
        @TableField(value = "name")
        var name: String,

        /**
         * 队伍描述
         */
        @TableField(value = "description")
        var description: String?,

        /**
         * 加入队伍所需的密钥
         */
        @TableField(value = "join_key")
        var joinKey: String?,

        /**
         * 允许加入的最大人数
         */
        @TableField(value = "max_num")
        var maxNum: Int,

        /**
         * 创建者userId
         */
        @TableField(value = "creator_user_id")
        var creatorUserId: Long,

        /**
         * 队长userId
         */
        @TableField(value = "owner_user_id")
        var ownerUserId: Long,

        /**
         * 状态 0-正常
         */
        @TableField(value = "status")
        var status: Int,

        /**
         * 加入方式 0-任何人都可加入 1-输入密码加入 2-不可加入
         */
        @TableField(value = "join_type")
        var joinType: Int,

        /**
         * 创建时间
         */
        @TableField(value = "create_datetime")
        var createDatetime: Date?,

        /**
         * 更新时间
         */
        @TableField(value = "update_datetime")
        var updateDatetime: Date?,

        /**
         * 逻辑删除，0未删除，1已删除
         */
        @TableLogic
        @TableField(value = "is_delete")
        var isDelete: Int?,

        /**
         * 当前队伍成员的id, 英文逗号作分割符
         */
        @TableField(value = "member_ids")
        var memberIds: String,
) : Serializable {
    constructor(request: TeamCreateRequest, loginUserId: Long) : this(
            null,
            request.name,
            request.description,
            request.joinKey,
            request.maxNum,
            loginUserId,
            loginUserId,
            0,
            request.joinType,
            null,
            null,
            null,
            loginUserId.toString()
    )

    companion object {
        private const val serialVersionUID = -81L
    }
}