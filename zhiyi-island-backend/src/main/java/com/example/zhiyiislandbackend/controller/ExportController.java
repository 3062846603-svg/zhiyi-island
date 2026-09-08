package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.model.dto.export.request.ExportRequest;
import com.example.zhiyiislandbackend.model.dto.export.response.ExportDataResponse;
import com.example.zhiyiislandbackend.model.dto.export.response.ExportRecordResponse;
import com.example.zhiyiislandbackend.service.DataTransferService;
import com.example.zhiyiislandbackend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 导出控制器
 * 处理数据导出相关的HTTP请求，支持多种格式导出
 */
@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final DataTransferService dataTransferService;

    /**
     * 导出数据
     *
     * @param request 导出请求体
     * @return 导出的文件
     */
    @PostMapping
    public ResponseEntity<byte[]> exportData(@RequestBody ExportRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到数据导出请求，用户ID：{}，格式：{}", userId, request.getFormat());

        ExportDataResponse response = dataTransferService.exportData(userId, request);

        String encodedFileName = URLEncoder.encode(response.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        byte[] content;
        if (response.isBinary()) {
            content = response.getBinaryContent();
        } else {
            content = response.getContent().getBytes(StandardCharsets.UTF_8);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .contentType(MediaType.parseMediaType(response.getContentType()))
                .body(content);
    }

    /**
     * 获取导出历史
     *
     * @return 导出历史列表
     */
    @GetMapping("/history")
    public Result<List<ExportRecordResponse>> getExportHistory() {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到获取导出历史请求，用户ID：{}", userId);
        List<ExportRecordResponse> history = dataTransferService.getExportHistory(userId);
        return Result.success(history);
    }
}
