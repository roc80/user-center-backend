package com.yupi.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.usercenter.model.Tag;
import com.yupi.usercenter.model.TagTreeNode;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.request.TagCreateRequest;
import com.yupi.usercenter.model.response.TagResponse;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
* @author Claude Sonnet4
* @description 针对表【tag(标签表)】的数据库操作Service
* @since 2025-05-05 15:26:29
*/
public interface TagService extends IService<Tag> {

    BaseResponse<TagResponse> createTag(@Valid TagCreateRequest request, Long creatorUserId);

    List<TagTreeNode> getUserTagTree();

    TagResponse getTagById(Long tagId);

    BaseResponse<Boolean> deleteTag(Long tagId, HttpServletRequest request);
}
