package com.yupi.usercenter.model

import com.baomidou.mybatisplus.annotation.*
import java.io.Serializable
import java.util.*

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
        var id: Long? = null,

        /**
         * 标签名
         */
        var tagName: String? = null,

        /**
         * 上传者id
         */
        var userId: Long? = null,

        /**
         * 父标签id
         */
        var parentId: Long? = null,

        /**
         * 是否是父标签 0 不是父标签 1 是
         */
        var isParent: Int? = null,

        /**
         * 记录创建时间
         */
        var createDatetime: Date? = null,

        /**
         * 记录更新时间
         */
        var updateDatetime: Date? = null,

        /**
         * 数据是否逻辑删除，0未删除 1已删除
         */
        @TableLogic
        var isDelete: Int? = null,
) : Serializable {

    companion object {
        @TableField(exist = false)
        private val serialVersionUID = 1L
    }
}