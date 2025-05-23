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
                    UserConstant.USER_GENDER_MAN -> 0
                    UserConstant.USER_GENDER_WOMAN -> 1
                    else -> null
                },
                userDTO.phone,
                userDTO.email,
                null,
                null,
                when (userDTO.state) {
                    UserConstant.USER_STATE_NORMAL -> 0
                    UserConstant.USER_STATE_INVALID -> 1
                    else -> null
                },
                null,
                when (userDTO.userRole) {
                    UserConstant.USER_ROLE_DEFAULT -> 0
                    UserConstant.USER_ROLE_ADMIN -> 1
                    else -> null
                },
                userDTO.tags,
        )
    }

    fun convertUserToUserDto(userPO: User): UserDTO {
        return UserDTO(
                userPO.id,
                userPO.userName,
                userPO.avatarUrl,
                when (userPO.gender) {
                    0 -> UserConstant.USER_GENDER_MAN
                    1 -> UserConstant.USER_GENDER_WOMAN
                    else -> UserConstant.EMPTY_STRING
                },
                userPO.phone,
                userPO.email,
                userPO.createDatetime,
                when (userPO.userRole) {
                    0 -> UserConstant.USER_ROLE_DEFAULT
                    1 -> UserConstant.USER_ROLE_ADMIN
                    else -> UserConstant.EMPTY_STRING
                },
                when (userPO.isValid) {
                    0 -> UserConstant.USER_STATE_NORMAL
                    1 -> UserConstant.USER_STATE_INVALID
                    else -> UserConstant.EMPTY_STRING
                },
                userPO.tagJsonList,
        )
    }
}