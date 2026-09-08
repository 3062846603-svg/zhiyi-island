package com.example.zhiyiislandbackend.model.dto.ai.response;

import com.example.zhiyiislandbackend.model.dto.search.response.SearchResultResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI搜索响应DTO
 * 返回AI智能搜索的结果，包含匹配结果和语义扩展词
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSearchResponse {
    /**
     * 匹配的搜索结果列表
     */
    private List<SearchResultResponse> results;
    /**
     * AI扩展的搜索词
     */
    private List<String> expandedTerms;
    /**
     * 结果总数
     */
    private Integer total;
}
