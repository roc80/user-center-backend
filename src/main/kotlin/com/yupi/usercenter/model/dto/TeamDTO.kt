package com.yupi.usercenter.model.dto

import com.yupi.usercenter.model.Team
import java.io.Serializable
import java.util.*

/**
 * @see com.yupi.usercenter.model.Team
 * @author lipeng
 * @since 2025/5/24 13:18
 */

data class TeamDTO(
        val id: Long? = null,
        val name: String? = null,
        val description: String? = null,
        val maxNum: Int? = null,
        val ownerUserId: Long? = null,
        val joinType: Int? = null,
        val createDatetime: Date? = null,
        val members: List<UserDTO>? = null
) : Serializable {

    constructor(team: Team) : this(
            team.id,
            team.name,
            team.description,
            team.maxNum,
            team.ownerUserId,
            team.joinType,
            team.createDatetime
    )

    constructor(team: Team, userDtoList: List<UserDTO>) : this(
            team.id,
            team.name,
            team.description,
            team.maxNum,
            team.ownerUserId,
            team.joinType,
            team.createDatetime,
            userDtoList
    )

    companion object {
        private const val serialVersionUID = -29L
    }
}
