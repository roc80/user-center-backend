package com.yupi.usercenter.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {

    public User(@NotNull String userName, @NotNull String userPassword) {
        this.userName = userName;
        this.userPassword = userPassword;
    }

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    @NonNull
    private String userName;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 
     */
    @NonNull
    private String userPassword;

    /**
     * 性别，0是男性，1是女性
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 记录创建时间
     */
    private Date createDatetime;

    /**
     * 记录更新时间
     */
    private Date updateDatetime;

    /**
     * 数据是否有效，0有效，1失效
     */
    @NonNull
    private Integer isValid;

    /**
     * 数据是否逻辑删除，0未删除，1已删除
     */
    @NonNull
    @TableLogic
    private Integer isDelete;

    /**
     * 用户角色 0-普通用户 1-管理员
     */
    private Integer userRole;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}