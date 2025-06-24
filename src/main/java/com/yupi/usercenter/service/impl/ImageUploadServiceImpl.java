package com.yupi.usercenter.service.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.yupi.usercenter.config.TencentCOSConfig;
import com.yupi.usercenter.config.UploadAvatarConfig;
import com.yupi.usercenter.exception.BusinessException;
import com.yupi.usercenter.model.base.Error;
import com.yupi.usercenter.service.ImageUploadService;
import com.yupi.usercenter.utils.FileValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Claude Sonnet4
 * @description
 * @since 2025/6/24 15:01
 */
@Service
@Slf4j
public class ImageUploadServiceImpl implements ImageUploadService {
    private final COSClient cosClient;
    private final TencentCOSConfig cosConfig;
    private final UploadAvatarConfig uploadAvatarConfig;

    public ImageUploadServiceImpl(COSClient cosClient, TencentCOSConfig cosConfig, UploadAvatarConfig uploadAvatarConfig) {
        this.cosClient = cosClient;
        this.cosConfig = cosConfig;
        this.uploadAvatarConfig = uploadAvatarConfig;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            // 1. 文件校验
            validateFile(file);

            // 2. 生成唯一文件名
            String objectKey = generateObjectKey(file.getOriginalFilename(), userId);

            // 3. 设置对象元数据
            ObjectMetadata metadata = createObjectMetadata(file);

            // 4. 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucketName(), objectKey, file.getInputStream(), metadata);

            // 5. 执行上传
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            log.info("文件上传成功，ETag: {}, 文件路径: {}", putObjectResult.getETag(), objectKey);

            // 6. 返回文件访问URL
            return cosConfig.getBaseUrl() + "/" + objectKey;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(Error.SERVER_ERROR, "文件上传失败");
        } catch (Exception e) {
            log.error("文件上传异常", e);
            throw new BusinessException(Error.SERVER_ERROR, "文件上传异常");
        }
    }

    /**
     * 文件校验
     */
    private void validateFile(MultipartFile file) {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "上传文件不能为空");
        }

        // 2. 检查文件大小
        if (!FileValidationUtil.validateFileSize(file, uploadAvatarConfig.getMaxSize())) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "文件大小不能超过 " + (uploadAvatarConfig.getMaxSize() / 1024 / 1024) + "MB");
        }

        // 3. 检查文件类型
        if (!FileValidationUtil.validateContentType(file, uploadAvatarConfig.getAllowedTypeList())) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "不支持的文件类型，仅支持: " + uploadAvatarConfig.getAllowedTypes());
        }

        // 4. 检查文件头（防止伪造）
        if (!FileValidationUtil.validateFileHeader(file)) {
            throw new BusinessException(Error.CLIENT_OPERATION_DENIED, "文件格式不正确或文件已损坏");
        }
    }

    /**
     * 生成唯一的对象键（文件路径）
     * 格式: avatars/2024/01/15/user_1001_20240115143022_abc12345.jpg
     */
    private String generateObjectKey(String originalFilename, Long userId) {
        // 获取文件扩展名
        String extension = FileValidationUtil.getFileExtension(originalFilename);
        if (extension.isEmpty()) {
            extension = "jpg"; // 默认扩展名
        }

        // 生成日期路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 生成时间戳
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 生成随机字符串
        String randomString = RandomStringUtils.randomAlphanumeric(8);

        // 构建完整的对象键
        return String.format("avatars/%s/user_%d_%s_%s.%s", datePath, userId, timestamp, randomString, extension);
    }

    /**
     * 创建对象元数据
     */
    private ObjectMetadata createObjectMetadata(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();

        // 设置内容类型
        metadata.setContentType(file.getContentType());

        // 设置内容长度
        metadata.setContentLength(file.getSize());

        // 设置缓存控制
        metadata.setCacheControl("max-age=31536000"); // 1年

        // 设置内容处置
        metadata.setContentDisposition("inline");

        return metadata;
    }
}
