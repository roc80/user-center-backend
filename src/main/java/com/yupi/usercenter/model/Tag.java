package com.yupi.usercenter.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标签表
 * @TableName tag
 */
@TableName(value ="tag")
@Data
public class Tag implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 上传者id
     */
    private Long userId;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 是否是父标签 0 不是父标签 1 是
     */
    private Integer isParent;

    /**
     * 记录创建时间
     */
    private Date createDatetime;

    /**
     * 记录更新时间
     */
    private Date updateDatetime;

    /**
     * 数据是否逻辑删除，0未删除 1已删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}