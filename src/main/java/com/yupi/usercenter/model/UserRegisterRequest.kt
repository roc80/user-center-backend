package com.yupi.usercenter.model

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRegisterRequest(
        @JsonProperty("userName") var userName: String,
        @JsonProperty("userPassword") var userPassword: String,
        @JsonProperty("repeatPassword") var repeatPassword: String,
)