package com.yupi.usercenter.model.request

data class UserRegisterRequest(
        var userName: String,
        var userPassword: String,
        var repeatPassword: String,
)