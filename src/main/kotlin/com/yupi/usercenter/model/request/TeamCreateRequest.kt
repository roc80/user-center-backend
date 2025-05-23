package com.yupi.usercenter.model.request

data class TeamCreateRequest(
        val name: String,
        val description: String?,
        val joinKey: String?,
        val maxNum: Int,
        val joinType: Int,
)
