package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.*
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
    @TableId(type = IdType.AUTO) var id: Long?,

    /**
     * 标签名
     */
    var tagName: String,

    /**
     * 上传者id
     */
    var userId: Long,

    /**
     * 父标签id
     */
    var parentId: Long,

    /**
     * 是否是父标签 0 不是父标签 1 是
     */
    var isParent: Int,

    /**
     * 记录创建时间
     */
    var createDatetime: LocalDateTime?,

    /**
     * 记录更新时间
     */
    var updateDatetime: LocalDateTime?,

    /**
     * 数据是否逻辑删除，0未删除 1已删除
     */
    @TableLogic var isDelete: Int?,
) : Serializable {

    companion object {
        @TableField(exist = false)
        private val serialVersionUID = 1L
    }

}