package com.yupi.usercenter.model.base

data class BaseResponse<T>(
        /**
         * 响应状态码
         */
        val code: Int,
        /**
         * 响应payload
         */
        val data: T?,
        /**
         * 简要信息
         */
        val message: String,
        /**
         * 详细描述
         */
        val description: String,
) {
    constructor(code: Int, data: T?) : this(code, data, "", "")
    constructor(code: Int, message: String) : this(code, null, message, "")
    constructor(code: Int, message: String, description: String) : this(code, null, message, description)

    constructor(error: Error) : this(error.code, null, error.message, "")
    constructor(error: Error, data: T?) : this(error.code, data, error.message, "")
    constructor(error: Error, data: T?, description: String) : this(error.code, data, error.message, description)
    constructor(error: Error, description: String) : this(error.code, null, error.message, description)

}