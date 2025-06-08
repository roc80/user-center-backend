package com.yupi.usercenter.controller;

import com.yupi.usercenter.model.TagTreeNode;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.dto.UserDTO;
import com.yupi.usercenter.model.request.CreateTagRequest;
import com.yupi.usercenter.model.response.TagResponse;
import com.yupi.usercenter.service.TagService;
import com.yupi.usercenter.utils.UserHelper;
import com.yupi.usercenter.utils.aspect.RequiredLogin;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * @author Claude Sonnet4
 * @description TODO
 * @since 2025/6/7 20:06
 */
@RestController
@RequestMapping("/tags")
@Validated
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * 创建标签
     */
    @PostMapping
    public BaseResponse<TagResponse> createTag(
            @Valid @RequestBody CreateTagRequest createTagRequest,
            HttpServletRequest request
    ) {
        UserDTO userDTO = UserHelper.getUserDtoFromRequest(request);
        Long creatorUserId = userDTO.getUserId();
        return tagService.createTag(createTagRequest, creatorUserId);
    }

    /**
     * 获取用户标签树
     */
    @RequiredLogin
    @GetMapping("/tree")
    public BaseResponse<List<TagTreeNode>> getUserTagTree() {
        List<TagTreeNode> tagTree = tagService.getUserTagTree();
        return ResponseUtils.success(tagTree);
    }

    /**
     * 获取标签详情
     */
    @RequiredLogin
    @GetMapping("/{tagId}")
    public BaseResponse<TagResponse> getTagById(@PathVariable Long tagId) {
        TagResponse response = tagService.getTagById(tagId);
        return ResponseUtils.success(response);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{tagId}")
    public BaseResponse<Boolean> deleteTag(@PathVariable Long tagId, HttpServletRequest request) {
        return tagService.deleteTag(tagId, request);
    }
}
