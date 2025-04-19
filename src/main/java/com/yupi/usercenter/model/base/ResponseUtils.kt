package com.yupi.usercenter.model.base

/**
 * 避免多次new BaseResponse
 * @author lipeng
 * @since 2025/4/19 16:32
 */
object ResponseUtils {

    @JvmStatic
    fun <T> success(data: T): BaseResponse<T> {
        return BaseResponse(Error.OK, data)
    }

    @JvmStatic
    fun <T> error(error: Error): BaseResponse<T> {
        return BaseResponse(error)
    }

    @JvmStatic
    fun <T> error(error: Error, description: String): BaseResponse<T> {
        return BaseResponse(error, description)
    }

    @JvmStatic
    fun <T> error(error: Error, data: T?, description: String): BaseResponse<T> {
        return BaseResponse(error, data, description)
    }

    @JvmStatic
    fun <T> error(error: Error, data: T?): BaseResponse<T> {
        return BaseResponse(error, data, "")
    }
}