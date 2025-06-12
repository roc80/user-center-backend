package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableLogic
import com.baomidou.mybatisplus.annotation.TableName
import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDateTime

/**
 *
 * @author lipeng
 * @TableName user_tag
 */
@TableName(value = "user_tag")
data class UserTag(
    /**
     * 用户id
     */
    @TableField("user_id")
    val userId: Long,

    /**
     * 标签id
     */
    @TableField("tag_id")
    val tagId: Long,

    /**
     * 创建时间
     */
    @TableField(value = "create_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val createDatetime: LocalDateTime?,

    /**
     * 更新时间
     */
    @TableField(value = "update_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    val updateDatetime: LocalDateTime?,

    /**
     * 逻辑删除，0未删除，1已删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    val isDelete: Int?,
) : Serializable {
    constructor(userId: Long, tagId: Long) : this(userId, tagId, null, null, 0)
}