package com.example.zhiyiislandbackend.service;

import com.example.zhiyiislandbackend.model.dto.export.request.ExportRequest;
import com.example.zhiyiislandbackend.model.dto.export.response.ExportDataResponse;
import com.example.zhiyiislandbackend.model.dto.export.response.ExportRecordResponse;
import com.example.zhiyiislandbackend.model.dto.imports.request.ImportRequest;
import com.example.zhiyiislandbackend.model.dto.imports.response.ImportRecordResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据传输服务接口
 * 定义数据导入、导出和历史记录管理相关的操作
 */
public interface DataTransferService {

    /**
     * 导入文件
     *
     * @param userId  用户ID
     * @param file    上传的文件
     * @param request 导入选项
     * @return 导入记录响应
     */
    ImportRecordResponse importFile(Long userId, MultipartFile file, ImportRequest request);

    /**
     * 获取导入历史记录
     *
     * @param userId 用户ID
     * @return 导入历史列表
     */
    List<ImportRecordResponse> getImportHistory(Long userId);

    /**
     * 删除导入记录
     *
     * @param userId   用户ID
     * @param recordId 记录ID
     */
    void deleteImportRecord(Long userId, Long recordId);

    /**
     * 导出数据
     *
     * @param userId  用户ID
     * @param request 导出请求（格式、类型等）
     * @return 导出数据响应（文件内容、文件名等）
     */
    ExportDataResponse exportData(Long userId, ExportRequest request);

    /**
     * 获取导出历史记录
     *
     * @param userId 用户ID
     * @return 导出历史列表
     */
    List<ExportRecordResponse> getExportHistory(Long userId);
}
