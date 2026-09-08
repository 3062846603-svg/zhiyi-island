package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.model.dto.imports.request.ImportRequest;
import com.example.zhiyiislandbackend.model.dto.imports.response.ImportRecordResponse;
import com.example.zhiyiislandbackend.service.DataTransferService;
import com.example.zhiyiislandbackend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 导入控制器
 * 处理文件导入相关的HTTP请求，支持PDF、DOCX、TXT、MD、EPUB格式
 *
 * <p>导入流程：
 * <ol>
 *   <li>提取文件原始文本内容</li>
 *   <li>AI智能解析：自动分段、生成标题、分类、清理格式、创建知识库</li>
 *   <li>AI不可用时回退到基础解析</li>
 *   <li>可选：为每条笔记生成AI摘要</li>
 * </ol>
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final DataTransferService dataTransferService;

    /**
     * 导入文件
     * 自动使用AI解析内容并创建知识库，无需手动选择分类
     *
     * @param file      上传的文件（支持PDF、DOCX、TXT、MD、EPUB）
     * @param aiSummary 是否生成AI摘要（可选，默认false）
     * @return 导入结果，包含生成的笔记数量和知识库信息
     */
    @PostMapping
    public Result<ImportRecordResponse> importFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(required = false) Boolean aiSummary) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到文件导入请求，用户ID：{}，文件名：{}", userId, file.getOriginalFilename());

        ImportRequest request = new ImportRequest();
        if (aiSummary != null) request.setAiSummary(aiSummary);

        ImportRecordResponse response = dataTransferService.importFile(userId, file, request);
        return Result.success("导入成功", response);
    }

    /**
     * 获取导入历史
     * 返回当前用户的所有导入记录
     *
     * @return 导入历史列表
     */
    @GetMapping("/history")
    public Result<List<ImportRecordResponse>> getImportHistory() {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到获取导入历史请求，用户ID：{}", userId);
        List<ImportRecordResponse> history = dataTransferService.getImportHistory(userId);
        return Result.success(history);
    }

    /**
     * 删除导入记录
     * 仅删除记录，不影响已导入的数据
     *
     * @param id 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteImportRecord(@PathVariable Long id) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到删除导入记录请求，记录ID：{}，用户ID：{}", id, userId);
        dataTransferService.deleteImportRecord(userId, id);
        return Result.success("删除成功", null);
    }
}
