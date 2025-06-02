package com.yupi.usercenter.model.request

data class TeamUpdateRequest(
        val id: Long,
        val name: String?,
        val description: String?,
        val maxNum: Int?,
        val ownerUserId: Long?,
        val status: Int?,
        val joinType: Int?,
        val joinKey: String?,
        val memberIdList: List<Long>? = null
)