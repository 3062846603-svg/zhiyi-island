package com.example.zhiyiislandbackend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 * 定义文件上传、删除和URL获取等操作
 */
public interface FileService {

    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param file 头像文件
     * @return 头像访问URL
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 上传文件到指定存储桶
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @param file 文件
     * @return 文件访问URL
     */
    String uploadFile(String bucketName, String objectName, MultipartFile file);

    /**
     * 删除文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     */
    void deleteFile(String bucketName, String objectName);

    /**
     * 获取文件访问URL
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @return 文件访问URL
     */
    String getFileUrl(String bucketName, String objectName);

    /**
     * 上传笔记图片
     * @param userId 用户ID
     * @param file 图片文件
     * @return 图片访问URL
     */
    String uploadNoteImage(Long userId, MultipartFile file);
}
