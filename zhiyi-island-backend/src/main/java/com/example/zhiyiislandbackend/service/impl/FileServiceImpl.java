package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.common.constant.BusinessConstants;
import com.example.zhiyiislandbackend.common.constant.MessageConstants;
import com.example.zhiyiislandbackend.common.constant.ValidationConstants;
import com.example.zhiyiislandbackend.config.MinioConfig;
import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.service.FileService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件服务实现类
 * 基于MinIO对象存储实现文件上传、删除等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 上传用户头像
     * 1. 验证文件类型和大小
     * 2. 生成唯一文件名（按日期分目录）
     * 3. 上传到MinIO
     */
    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        log.info("开始上传头像，用户ID：{}", userId);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, MessageConstants.Error.FILE_EMPTY);
        }

        if (file.getSize() > ValidationConstants.File.AVATAR_MAX_SIZE) {
            throw new BusinessException(400, MessageConstants.Error.AVATAR_SIZE_EXCEEDED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, MessageConstants.Error.IMAGE_ONLY);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern(BusinessConstants.File.DATE_FORMAT));
        String objectName = BusinessConstants.File.AVATAR_PREFIX + datePath + "/" + userId + "_" + UUID.randomUUID() + extension;

        try {
            String bucketName = minioConfig.getBucketName();
            ensureBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            String fileUrl = getFileUrl(bucketName, objectName);
            log.info("头像上传成功，用户ID：{}，文件路径：{}", userId, objectName);
            return fileUrl;
        } catch (Exception e) {
            log.error("头像上传失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new BusinessException(500, MessageConstants.Error.AVATAR_UPLOAD_FAILED + e.getMessage());
        }
    }

    /**
     * 上传文件到指定存储桶
     */
    @Override
    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        log.info("开始上传文件，存储桶：{}，对象名：{}", bucketName, objectName);

        try {
            ensureBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String fileUrl = getFileUrl(bucketName, objectName);
            log.info("文件上传成功，存储桶：{}，对象名：{}", bucketName, objectName);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败，存储桶：{}，对象名：{}，错误：{}", bucketName, objectName, e.getMessage(), e);
            throw new BusinessException(500, MessageConstants.Error.FILE_UPLOAD_FAILED + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @Override
    public void deleteFile(String bucketName, String objectName) {
        log.info("开始删除文件，存储桶：{}，对象名：{}", bucketName, objectName);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功，存储桶：{}，对象名：{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("文件删除失败，存储桶：{}，对象名：{}，错误：{}", bucketName, objectName, e.getMessage(), e);
            throw new BusinessException(500, MessageConstants.Error.FILE_DELETE_FAILED + e.getMessage());
        }
    }

    /**
     * 获取文件访问URL
     * 返回长期有效的公开访问URL（存储桶已设置为公开访问）
     */
    @Override
    public String getFileUrl(String bucketName, String objectName) {
        return minioConfig.getPublicUrl() + "/" + bucketName + "/" + objectName;
    }

    /**
     * 确保存储桶存在
     * 如果不存在则创建并设置为公开访问
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            log.info("创建存储桶成功：{}", bucketName);

            setBucketPublicPolicy(bucketName);
        }
    }

    /**
     * 设置存储桶公开访问策略
     * 允许匿名用户读取存储桶中的文件，实现长期有效的URL
     */
    private void setBucketPublicPolicy(String bucketName) throws Exception {
        String policy = """
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": {
                                "AWS": ["*"]
                            },
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """.formatted(bucketName);

        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
        );
        log.info("设置存储桶公开访问策略成功：{}", bucketName);
    }

    @Override
    public String uploadNoteImage(Long userId, MultipartFile file) {
        log.info("开始上传笔记图片，用户ID：{}", userId);

        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        if (file.getSize() > ValidationConstants.File.NOTE_IMAGE_MAX_SIZE) {
            throw new BusinessException(400, "图片文件大小不能超过10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "只能上传图片文件");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = BusinessConstants.File.NOTE_IMAGE_PREFIX + datePath + "/" + userId + "_" + UUID.randomUUID() + extension;

        try {
            String bucketName = minioConfig.getBucketName();
            ensureBucketExists(bucketName);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            String fileUrl = getFileUrl(bucketName, objectName);
            log.info("笔记图片上传成功，用户ID：{}，文件路径：{}", userId, objectName);
            return fileUrl;
        } catch (Exception e) {
            log.error("笔记图片上传失败，用户ID：{}，错误：{}", userId, e.getMessage(), e);
            throw new BusinessException(500, "图片上传失败：" + e.getMessage());
        }
    }
}
