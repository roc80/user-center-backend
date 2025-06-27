package com.yupi.usercenter.model.response

import com.yupi.usercenter.model.dto.UserDTO

/**
 * @description
 * @author lipeng
 * @since 2025/6/25 18:10
 */
data class LoginResponse(
    val user: UserDTO,
    val redirectUrl: String?,
)
