package com.yupi.usercenter.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileValidationUtil {
    
    // 文件头魔数映射
    private static final Map<String, String> FILE_TYPE_MAP = new HashMap<>();
    
    static {
        FILE_TYPE_MAP.put("ffd8ff", "image/jpeg");     // JPEG
        FILE_TYPE_MAP.put("89504e", "image/png");      // PNG
        FILE_TYPE_MAP.put("474946", "image/gif");      // GIF
        FILE_TYPE_MAP.put("524946", "image/webp");     // WEBP
    }
    
    /**
     * 校验文件大小
     */
    public static boolean validateFileSize(MultipartFile file, long maxSize) {
        return file.getSize() <= maxSize;
    }
    
    /**
     * 校验文件类型（基于Content-Type）
     */
    public static boolean validateContentType(MultipartFile file, List<String> allowedTypes) {
        String contentType = file.getContentType();
        return contentType != null && allowedTypes.contains(contentType.toLowerCase());
    }
    
    /**
     * 校验文件头（防止文件伪造）
     */
    public static boolean validateFileHeader(MultipartFile file) {
        try {
            byte[] fileHeader = new byte[3];
            file.getInputStream().read(fileHeader);
            String fileCode = bytesToHexString(fileHeader);
            
            return FILE_TYPE_MAP.containsKey(fileCode);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length == 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
