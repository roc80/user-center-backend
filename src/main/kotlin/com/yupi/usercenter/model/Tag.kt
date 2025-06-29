package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.*
import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDateTime

/**
 * 用户中心 标签表
 * @author lipeng
 * @since 2025/5/15 13:32
 */
@TableName(value = "tag")
data class Tag(
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    var id: Long?,

    /**
     * 标签名
     */
    @TableField(value = "tag_name")
    var tagName: String,

    /**
     * 上传者id
     */
    @TableField(value = "user_id")
    var userId: Long,

    /**
     * 父标签id
     */
    @TableField(value = "parent_id")
    var parentId: Long,

    /**
     * 是否是父标签 0 不是父标签 1 是
     */
    @TableField(value = "is_parent")
    var isParent: Int,

    /**
     * 记录创建时间
     */
    @TableField(value = "create_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    var createDatetime: LocalDateTime?,

    /**
     * 记录更新时间
     */
    @TableField(value = "update_datetime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    var updateDatetime: LocalDateTime?,

    /**
     * 数据是否逻辑删除，0未删除 1已删除
     */
    @TableField(value = "is_delete")
    @TableLogic
    var isDelete: Int?,
) : Serializable {

    companion object {
        @TableField(exist = false)
        private val serialVersionUID = 1L
    }

}