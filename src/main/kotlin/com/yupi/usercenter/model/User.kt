package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.*
import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDateTime

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

    @TableField(value = "user_name")
    var userName: String? = null,

    /**
     * 用户头像URL
     */
    @TableField(value = "avatar_url")
    var avatarUrl: String? = null,

    @TableField(value = "user_password")
    var userPassword: String? = null,

    /**
     * 性别，0是男性，1是女性
     */
    @TableField(value = "gender")
    var gender: Int? = null,

    /**
     * 手机号
     */
    @TableField(value = "phone")
    var phone: String? = null,

    /**
     * 邮箱
     */
    @TableField(value = "email")
    var email: String? = null,

    /**
     * 记录创建时间
     */
    @TableField(value = "create_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    var createDatetime: LocalDateTime? = null,

    /**
     * 记录更新时间
     */
    @TableField(value = "update_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    var updateDatetime: LocalDateTime? = null,

    /**
     * 数据是否有效，0有效，1失效
     */
    @TableField(value = "is_valid")
    var isValid: Int? = null,

    /**
     * 数据是否逻辑删除，0未删除，1已删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    var isDelete: Int? = null,

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    @TableField(value = "user_role")
    var userRole: Int? = null,
) : Serializable {

    constructor(userName: String, userPassword: String) : this() {
        this.userName = userName
        this.userPassword = userPassword
        isValid = 0
        isDelete = 0
        userRole = 0
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}