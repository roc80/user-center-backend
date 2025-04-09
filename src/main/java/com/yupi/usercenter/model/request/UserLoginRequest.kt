package com.yupi.usercenter.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UserLoginRequest(
        @JsonProperty("userName") var userName: String,
        @JsonProperty("userPassword") var userPassword: String,
)
