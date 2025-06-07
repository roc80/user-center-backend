package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.TagTreeNode;
import com.yupi.usercenter.model.request.CreateTagRequest;
import com.yupi.usercenter.model.response.TagResponse;

import javax.validation.Valid;
import java.util.List;

/**
* @author Claude Sonnet4
* @description 针对表【tag(标签表)】的数据库操作Service
* @since 2025-05-05 15:26:29
*/
public interface TagService extends IService<Tag> {

    TagResponse createTag(@Valid CreateTagRequest request, Long userId);

    List<TagTreeNode> getUserTagTree(Long userId);

    TagResponse getTagById(Long tagId, Long userId);

    void deleteTag(Long tagId, Long userId);
}
