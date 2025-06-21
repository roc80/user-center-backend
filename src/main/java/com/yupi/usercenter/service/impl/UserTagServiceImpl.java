package com.yupi.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.mapper.TagMapper;
import com.yupi.usercenter.mapper.UserMapper;
import com.yupi.usercenter.mapper.UserTagMapper;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.User;
import com.yupi.usercenter.model.UserTag;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.service.UserTagService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author lipeng
* @description 维护User-Tag关系，如果User或Tag逻辑删除，User-Tag表中的记录仍保留，提供对外服务时过滤掉逻辑删除的部分。
* @since  2025-06-12 09:03:55
*/
@Service
public class UserTagServiceImpl extends ServiceImpl<UserTagMapper, UserTag>
    implements UserTagService{

    private final UserTagMapper userTagMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;

    public UserTagServiceImpl(UserTagMapper userTagMapper, UserMapper userMapper, TagMapper tagMapper) {
        this.userTagMapper = userTagMapper;
        this.userMapper = userMapper;
        this.tagMapper = tagMapper;
    }

    @Override
    public @NonNull List<User> getUserList(List<Long> tagIdList) {
        ArrayList<User> matchedUserList = new ArrayList<>();
        if (tagIdList == null || tagIdList.isEmpty()) {
            return matchedUserList;
        }
        List<Long> matchedUserIdList = userTagMapper.selectUserIdByTagIdList(tagIdList, tagIdList.size());
        if (matchedUserIdList != null) {
            matchedUserIdList.forEach(userId -> matchedUserList.add(userMapper.selectById(userId)));
        }
        return matchedUserList;
    }

    @Override
    public @NonNull List<Tag> getTagList(Long userId) {
        ArrayList<Tag> userTagList = new ArrayList<>();
        if (userId == null || userId <= 0) {
            return userTagList;
        }
        List<Long> tagIdList = userTagMapper.selectTagIdByUserId(userId);
        if (tagIdList != null && !tagIdList.isEmpty()) {
            tagIdList.forEach(tagId -> userTagList.add(tagMapper.selectById(tagId)));
        }
        return userTagList;
    }

    @Override
    public @NonNull List<String> getTagNameList(Long userId) {
        return getTagList(userId).stream().map(Tag::getTagName).collect(Collectors.toList());
    }

    @Override
    @Transactional(timeout = 3, rollbackFor = Exception.class)
    public Integer updateTagsOnUser(Long userId, List<Long> newTagIdList) {
        // TODO@lp 多次更新，状态可能异常，加锁
        int result = 0;
        if (userId == null || newTagIdList == null) {
            return result;
        }
        List<Long> oldTagIdList = getTagList(userId).stream().map(Tag::getId).collect(Collectors.toList());
        for (Long tagId : oldTagIdList) {
            if (!newTagIdList.contains(tagId)) {
                userTagMapper.logicDeleteByUniqueId(userId, tagId);
            } else {
                result++;
            }
        }
        for (Long tagId : newTagIdList) {
            if (!oldTagIdList.contains(tagId)) {
                if (addUserTag(userId, tagId) == 1) {
                    result++;
                }
            }
        }
        return result;
    }

    @Override
    @NonNull
    public List<Long> getAllUserWithTag() {
        List<Long> hasTagsUserIdList = userTagMapper.selectUserIdWithTags();
        if (hasTagsUserIdList == null) {
            return new ArrayList<>();
        } else {
            return hasTagsUserIdList;
        }
    }

    private Integer addUserTag(Long userId, Long tagId) {
        Integer rows = userTagMapper.restoreDeletedOneByUniqueId(userId, tagId);
        if (rows == 0) {
            boolean saved = this.save(new UserTag(userId, tagId));
            if (saved) {
                return 1;
            } else {
                return 0;
            }
        } else if (rows == 1) {
            return 1;
        } else {
            throw new BusinessException(Error.SERVER_ERROR, "添加标签异常");
        }
    }

}




