package com.example.zhiyiislandbackend.service;

import com.example.zhiyiislandbackend.model.dto.ai.request.AiChatRequest;
import com.example.zhiyiislandbackend.model.dto.ai.request.AiSemanticChatRequest;
import com.example.zhiyiislandbackend.model.dto.ai.request.SummaryRequest;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiChatResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiSearchResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiSemanticChatResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.KnowledgeExtractionResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.KnowledgeGraphResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.SummaryResponse;

import java.util.List;

/**
 * AI服务接口
 * 定义AI相关的业务操作，包括摘要生成、关键词提取和知识图谱生成
 */
public interface AiService {

    /**
     * 生成AI摘要
     *
     * @param userId  用户ID
     * @param request 摘要请求信息
     * @return 生成的摘要响应
     */
    SummaryResponse generateSummary(Long userId, SummaryRequest request);

    /**
     * 获取AI摘要历史
     *
     * @param userId 用户ID
     * @param limit  返回数量限制
     * @return 摘要历史列表
     */
    List<SummaryResponse> getSummaryHistory(Long userId, Integer limit);

    /**
     * 删除AI摘要
     *
     * @param userId    用户ID
     * @param summaryId 摘要ID
     */
    void deleteSummary(Long userId, Long summaryId);

    /**
     * 保存AI摘要到笔记
     *
     * @param userId    用户ID
     * @param summaryId 摘要ID
     * @return 新创建的笔记ID
     */
    Long saveToNote(Long userId, Long summaryId);

    /**
     * 提取关键词
     *
     * @param content 待提取的内容
     * @return 提取的关键词
     */
    String extractKeywords(String content);

    /**
     * 生成知识图谱
     *
     * @param userId 用户ID
     * @return 知识图谱数据
     */
    KnowledgeGraphResponse generateKnowledgeGraph(Long userId);

    /**
     * AI智能搜索
     *
     * @param userId 用户ID
     * @param query  搜索查询
     * @return AI搜索响应
     */
    AiSearchResponse aiSearch(Long userId, String query);

    /**
     * AI问答对话
     *
     * @param userId  用户ID
     * @param request 问答请求
     * @return AI问答响应
     */
    AiChatResponse aiChat(Long userId, AiChatRequest request);

    /**
     * 从笔记内容中提取知识点
     *
     * @param noteTitle   笔记标题
     * @param noteContent 笔记内容
     * @return 知识提取响应
     */
    KnowledgeExtractionResponse extractKnowledge(String noteTitle, String noteContent);

    /**
     * AI语义化问答
     * 理解用户自然语言问题，基于数据库内容生成针对性回答
     *
     * @param userId  用户ID
     * @param request 语义问答请求
     * @return 语义问答响应
     */
    AiSemanticChatResponse semanticChat(Long userId, AiSemanticChatRequest request);

    /**
     * AI智能搜索增强版
     * 理解用户自然语言查询意图，提供精准搜索结果
     *
     * @param userId 用户ID
     * @param query  搜索查询
     * @return AI搜索响应
     */
    AiSearchResponse intelligentSearch(Long userId, String query);

    /**
     * 为笔记生成AI摘要并保存到数据库
     *
     * @param noteId 笔记ID
     * @param userId 用户ID
     * @param title  笔记标题
     * @param content 笔记内容
     * @return 生成的摘要内容
     */
    String generateSummaryForNote(Long noteId, Long userId, String title, String content);

    /**
     * 为笔记生成AI摘要并保存到数据库（带选项）
     *
     * @param noteId  笔记ID
     * @param userId  用户ID
     * @param title   笔记标题
     * @param content 笔记内容
     * @param style   摘要风格
     * @param length  摘要长度
     * @return 生成的摘要内容
     */
    String generateSummaryForNote(Long noteId, Long userId, String title, String content, String style, String length);

    /**
     * 为笔记生成向量嵌入
     *
     * @param noteId 笔记ID
     * @param text   文本内容
     */
    void generateAndSaveEmbedding(Long noteId, String text);

    /**
     * 批量生成笔记向量嵌入
     *
     * @param userId    用户ID
     * @param batchSize 批次大小
     */
    void batchGenerateEmbeddings(Long userId, int batchSize);

    /**
     * 向量搜索笔记
     *
     * @param userId 用户ID
     * @param query  查询文本
     * @param limit  返回数量限制
     * @return 匹配的笔记列表
     */
    List<com.example.zhiyiislandbackend.model.entity.Note> searchByVector(Long userId, String query, int limit);

    /**
     * 带阈值的向量搜索
     *
     * @param userId    用户ID
     * @param query     查询文本
     * @param threshold 相似度阈值
     * @param limit     返回数量限制
     * @return 匹配的笔记列表
     */
    List<com.example.zhiyiislandbackend.model.entity.Note> searchByVectorWithThreshold(Long userId, String query, double threshold, int limit);

    /**
     * 生成文本的向量嵌入
     *
     * @param text 文本内容
     * @return 向量数组
     */
    float[] generateEmbedding(String text);
}
