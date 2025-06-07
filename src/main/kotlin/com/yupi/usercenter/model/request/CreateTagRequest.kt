package com.yupi.usercenter.model.request

import org.hibernate.validator.constraints.Length
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class CreateTagRequest(
    @field:NotBlank(message = "标签名不能为空")
    @field:Length(max = 256, message = "标签名长度不能超过256个字符")
    val tagName: String? = null,

    @field:NotNull(message = "父标签ID不能为空")
    @field:Min(value = 0, message = "父标签ID不能为负数")
    val parentId: Long? = null, // 0表示根标签或独立标签

    @field:NotNull(message = "是否父标签标识不能为空")
    @field:Min(value = 0, message = "是否父标签标识必须为0或1")
    @field:Max(value = 1, message = "是否父标签标识必须为0或1")
    val isParent: Int? = null // 0: 不是父标签, 1: 是父标签
)