package com.example.zhiyiislandbackend.model.dto.export.request;

import lombok.Data;

/**
 * 数据导出请求DTO
 * 指定导出的数据范围和格式
 */
@Data
public class ExportRequest {
    /** 是否导出笔记 */
    private Boolean notes = true;
    /** 是否导出知识库 */
    private Boolean knowledge = true;
    /** 是否导出AI摘要 */
    private Boolean aiSummaries = true;
    /** 导出格式：json/markdown/txt */
    private String format = "json";
}
