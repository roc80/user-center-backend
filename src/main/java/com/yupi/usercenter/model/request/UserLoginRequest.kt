package com.yupi.usercenter.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UserLoginRequest(
        @JsonProperty("username") var userName: String,
        @JsonProperty("password") var userPassword: String,
)
