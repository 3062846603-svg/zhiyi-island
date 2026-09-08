package com.example.zhiyiislandbackend.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置类
 * 配置MinIO客户端并初始化存储桶
 */
@Data
@Slf4j
@Configuration
public class MinioConfig implements CommandLineRunner {

    /** MinIO服务端点 */
    @Value("${minio.endpoint}")
    private String endpoint;

    /** 浏览器访问文件的地址；未配置时保持使用MinIO端点 */
    @Value("${minio.public-url:}")
    private String publicUrl;

    public String getPublicUrl() {
        String url = publicUrl == null || publicUrl.isBlank() ? endpoint : publicUrl;
        return url.replaceAll("/+$", "");
    }

    /** MinIO访问密钥 */
    @Value("${minio.access-key}")
    private String accessKey;

    /** MinIO私钥 */
    @Value("${minio.secret-key}")
    private String secretKey;

    /** 存储桶名称 */
    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * 创建MinIO客户端Bean
     */
    @Bean
    public MinioClient minioClient() {
        log.info("正在创建 MinIO 客户端，endpoint: {}", endpoint);
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 应用启动时初始化存储桶
     */
    @Override
    public void run(String... args) {
        try {
            log.info("开始初始化 MinIO 存储桶: {}", bucketName);

            MinioClient client = minioClient();
            boolean exists = client.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                client.makeBucket(
                        io.minio.MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建存储桶成功：{}", bucketName);
            }

            setBucketPublicPolicy(client, bucketName);
            log.info("MinIO存储桶初始化完成，存储桶：{}，访问策略：公开", bucketName);
        } catch (Exception e) {
            log.error("MinIO存储桶初始化失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 设置存储桶公开访问策略
     */
    private void setBucketPublicPolicy(MinioClient client, String bucketName) throws Exception {
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

        client.setBucketPolicy(
                io.minio.SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
        );
        log.info("设置存储桶公开访问策略成功：{}", bucketName);
    }
}
