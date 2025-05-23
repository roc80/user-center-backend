package com.yupi.usercenter.model.request

data class UserRegisterRequest(
        var username: String,
        var password: String,
        var repeatPassword: String,
)