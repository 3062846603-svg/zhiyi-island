package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.config.MinioConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileServiceUrlTest {
    @Test
    void defaultsToInternalEndpointForExistingLocalConfiguration() {
        MinioConfig config = new MinioConfig();
        config.setEndpoint("http://localhost:9005");
        assertEquals("http://localhost:9005/zhiyi-island/image.png",
                new FileServiceImpl(null, config).getFileUrl("zhiyi-island", "image.png"));
    }

    @Test
    void usesSameOriginPathWithoutExposingInternalEndpoint() {
        MinioConfig config = new MinioConfig();
        config.setEndpoint("http://127.0.0.1:9005");
        config.setPublicUrl("/files/");
        assertEquals("/files/zhiyi-island/notes/image.png",
                new FileServiceImpl(null, config).getFileUrl("zhiyi-island", "notes/image.png"));
    }

    @Test
    void supportsHttpsDomainForFuturePublicDeployment() {
        MinioConfig config = new MinioConfig();
        config.setEndpoint("http://127.0.0.1:9005");
        config.setPublicUrl("https://example.com/files");
        assertEquals("https://example.com/files/zhiyi-island/image.png",
                new FileServiceImpl(null, config).getFileUrl("zhiyi-island", "image.png"));
    }
}