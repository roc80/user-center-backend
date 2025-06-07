package com.yupi.usercenter.model.response

import java.util.*

data class TagResponse(
    val id: Long? = null,
    val tagName: String? = null,
    val userId: Long? = null,
    val parentId: Long? = null,
    val isParent: Int? = null,
    val createDatetime: Date? = null,
    val parentTagName: String? = null // 父标签名称（如果有）
)