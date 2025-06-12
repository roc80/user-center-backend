package com.yupi.usercenter.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.lang.Nullable
import java.io.Serializable
import java.time.LocalDateTime

/**
 * @author lipeng
 * @see com.yupi.usercenter.model.User
 * @since 2025/5/11 7:54
 */

data class UserDTO(
        @Nullable
        var userId: Long? = null,

        @Nullable
        var userName: String? = null,

        /**
         * 用户头像URL
         */
        @Nullable
        var avatarUrl: String? = null,

        @Nullable
        var gender: String? = null,

        /**
         * 手机号
         */
        @Nullable
        var phone: String? = null,

        /**
         * 邮箱
         */
        @Nullable
        var email: String? = null,

        /**
         * 用户注册时间
         */
        @Nullable
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
        var createDatetime: LocalDateTime? = null,

        /**
         * 用户角色
         */
        @Nullable
        var userRole: String? = null,

        /**
         * 用户状态
         */
        @Nullable
        var state: String? = null,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}