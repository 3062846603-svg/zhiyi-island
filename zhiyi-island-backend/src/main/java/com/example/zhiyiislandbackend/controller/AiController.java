package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.model.dto.ai.request.AiChatRequest;
import com.example.zhiyiislandbackend.model.dto.ai.request.SummaryRequest;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiChatResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.KnowledgeGraphResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.SummaryResponse;
import com.example.zhiyiislandbackend.service.AiService;
import com.example.zhiyiislandbackend.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI控制器
 * 处理AI相关的HTTP请求，包括摘要生成、关键词提取和知识图谱生成
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 生成AI摘要
     *
     * @param request 摘要请求体
     * @return 生成的摘要
     */
    @PostMapping("/summary")
    public Result<SummaryResponse> generateSummary(@Valid @RequestBody SummaryRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到生成AI摘要请求，用户ID：{}", userId);
        SummaryResponse response = aiService.generateSummary(userId, request);
        return Result.success("生成成功", response);
    }

    /**
     * 获取AI摘要历史
     *
     * @param limit 返回数量限制
     * @return 摘要历史列表
     */
    @GetMapping("/summary/history")
    public Result<List<SummaryResponse>> getSummaryHistory(@RequestParam(defaultValue = "10") Integer limit) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到获取AI摘要历史请求，用户ID：{}，数量：{}", userId, limit);
        List<SummaryResponse> history = aiService.getSummaryHistory(userId, limit);
        return Result.success(history);
    }

    /**
     * 删除AI摘要
     *
     * @param id 摘要ID
     * @return 删除结果
     */
    @DeleteMapping("/summary/{id}")
    public Result<Void> deleteSummary(@PathVariable Long id) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到删除AI摘要请求，摘要ID：{}，用户ID：{}", id, userId);
        aiService.deleteSummary(userId, id);
        return Result.success("删除成功", null);
    }

    /**
     * 保存AI摘要到笔记
     *
     * @param id 摘要ID
     * @return 保存结果，包含新笔记ID
     */
    @PostMapping("/summary/{id}/save-to-note")
    public Result<Long> saveToNote(@PathVariable Long id) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到保存AI摘要到笔记请求，摘要ID：{}，用户ID：{}", id, userId);
        Long noteId = aiService.saveToNote(userId, id);
        return Result.success("已保存到笔记", noteId);
    }

    /**
     * 提取关键词
     *
     * @param content 待提取的内容
     * @return 提取的关键词
     */
    @PostMapping("/keywords")
    public Result<String> extractKeywords(@RequestBody String content) {
        log.info("收到提取关键词请求");
        String keywords = aiService.extractKeywords(content);
        return Result.success(keywords);
    }

    /**
     * 生成知识图谱
     *
     * @return 知识图谱数据
     */
    @GetMapping("/knowledge-graph")
    public Result<KnowledgeGraphResponse> generateKnowledgeGraph() {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到生成知识图谱请求，用户ID：{}", userId);
        KnowledgeGraphResponse graphData = aiService.generateKnowledgeGraph(userId);
        return Result.success(graphData);
    }

    /**
     * AI问答对话
     *
     * @param request 问答请求体
     * @return AI回答
     */
    @PostMapping("/chat")
    public Result<AiChatResponse> aiChat(@Valid @RequestBody AiChatRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到AI问答请求，用户ID：{}", userId);
        AiChatResponse response = aiService.aiChat(userId, request);
        return Result.success(response);
    }
}
