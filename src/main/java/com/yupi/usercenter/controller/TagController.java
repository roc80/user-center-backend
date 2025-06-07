package com.yupi.usercenter.controller;

import com.yupi.usercenter.model.TagTreeNode;
import com.yupi.usercenter.model.base.BaseResponse;
import com.yupi.usercenter.model.base.ResponseUtils;
import com.yupi.usercenter.model.request.CreateTagRequest;
import com.yupi.usercenter.model.response.TagResponse;
import com.yupi.usercenter.service.TagService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody CreateTagRequest request,
            @RequestHeader("User-Id") Long userId) {

        TagResponse response = tagService.createTag(request, userId);
        return ResponseUtils.success(response);
    }

    /**
     * 获取用户标签树
     */
    @GetMapping("/tree")
    public BaseResponse<List<TagTreeNode>> getUserTagTree(
            @RequestHeader("User-Id") Long userId) {

        List<TagTreeNode> tagTree = tagService.getUserTagTree(userId);
        return ResponseUtils.success(tagTree);
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{tagId}")
    public BaseResponse<TagResponse> getTagById(
            @PathVariable Long tagId,
            @RequestHeader("User-Id") Long userId) {

        TagResponse response = tagService.getTagById(tagId, userId);
        return ResponseUtils.success(response);
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{tagId}")
    public BaseResponse<Void> deleteTag(
            @PathVariable Long tagId,
            @RequestHeader("User-Id") Long userId) {

        tagService.deleteTag(tagId, userId);
        return ResponseUtils.success(null);
    }
}
