package com.example.zhiyiislandbackend.model.dto.note.request;

import com.example.zhiyiislandbackend.common.constant.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 笔记请求DTO
 * 用于创建和更新笔记
 */
@Data
public class NoteRequest {
    /** 标题 */
    @NotBlank(message = ValidationConstants.Note.TITLE_NOT_BLANK)
    @Size(max = 200, message = ValidationConstants.Note.TITLE_SIZE)
    private String title;

    /** 内容 */
    @NotBlank(message = ValidationConstants.Note.CONTENT_NOT_BLANK)
    private String content;

    /** 分类 */
    private Integer category;

    /** 所属知识库ID */
    private Long knowledgeId;

    /** 来源 */
    private String source;

    /** 图片URL列表 */
    private List<String> images;

    /** 状态 */
    private Integer status;
}
