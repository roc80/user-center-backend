package com.yupi.usercenter.model

data class TagTreeNode(
    val id: Long? = null,
    val tagName: String? = null,
    val isParent: Int? = null,
    val children: List<TagTreeNode>? = null
)