package com.yupi.usercenter.model;

import com.yupi.usercenter.constant.UserConstant;
import lombok.Data;
import lombok.NonNull;
import org.springframework.lang.Nullable;

import java.util.Date;

/**
 * @see com.yupi.usercenter.model.User
 * @author lipeng
 * @since 2025/5/11 7:54
*/
@Data
public class UserDTO {

    public UserDTO(@NonNull User userPO) {
        this.userId = userPO.getId();
        this.userName = userPO.getUserName();
        this.avatarUrl = userPO.getAvatarUrl();
        this.gender = userPO.getGender() == 0 ? "男" : "女";
        this.email = userPO.getEmail();
        this.phone = userPO.getPhone();
        this.createDatetime = userPO.getCreateDatetime();
        this.userRole = userPO.getUserRole() == 0 ? UserConstant.USER_ROLE_DEFAULT : UserConstant.USER_ROLE_ADMIN;
        this.tags = userPO.getTagJsonList();
        this.state = userPO.getIsValid() == 0 ? "normal" : "invalid";
    }

    @NonNull
    private Long userId;

    @NonNull
    private String userName;

    /**
     * 用户头像URL
     */
    @Nullable
    private String avatarUrl;

    @Nullable
    private String gender;

    /**
     * 手机号
     */
    @Nullable
    private String phone;

    /**
     * 邮箱
     */
    @Nullable
    private String email;

    /**
     * 用户注册时间
     */
    @NonNull
    private Date createDatetime;

    /**
     * 用户角色
     */
    @NonNull
    private String userRole;

    /**
     * 用户状态
    */
    @NonNull
    private String state;

    /**
     * 用户标签-JSON列表
     */
    @Nullable
    private String tags;

    private static final long serialVersionUID = 1L;
}
