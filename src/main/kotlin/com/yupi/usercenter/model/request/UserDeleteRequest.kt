package com.yupi.usercenter.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.yupi.usercenter.model.annotation.Entity
import com.yupi.usercenter.model.annotation.NoArg

@Entity
@NoArg
data class UserDeleteRequest(
        @JsonProperty("userId")
        var userId: Long = -1,
)