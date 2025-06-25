package com.yupi.usercenter.model.response

/**
 * @description
 * @author lipeng
 * @since 2025/6/25 12:04
 */
data class PageResponse<T> (
    val records: Collection<T>,
    val pageNum: Int,
    val pageSize: Int,
    val hasMore: Boolean,
)