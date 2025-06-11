package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * 用户中心 User表
 * @author lipeng
 * @since 2025/5/15 13:06
 */
@TableName(value = "user")
data class User(
        /**
         * 主键
         */
        @TableId(type = IdType.AUTO)
        var id: Long? = null,

        var userName: String? = null,

        /**
         * 用户头像URL
         */
        var avatarUrl: String? = null,

        var userPassword: String? = null,

        /**
         * 性别，0是男性，1是女性
         */
        var gender: Int? = null,

        /**
         * 手机号
         */
        var phone: String? = null,

        /**
         * 邮箱
         */
        var email: String? = null,

        /**
         * 记录创建时间
         */
        var createDatetime: LocalDateTime? = null,

        /**
         * 记录更新时间
         */
        var updateDatetime: LocalDateTime? = null,

        /**
         * 数据是否有效，0有效，1失效
         */
        var isValid: Int? = null,

        /**
         * 数据是否逻辑删除，0未删除，1已删除
         */
        @TableLogic
        var isDelete: Int? = null,

        /**
         * 用户角色 0-普通用户 1-管理员
         */
        var userRole: Int? = null,

        /**
         * 标签-JSON列表
         */
        var tagJsonList: String? = null,
) : Serializable {

    constructor(userName: String, userPassword: String) : this() {
        this.userName = userName
        this.userPassword = userPassword
        createDatetime = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        isValid = 0
        isDelete = 0
        userRole = 0
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}