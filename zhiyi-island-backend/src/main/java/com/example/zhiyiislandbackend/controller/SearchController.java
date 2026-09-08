package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.model.dto.ai.request.AiSemanticChatRequest;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiSearchResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiSemanticChatResponse;
import com.example.zhiyiislandbackend.model.dto.search.response.SearchHistoryResponse;
import com.example.zhiyiislandbackend.model.dto.search.response.SearchResultResponse;
import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.Note;
import com.example.zhiyiislandbackend.model.entity.SearchHistory;
import com.example.zhiyiislandbackend.service.*;
import com.example.zhiyiislandbackend.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 搜索控制器
 * 处理搜索相关的HTTP请求，包括普通搜索、AI搜索、智能搜索、语义问答和搜索历史管理
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchHistoryService searchHistoryService;
    private final NoteService noteService;
    private final KnowledgeService knowledgeService;
    private final AiService aiService;
    private final IdempotencyService idempotencyService;

    /**
     * 普通搜索
     * 根据关键词搜索笔记和知识库，支持按类型筛选
     *
     * @param query 搜索关键词
     * @param type  搜索类型：all/note/knowledge
     * @return 搜索结果列表
     */
    @GetMapping
    public Result<List<SearchResultResponse>> search(@RequestParam String query,
                                                     @RequestParam(defaultValue = "all") String type) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到搜索请求，用户ID：{}，关键词：{}，类型：{}", userId, query, type);

        if (query == null || query.trim().isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<SearchResultResponse> results = new ArrayList<>();

        if ("all".equals(type) || "note".equals(type)) {
            List<Note> notes = noteService.search(userId, query);
            results.addAll(notes.stream()
                    .map(SearchResultResponse::fromNote)
                    .toList());
        }

        if ("all".equals(type) || "knowledge".equals(type)) {
            List<Knowledge> knowledgeList = knowledgeService.getByUserId(userId);
            String lowerQuery = query.toLowerCase();
            results.addAll(knowledgeList.stream()
                    .filter(k -> k.getTitle() != null && k.getTitle().toLowerCase().contains(lowerQuery))
                    .map(SearchResultResponse::fromKnowledge)
                    .toList());
        }

        searchHistoryService.record(userId, query, "search");
        return Result.success(results);
    }

    /**
     * AI搜索
     * 使用AI扩展搜索词，提供更智能的搜索结果
     *
     * @param request 包含搜索关键词的请求体
     * @return AI搜索响应
     */
    @PostMapping("/ai")
    public Result<AiSearchResponse> aiSearch(@RequestBody java.util.Map<String, String> request) {
        Long userId = SecurityUtil.getRequiredUserId();
        String query = request.get("query");
        log.info("收到AI搜索请求，用户ID：{}，关键词：{}", userId, query);

        if (query == null || query.trim().isEmpty()) {
            return Result.success(AiSearchResponse.builder()
                    .results(new ArrayList<>())
                    .expandedTerms(new ArrayList<>())
                    .total(0)
                    .build());
        }

        String idempotencyKey = idempotencyService.buildKey(userId, "ai-search", query);
        if (!idempotencyService.tryAcquire(idempotencyKey)) {
            log.warn("AI搜索请求正在处理中，用户ID：{}，关键词：{}", userId, query);
            return Result.error(429, "请求正在处理中，请稍候");
        }

        try {
            AiSearchResponse response = aiService.aiSearch(userId, query);
            searchHistoryService.record(userId, query, "ai");
            return Result.success(response);
        } finally {
            idempotencyService.release(idempotencyKey);
        }
    }

    /**
     * 智能搜索
     * 结合AI语义理解，提供更精准的搜索结果
     *
     * @param request 包含搜索关键词的请求体
     * @return 智能搜索响应
     */
    @PostMapping("/intelligent")
    public Result<AiSearchResponse> intelligentSearch(@RequestBody java.util.Map<String, String> request) {
        Long userId = SecurityUtil.getRequiredUserId();
        String query = request.get("query");
        log.info("收到智能搜索请求，用户ID：{}，关键词：{}", userId, query);

        if (query == null || query.trim().isEmpty()) {
            return Result.success(AiSearchResponse.builder()
                    .results(new ArrayList<>())
                    .expandedTerms(new ArrayList<>())
                    .total(0)
                    .build());
        }

        String idempotencyKey = idempotencyService.buildKey(userId, "intelligent-search", query);
        if (!idempotencyService.tryAcquire(idempotencyKey)) {
            log.warn("智能搜索请求正在处理中，用户ID：{}，关键词：{}", userId, query);
            return Result.error(429, "请求正在处理中，请稍候");
        }

        try {
            AiSearchResponse response = aiService.intelligentSearch(userId, query);
            searchHistoryService.record(userId, query, "intelligent");
            return Result.success(response);
        } finally {
            idempotencyService.release(idempotencyKey);
        }
    }

    /**
     * 语义问答
     * 基于用户笔记和知识库进行语义理解和问答
     *
     * @param request 语义问答请求体
     * @return 语义问答响应
     */
    @PostMapping("/semantic-chat")
    public Result<AiSemanticChatResponse> semanticChat(@RequestBody AiSemanticChatRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到语义问答请求，用户ID：{}，问题：{}", userId, request.getQuestion());

        if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
            return Result.success(AiSemanticChatResponse.builder()
                    .answer("请输入您的问题")
                    .sourceType("ai")
                    .relatedContents(new ArrayList<>())
                    .keywords(new ArrayList<>())
                    .intent("general")
                    .confidence(0.0)
                    .build());
        }

        String idempotencyKey = idempotencyService.buildKey(userId, "semantic-chat", request.getQuestion());
        if (!idempotencyService.tryAcquire(idempotencyKey)) {
            log.warn("语义问答请求正在处理中，用户ID：{}，问题：{}", userId, request.getQuestion());
            return Result.error(429, "请求正在处理中，请稍候");
        }

        try {
            AiSemanticChatResponse response = aiService.semanticChat(userId, request);
            return Result.success(response);
        } finally {
            idempotencyService.release(idempotencyKey);
        }
    }

    /**
     * 获取搜索历史
     *
     * @param limit 返回数量限制
     * @return 搜索历史列表
     */
    @GetMapping("/history")
    public Result<List<SearchHistoryResponse>> getHistory(@RequestParam(defaultValue = "10") int limit) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到获取搜索历史请求，用户ID：{}，数量：{}", userId, limit);
        List<SearchHistory> history = searchHistoryService.getRecent(userId, limit);
        List<SearchHistoryResponse> responseList = history.stream()
                .map(SearchHistoryResponse::fromEntity)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 清空搜索历史
     * 删除用户所有搜索历史记录
     *
     * @return 操作结果
     */
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到清空搜索历史请求，用户ID：{}", userId);
        searchHistoryService.clear(userId);
        return Result.success("清空成功", null);
    }

    /**
     * 删除单条搜索历史
     *
     * @param id 搜索历史ID
     * @return 操作结果
     */
    @DeleteMapping("/history/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到删除搜索历史请求，ID：{}，用户ID：{}", id, userId);
        searchHistoryService.delete(id, userId);
        return Result.success("删除成功", null);
    }
}
