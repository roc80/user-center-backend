package com.yupi.usercenter.model.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class TagResponse(
    val id: Long? = null,
    val tagName: String? = null,
    val userId: Long? = null,
    val parentId: Long? = null,
    val isParent: Int? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createDatetime: LocalDateTime? = null,
    val parentTagName: String? = null // 父标签名称（如果有）
)