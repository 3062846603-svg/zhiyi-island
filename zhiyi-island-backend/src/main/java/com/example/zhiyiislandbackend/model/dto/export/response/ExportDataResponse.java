package com.example.zhiyiislandbackend.model.dto.export.response;

import lombok.Data;

/**
 * 导出数据响应DTO
 * 返回导出的文件内容
 */
@Data
public class ExportDataResponse {
    /**
     * 文件内容（文本格式）
     */
    private String content;

    /**
     * 文件内容（二进制格式，用于PDF等）
     */
    private byte[] binaryContent;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件MIME类型
     */
    private String contentType;

    /**
     * 是否为二进制内容
     */
    private boolean binary;
}
