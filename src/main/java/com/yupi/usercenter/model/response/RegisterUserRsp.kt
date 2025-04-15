package com.yupi.usercenter.model.response

data class RegisterUserRsp(
        var id: Long = -1,
        var msg: String = "注册失败",
) {
    constructor(msg: String) : this() {
        this.msg = msg;
    }
}