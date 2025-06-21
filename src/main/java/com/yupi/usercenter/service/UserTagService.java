package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.UserTag;
import org.springframework.lang.NonNull;

import java.util.List;

/**
* @author lipeng
* @description 针对表【user_tag】的数据库操作Service
* @since 2025-06-12 09:03:55
*/
public interface UserTagService extends IService<UserTag> {

    @NonNull
    List<User> getUserList(List<Long> tagIdList);

     /**
      * @param userId 未被删除的userId
      * @return 未被删除的
      */
    @NonNull
    List<Tag> getTagList(Long userId);

     /**
      *
      * @param userId 未被删除的userId
      * @return 未被删除的
      */
    @NonNull
    List<String> getTagNameList(Long userId);

     /**
      *
      * @return newTagIdList中有几个和userId已经绑定
      */
    Integer updateTagsOnUser(Long userId, List<Long> newTagIdList);

    @NonNull
    List<Long> getAllUserWithTag();
}
