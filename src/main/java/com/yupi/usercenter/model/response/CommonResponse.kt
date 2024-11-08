package com.yupi.usercenter.model.response


data class CommonResponse(
        val code: Int,
        val msg: String,
        val data: Any?,
) {
    constructor() : this(0, "", null)
}
