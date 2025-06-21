package com.yupi.usercenter.model.dto

import com.yupi.usercenter.model.Tag
import java.time.LocalDateTime

/**
 * @description
 * @author lipeng
 * @since 2025/6/12 22:42
 */
data class TagDTO(
    val id: Long?,
    val tagName: String,
    val createDatetime: LocalDateTime?,
) {
    constructor(tag: Tag) : this(
        tag.id, tag.tagName, tag.createDatetime
    )
}
