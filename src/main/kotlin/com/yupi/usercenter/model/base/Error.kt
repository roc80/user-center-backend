package com.yupi.usercenter.model.base

import java.time.LocalDateTime

/**
 * 将同类的Error归为一个枚举值，并附上简要信息
 * @author lipeng
 * @since 2025/4/19 16:35
 */
enum class Error(
        /**
         * 对HTTP状态码的业务扩展
        */
        val code: Int,
        val message: String,
        val dateTime: LocalDateTime = LocalDateTime.now(),
) {
    OK(20000, "成功"),

    CLIENT_PARAMS_ERROR(40000, "请求参数错误"),
    CLIENT_NO_AUTH(40001, "未登录"),
    CLIENT_FORBIDDEN(40003, "无权限"),
    CLIENT_PATH_ERROR(40004, "请求路径错误"),
    CLIENT_PARAMS_NULL(40005, "请求参数为空"),
    CLIENT_OPERATION_DENIED(40006, "客户端操作被拒绝"),

    SERVER_ERROR(50000, "服务端错误"),
    SERVER_DIRTY_DATA(50001, "数据库脏数据")
}