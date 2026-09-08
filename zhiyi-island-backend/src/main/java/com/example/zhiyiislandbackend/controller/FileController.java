package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.service.FileService;
import com.example.zhiyiislandbackend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件控制器
 * 处理文件上传相关的HTTP请求，包括笔记图片和用户头像上传
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    /**
     * 上传笔记图片
     * 支持常见图片格式，单张图片最大10MB
     *
     * @param file 上传的图片文件
     * @return 包含图片URL的响应
     */
    @PostMapping("/upload/note-image")
    public Result<Map<String, String>> uploadNoteImage(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到笔记图片上传请求，用户ID：{}", userId);

        String url = fileService.uploadNoteImage(userId, file);

        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        return Result.success(data);
    }

    /**
     * 上传用户头像
     * 支持常见图片格式，头像最大2MB
     *
     * @param file 上传的头像文件
     * @return 包含头像URL的响应
     */
    @PostMapping("/upload/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到头像上传请求，用户ID：{}", userId);

        String url = fileService.uploadAvatar(userId, file);

        Map<String, String> data = new HashMap<>();
        data.put("url", url);

        return Result.success(data);
    }
}
