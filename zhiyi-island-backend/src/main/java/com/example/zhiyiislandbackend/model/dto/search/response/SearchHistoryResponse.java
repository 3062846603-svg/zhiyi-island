package com.example.zhiyiislandbackend.model.dto.search.response;

import com.example.zhiyiislandbackend.model.entity.SearchHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 搜索历史响应DTO
 * 返回用户的搜索历史记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponse {
    /** 记录ID */
    private Long id;
    /** 搜索关键词 */
    private String keyword;
    /** 搜索类型 */
    private String searchType;
    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 从实体转换为响应DTO
     */
    public static SearchHistoryResponse fromEntity(SearchHistory history) {
        if (history == null) return null;
        return SearchHistoryResponse.builder()
                .id(history.getId())
                .keyword(history.getKeyword())
                .searchType(history.getSearchType())
                .createTime(history.getCreateTime())
                .build();
    }
}
