package com.yupi.usercenter.model.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UserRegisterRequest(
        @JsonProperty("username") var userName: String,
        @JsonProperty("password") var userPassword: String,
        @JsonProperty("repeatPassword") var repeatPassword: String,
)