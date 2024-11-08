package com.yupi.usercenter.model.response

import com.yupi.usercenter.model.User

data class LoginUserRsp(
        var user: User?,
        var msg: String,
)