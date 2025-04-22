package com.yupi.usercenter.model.request

import com.fasterxml.jackson.annotation.JsonProperty
import com.yupi.usercenter.model.annotation.Entity
import com.yupi.usercenter.model.annotation.NoArg

@Entity
@NoArg
data class UserLoginRequest(
        @JsonProperty("username") var userName: String,
        @JsonProperty("password") var userPassword: String,
)
