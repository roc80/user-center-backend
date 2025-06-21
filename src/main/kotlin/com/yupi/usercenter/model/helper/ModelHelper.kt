package com.yupi.usercenter.model.helper

import com.yupi.usercenter.constant.UserConstant
import com.yupi.usercenter.model.User
import com.yupi.usercenter.model.dto.UserDTO

object ModelHelper {
    fun convertUserDtoToUser(userDTO: UserDTO): User {
        return User(
            userDTO.userId,
            userDTO.userName,
            userDTO.avatarUrl,
            null,
            when (userDTO.gender) {
                UserConstant.USER_GENDER_MAN -> UserConstant.USER_PO_GENDER_MAN
                UserConstant.USER_GENDER_WOMAN -> UserConstant.USER_PO_GENDER_WOMAN
                else -> null
            },
            userDTO.phone,
            userDTO.email,
            null,
            null,
            when (userDTO.state) {
                UserConstant.USER_STATE_NORMAL -> UserConstant.USER_PO_STATE_NORMAL
                UserConstant.USER_STATE_INVALID -> UserConstant.USER_PO_STATE_INVALID
                else -> null
            },
            null,
            when (userDTO.userRole) {
                UserConstant.USER_ROLE_DEFAULT -> UserConstant.USER_PO_ROLE_DEFAULT
                UserConstant.USER_ROLE_ADMIN -> UserConstant.USER_PO_ROLE_ADMIN
                else -> null
            },
        )
    }

    fun convertUserToUserDto(userPO: User): UserDTO {
        return UserDTO(
            userPO.id,
            userPO.userName,
            userPO.avatarUrl,
            when (userPO.gender) {
                UserConstant.USER_PO_GENDER_MAN -> UserConstant.USER_GENDER_MAN
                UserConstant.USER_PO_GENDER_WOMAN -> UserConstant.USER_GENDER_WOMAN
                else -> UserConstant.EMPTY_STRING
            },
            userPO.phone,
            userPO.email,
            userPO.createDatetime,
            when (userPO.userRole) {
                UserConstant.USER_PO_ROLE_DEFAULT -> UserConstant.USER_ROLE_DEFAULT
                UserConstant.USER_PO_ROLE_ADMIN -> UserConstant.USER_ROLE_ADMIN
                else -> UserConstant.EMPTY_STRING
            },
            when (userPO.isValid) {
                UserConstant.USER_PO_STATE_NORMAL -> UserConstant.USER_STATE_NORMAL
                UserConstant.USER_PO_STATE_INVALID -> UserConstant.USER_STATE_INVALID
                else -> UserConstant.EMPTY_STRING
            },

        )
    }
}