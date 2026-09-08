package com.example.zhiyiislandbackend.model.dto.imports.request;

import lombok.Data;

/**
 * 文件导入请求DTO
 * 指定文件导入的处理选项
 *
 * <p>导入流程说明：
 * <ul>
 *   <li>AI服务可用时：自动使用AI解析内容、智能分段、生成标题和分类，并自动创建知识库</li>
 *   <li>AI服务不可用时：回退到基础解析，按段落分割内容</li>
 * </ul>
 * </p>
 */
@Data
public class ImportRequest {
    /**
     * 是否使用AI生成摘要
     * 开启后会在导入笔记时自动调用AI为每条笔记生成摘要
     * 默认关闭，因为会增加导入耗时
     */
    private Boolean aiSummary = false;
}
