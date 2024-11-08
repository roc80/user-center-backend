package com.yupi.usercenter.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
        this.createDatetime = new Date();
        this.updateDatetime = this.createDatetime;
        this.isValid = 0;
        this.isDelete = 0;
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
    @NonNull
    private Date createDatetime;

    /**
     * 记录更新时间
     */
    @NonNull
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
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}