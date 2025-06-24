package com.yupi.usercenter.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author lipeng
 * @description
 * @since 2025/6/24 14:59
 */
public interface ImageUploadService {
    /**
     * 上传头像图片
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 图片访问URL
     */
    String uploadAvatar(MultipartFile file, Long userId);
}
