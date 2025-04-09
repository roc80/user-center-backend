package com.yupi.usercenter.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UserDeleteRequest(
        @JsonProperty("userId")
        var userId: Long = -1,
)