package com.yupi.usercenter.model.enums

/**
 *
 * @author lipeng
 * @since 2025/5/23 17:34
 */
enum class TeamTypeEnum(
        val value: Int,
        val des: String,
) {
    PUBLIC(0, "任何人都可加入"),
    SECRET(1, "输入密码加入"),
    PRIVATE(2, "不可加入");

    companion object {
        fun fromValue(value: Int): TeamTypeEnum? {
            return values().find { it.value == value }
        }
    }
}