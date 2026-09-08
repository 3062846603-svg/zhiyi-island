package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.model.dto.ai.request.AiChatRequest;
import com.example.zhiyiislandbackend.model.dto.ai.request.AiSemanticChatRequest;
import com.example.zhiyiislandbackend.model.dto.ai.request.SummaryRequest;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiChatResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiSearchResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.AiSemanticChatResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.KnowledgeExtractionResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.KnowledgeGraphResponse;
import com.example.zhiyiislandbackend.model.dto.ai.response.SummaryResponse;
import com.example.zhiyiislandbackend.model.dto.note.request.NoteRequest;
import com.example.zhiyiislandbackend.model.dto.search.response.SearchResultResponse;
import com.example.zhiyiislandbackend.model.entity.AiSummary;
import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.Note;
import com.example.zhiyiislandbackend.model.enums.NoteCategoryEnum;
import com.example.zhiyiislandbackend.model.enums.NotificationTypeEnum;
import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.mapper.AiSummaryMapper;
import com.example.zhiyiislandbackend.mapper.KnowledgeMapper;
import com.example.zhiyiislandbackend.mapper.NoteMapper;
import com.example.zhiyiislandbackend.service.AiService;
import com.example.zhiyiislandbackend.service.AiProviderService;
import com.example.zhiyiislandbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI服务实现类
 * 实现AI相关的业务逻辑，基于硅基流动API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiSummaryMapper aiSummaryMapper;
    private final NoteMapper noteMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final NotificationService notificationService;
    private final AiProviderService aiProviderService;

    @Value("${spring.ai.siliconflow.knowledge-model}")
    private String knowledgeModel;

    /**
     * 生成AI摘要
     * 根据请求的风格和长度生成摘要，并保存到数据库
     */
    @Override
    @Transactional
    public SummaryResponse generateSummary(Long userId, SummaryRequest request) {
        log.info("生成AI摘要，用户ID：{}，内容长度：{}", userId, request.getContent().length());

        String prompt = buildSummaryPrompt(request);
        String title = request.getTitle() != null ? request.getTitle() : "AI摘要";

        String summaryContent;
        try {
            summaryContent = aiProviderService.generateResponse(prompt);
            log.info("AI返回摘要内容长度：{}，内容预览：{}",
                    summaryContent == null ? "null" : summaryContent.length(),
                    summaryContent == null ? "null" : (summaryContent.length() > 100 ? summaryContent.substring(0, 100) + "..." : summaryContent));
        } catch (Exception e) {
            log.error("调用AI服务失败：{}", e.getMessage());
            String errorMsg = translateErrorMessage(e.getMessage());
            notificationService.createNotification(
                    userId,
                    "AI摘要生成失败",
                    "生成摘要《" + title + "》失败，原因：" + errorMsg + "。请稍后再试。",
                    NotificationTypeEnum.AI
            );
            throw new BusinessException(500, "AI服务暂时不可用，请稍后再试");
        }

        if (summaryContent == null || summaryContent.isBlank()) {
            log.error("AI返回的摘要内容为空");
            throw new BusinessException(500, "AI生成摘要失败，请重试");
        }

        AiSummary aiSummary = new AiSummary();
        aiSummary.setUserId(userId);
        aiSummary.setNoteId(null);
        aiSummary.setSummaryContent(summaryContent);
        aiSummary.setSummaryType("manual");
        aiSummary.setSummaryStyle(request.getStyle());
        aiSummary.setSummaryLength(request.getLength());
        aiSummary.setOriginalWordCount(request.getContent() != null ? request.getContent().length() : 0);
        aiSummary.setSummaryWordCount(summaryContent.length());
        aiSummary.setCreateTime(LocalDateTime.now());

        aiSummaryMapper.insert(aiSummary);

        notificationService.createNotification(
                userId,
                "AI摘要生成完成",
                "您的AI摘要已成功生成，共" + summaryContent.length() + "字。",
                NotificationTypeEnum.AI
        );

        return convertToResponse(aiSummary);
    }

    /**
     * 获取AI摘要历史
     * 按创建时间倒序返回用户的摘要历史
     */
    @Override
    public List<SummaryResponse> getSummaryHistory(Long userId, Integer limit) {
        log.info("获取AI摘要历史，用户ID：{}，数量：{}", userId, limit);
        List<AiSummary> summaries = aiSummaryMapper.selectByUserId(userId, limit);
        return summaries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 删除AI摘要
     * 验证用户权限后删除摘要
     */
    @Override
    public void deleteSummary(Long userId, Long summaryId) {
        log.info("删除AI摘要，用户ID：{}，摘要ID：{}", userId, summaryId);
        AiSummary summary = aiSummaryMapper.selectById(summaryId);
        if (summary == null) {
            throw new BusinessException(404, "摘要不存在");
        }
        if (!summary.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此摘要");
        }
        aiSummaryMapper.deleteById(summaryId);
    }

    /**
     * 保存AI摘要到笔记
     * 将摘要内容保存为新的笔记
     */
    @Override
    @Transactional
    public Long saveToNote(Long userId, Long summaryId) {
        log.info("保存AI摘要到笔记，用户ID：{}，摘要ID：{}", userId, summaryId);
        AiSummary summary = aiSummaryMapper.selectById(summaryId);
        if (summary == null) {
            throw new BusinessException(404, "摘要不存在");
        }
        if (!summary.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此摘要");
        }

        NoteRequest noteRequest = new NoteRequest();
        String noteTitle = "AI摘要";
        if (summary.getNoteId() != null) {
            Note originalNote = noteMapper.selectById(summary.getNoteId());
            if (originalNote != null) {
                noteTitle = originalNote.getTitle() + " - AI摘要";
            }
        }
        noteRequest.setTitle(noteTitle);
        noteRequest.setContent(summary.getSummaryContent());
        noteRequest.setCategory(NoteCategoryEnum.OTHER.getCode());

        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(noteRequest.getTitle());
        note.setContent(noteRequest.getContent());
        note.setCategory(NoteCategoryEnum.OTHER);
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());

        noteMapper.insert(note);
        log.info("AI摘要已保存为笔记，笔记ID：{}", note.getId());
        return note.getId();
    }

    /**
     * 提取关键词
     * 从内容中提取5-10个关键词
     */
    @Override
    public String extractKeywords(String content) {
        log.info("提取关键词，内容长度：{}", content.length());

        String prompt = "请从以下内容中提取5-10个关键词，用逗号分隔返回：\n\n" + content;

        try {
            return aiProviderService.generateResponse(prompt);
        } catch (Exception e) {
            log.error("调用AI服务失败：{}", e.getMessage());
            throw new BusinessException(500, "AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 生成知识图谱
     * 基于用户的笔记和知识库生成知识关联图谱
     */
    @Override
    public KnowledgeGraphResponse generateKnowledgeGraph(Long userId) {
        log.info("生成知识图谱数据，用户ID：{}", userId);

        List<Note> notes = noteMapper.selectByUserId(userId);
        List<Knowledge> knowledgeList = knowledgeMapper.selectByUserId(userId);

        if (notes.isEmpty() && knowledgeList.isEmpty()) {
            return KnowledgeGraphResponse.builder()
                    .nodes(List.of())
                    .edges(List.of())
                    .build();
        }

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("用户笔记标题：\n");
        for (Note note : notes) {
            contentBuilder.append("- ").append(note.getTitle()).append("\n");
        }
        contentBuilder.append("\n用户知识库：\n");
        for (Knowledge knowledge : knowledgeList) {
            contentBuilder.append("- ").append(knowledge.getTitle()).append(" (").append(knowledge.getCategory() != null ? knowledge.getCategory().getDescription() : "").append(")\n");
        }

        String prompt = """
                基于以下用户的笔记和知识库内容，生成一个知识图谱的JSON数据。
                返回格式如下（只返回JSON，不要其他内容）：
                {
                    "nodes": [
                        {"id": 1, "label": "概念名称", "level": 0, "color": "#667eea"}
                    ],
                    "edges": [
                        {"from": 1, "to": 2}
                    ]
                }
                
                用户数据：
                """ + contentBuilder.toString();

        try {
            String result = aiProviderService.generateResponse(prompt);
            String cleanedResult = null;
            if (result != null) {
                cleanedResult = result.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            }

            return parseKnowledgeGraphJson(cleanedResult);
        } catch (Exception e) {
            log.error("调用AI服务失败：{}", e.getMessage());
            String errorMsg = translateErrorMessage(e.getMessage());
            notificationService.createNotification(
                    userId,
                    "知识图谱生成失败",
                    "生成知识图谱失败，原因：" + errorMsg + "。请稍后再试。",
                    NotificationTypeEnum.AI
            );
            throw new BusinessException(500, "AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 解析知识图谱JSON响应
     */
    private KnowledgeGraphResponse parseKnowledgeGraphJson(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, KnowledgeGraphResponse.class);
        } catch (Exception e) {
            log.error("解析知识图谱JSON失败：{}", e.getMessage());
            return KnowledgeGraphResponse.builder()
                    .nodes(List.of())
                    .edges(List.of())
                    .build();
        }
    }

    /**
     * 构建摘要生成的提示词
     * 根据风格和长度参数构建不同的提示词
     */
    private String buildSummaryPrompt(SummaryRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("请对以下内容进行摘要。\n\n");

        if ("keypoints".equals(request.getStyle())) {
            prompt.append("请以要点列表的形式输出摘要，每个要点用数字标记。\n");
        } else {
            prompt.append("请以段落形式输出摘要。\n");
        }

        if ("short".equals(request.getLength())) {
            prompt.append("摘要长度控制在100字以内。\n");
        } else if ("detailed".equals(request.getLength())) {
            prompt.append("摘要长度控制在300-500字。\n");
        } else {
            prompt.append("摘要长度控制在200-300字。\n");
        }

        prompt.append("\n原文内容：\n").append(request.getContent());

        return prompt.toString();
    }

    /**
     * 将AI摘要实体转换为响应DTO
     */
    private SummaryResponse convertToResponse(AiSummary aiSummary) {
        SummaryResponse response = new SummaryResponse();
        response.setId(aiSummary.getId());
        if (aiSummary.getNoteId() != null) {
            Note note = noteMapper.selectById(aiSummary.getNoteId());
            if (note != null) {
                response.setTitle(note.getTitle());
            }
        }
        response.setSummaryContent(aiSummary.getSummaryContent());
        response.setSummaryStyle(aiSummary.getSummaryStyle());
        response.setSummaryLength(aiSummary.getSummaryLength());
        response.setWordCount(aiSummary.getSummaryWordCount());
        response.setCreateTime(aiSummary.getCreateTime());
        return response;
    }

    /**
     * 将英文错误信息转换为中文
     */
    private String translateErrorMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "未知错误";
        }

        String lowerMsg = message.toLowerCase();

        if (lowerMsg.contains("connection refused") || lowerMsg.contains("connect")) {
            return "连接失败";
        }
        if (lowerMsg.contains("timeout") || lowerMsg.contains("timed out")) {
            return "连接超时";
        }
        if (lowerMsg.contains("not found")) {
            return "服务未找到";
        }
        if (lowerMsg.contains("unauthorized") || lowerMsg.contains("forbidden")) {
            return "权限不足";
        }
        if (lowerMsg.contains("internal server error") || lowerMsg.contains("500")) {
            return "服务器内部错误";
        }
        if (lowerMsg.contains("service unavailable") || lowerMsg.contains("503")) {
            return "服务暂不可用";
        }
        if (lowerMsg.contains("bad request") || lowerMsg.contains("400")) {
            return "请求参数错误";
        }
        if (lowerMsg.contains("too many requests") || lowerMsg.contains("429")) {
            return "请求过于频繁";
        }
        if (lowerMsg.contains("memory") || lowerMsg.contains("heap")) {
            return "内存不足";
        }
        if (lowerMsg.contains("model") || lowerMsg.contains("not loaded")) {
            return "模型加载失败";
        }

        return "服务异常";
    }

    /**
     * AI智能搜索
     * 通过LLM进行语义扩展，再用扩展词搜索用户笔记和知识库
     */
    @Override
    public AiSearchResponse aiSearch(Long userId, String query) {
        log.info("AI智能搜索，用户ID：{}，查询：{}", userId, query);

        List<Note> allNotes = noteMapper.selectByUserId(userId);
        List<Knowledge> allKnowledge = knowledgeMapper.selectByUserId(userId);

        List<String> expandedTerms = expandSearchTerms(query);

        List<SearchResultResponse> keywordResults = semanticSearch(query, expandedTerms, allNotes, allKnowledge);

        List<SearchResultResponse> vectorResults = new ArrayList<>();
        try {
            List<Note> vectorNotes = searchByVectorWithThreshold(userId, query, 0.5, 10);
            for (Note note : vectorNotes) {
                boolean alreadyExists = keywordResults.stream()
                        .anyMatch(r -> r.getType().equals("note") && r.getId().equals(note.getId()));
                if (!alreadyExists) {
                    vectorResults.add(SearchResultResponse.fromNote(note));
                }
            }
            log.info("向量搜索找到 {} 条结果，去重后新增 {} 条", vectorNotes.size(), vectorResults.size());
        } catch (Exception e) {
            log.warn("向量搜索失败，仅使用关键词搜索: {}", e.getMessage());
        }

        List<SearchResultResponse> matchedResults = new ArrayList<>();
        matchedResults.addAll(keywordResults);
        matchedResults.addAll(vectorResults);

        return AiSearchResponse.builder()
                .results(matchedResults)
                .expandedTerms(expandedTerms)
                .total(matchedResults.size())
                .build();
    }

    /**
     * AI问答对话
     * 基于上下文内容回答用户问题，返回相关的笔记和知识库内容
     */
    @Override
    public AiChatResponse aiChat(Long userId, AiChatRequest request) {
        log.info("AI问答，用户ID：{}，问题：{}", userId, request.getQuestion());

        // 获取用户所有笔记和知识库
        List<Note> allNotes = noteMapper.selectByUserId(userId);
        List<Knowledge> allKnowledge = knowledgeMapper.selectByUserId(userId);

        // 用问题进行语义搜索
        List<String> expandedTerms = expandSearchTerms(request.getQuestion());
        List<SearchResultResponse> matchedResults = semanticSearch(request.getQuestion(), expandedTerms, allNotes, allKnowledge);

        // 构建上下文：将匹配到的内容发给LLM
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("用户的提问：").append(request.getQuestion()).append("\n\n");

        if (request.getContext() != null && !request.getContext().isEmpty()) {
            promptBuilder.append("当前查看的内容：\n").append(request.getContext()).append("\n\n");
        }

        if (!matchedResults.isEmpty()) {
            promptBuilder.append("从用户的笔记和知识库中找到以下相关内容：\n\n");
            for (int i = 0; i < Math.min(matchedResults.size(), 3); i++) {
                SearchResultResponse r = matchedResults.get(i);
                promptBuilder.append(i + 1).append(". 【").append(r.getType().equals("note") ? "笔记" : "知识库").append("】");
                promptBuilder.append(r.getTitle()).append("\n");
                if (r.getContent() != null) {
                    String content = r.getContent();
                    if (content.length() > 300) {
                        content = content.substring(0, 300) + "...";
                    }
                    promptBuilder.append("   ").append(content).append("\n");
                }
                promptBuilder.append("\n");
            }
        }

        promptBuilder.append("请基于以上信息，简洁准确地回答用户的问题。如果笔记中有相关内容，优先引用笔记内容。");

        try {
            String answer = aiProviderService.generateResponse(promptBuilder.toString());
            return AiChatResponse.builder().answer(answer).build();
        } catch (Exception e) {
            log.error("AI问答失败：{}", e.getMessage());
            throw new BusinessException(500, "AI服务暂时不可用，请稍后再试");
        }
    }

    /**
     * 语义扩展搜索词
     * 让LLM根据用户查询生成相关的搜索词
     */
    private List<String> expandSearchTerms(String query) {
        String prompt = """
                用户想要搜索以下内容："%s"
                
                请生成5个相关的搜索词或同义词，帮助找到更多相关内容。
                要求：
                1. 每个词占一行
                2. 只返回搜索词，不要编号、不要解释
                3. 搜索词应该涵盖不同的表述方式和相关概念
                
                搜索词：
                """.formatted(query);

        try {
            String response = aiProviderService.generateResponseWithoutThinking(prompt);
            return parseExpandedTerms(response);
        } catch (Exception e) {
            log.warn("语义扩展失败，使用原始查询：{}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 解析LLM返回的扩展词
     */
    private List<String> parseExpandedTerms(String response) {
        if (response == null || response.isBlank()) {
            return List.of();
        }
        return response.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.matches("^\\d+[.、].*"))  // 过滤掉编号行
                .map(line -> line.replaceAll("^[\\d]+[.、：:)]\\s*", ""))  // 去掉编号前缀
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 语义搜索
     * 使用原始查询和扩展词在笔记和知识库中搜索
     */
    private List<SearchResultResponse> semanticSearch(String query, List<String> expandedTerms,
                                                      List<Note> allNotes, List<Knowledge> allKnowledge) {
        // 收集所有搜索词（原始 + 扩展）
        List<String> allTerms = new ArrayList<>();
        allTerms.add(query);
        allTerms.addAll(expandedTerms);

        List<SearchResultResponse> results = new ArrayList<>();

        // 搜索笔记
        for (Note note : allNotes) {
            int score = calculateRelevanceScore(note.getTitle(), note.getContent(), null, null, allTerms);
            if (score > 0) {
                SearchResultResponse result = SearchResultResponse.fromNote(note);
                results.add(result);
            }
        }

        // 搜索知识库
        for (Knowledge knowledge : allKnowledge) {
            int score = calculateRelevanceScore(knowledge.getTitle(), knowledge.getDescription(), null, knowledge.getCategory() != null ? knowledge.getCategory().getDescription() : null, allTerms);
            if (score > 0) {
                SearchResultResponse result = SearchResultResponse.fromKnowledge(knowledge);
                results.add(result);
            }
        }

        // 按相关度排序（这里简单处理，标题匹配的排前面）
        results.sort((a, b) -> {
            boolean aTitleMatch = allTerms.stream().anyMatch(t -> a.getTitle() != null && a.getTitle().toLowerCase().contains(t.toLowerCase()));
            boolean bTitleMatch = allTerms.stream().anyMatch(t -> b.getTitle() != null && b.getTitle().toLowerCase().contains(t.toLowerCase()));
            if (aTitleMatch && !bTitleMatch) return -1;
            if (!aTitleMatch && bTitleMatch) return 1;
            return 0;
        });

        return results;
    }

    /**
     * 计算内容与搜索词的相关度分数
     */
    private int calculateRelevanceScore(String title, String content, String tags, String extra, List<String> terms) {
        int score = 0;
        for (String term : terms) {
            String lowerTerm = term.toLowerCase();
            if (title != null && title.toLowerCase().contains(lowerTerm)) {
                score += 10; // 标题匹配权重最高
            }
            if (content != null && content.toLowerCase().contains(lowerTerm)) {
                score += 3;  // 内容匹配
            }
            if (tags != null && tags.toLowerCase().contains(lowerTerm)) {
                score += 5;  // 标签匹配
            }
            if (extra != null && extra.toLowerCase().contains(lowerTerm)) {
                score += 2;  // 附加信息匹配
            }
        }
        return score;
    }

    /**
     * 从笔记内容中提取知识点
     * 使用AI分析笔记内容，提取关键知识点并建议知识库分类
     */
    @Override
    public KnowledgeExtractionResponse extractKnowledge(String noteTitle, String noteContent) {
        log.info("从笔记提取知识点，标题：{}，内容长度：{}", noteTitle, noteContent.length());

        String prompt = buildKnowledgeExtractionPrompt(noteTitle, noteContent);

        int maxRetries = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("知识点提取尝试 {}/{}", attempt, maxRetries);
                String response = aiProviderService.generateResponseWithModel(prompt, knowledgeModel, 0.1);

                if (response == null || response.isBlank()) {
                    log.warn("AI返回空响应，尝试次数：{}", attempt);
                    continue;
                }

                KnowledgeExtractionResponse result = parseKnowledgeExtractionResponse(response);
                if (result != null && result.getKnowledgePoints() != null && !result.getKnowledgePoints().isEmpty()) {
                    log.info("知识点提取成功，提取到 {} 个知识点", result.getKnowledgePoints().size());
                    return result;
                }

                log.warn("解析结果为空或无知识点，尝试次数：{}", attempt);
            } catch (Exception e) {
                log.error("知识点提取失败，尝试次数：{}，错误：{}", attempt, e.getMessage());
                lastException = e;
            }
        }

        log.error("知识点提取最终失败，已重试 {} 次", maxRetries);
        return KnowledgeExtractionResponse.builder()
                .knowledgeTitle(noteTitle.length() > 20 ? noteTitle.substring(0, 20) : noteTitle)
                .knowledgeCategory("其他")
                .knowledgePoints(List.of())
                .build();
    }

    /**
     * 构建知识点提取的提示词
     */
    private String buildKnowledgeExtractionPrompt(String noteTitle, String noteContent) {
        return """
                分析以下笔记，提取知识点。只返回JSON，不要其他内容。
                
                笔记标题：%s
                笔记内容：%s
                
                返回格式：
                {"knowledgeTitle":"标题","knowledgeCategory":"分类","knowledgePoints":[{"title":"知识点标题","content":"知识点内容"}]}
                
                分类选择规则（根据笔记内容严格匹配）：
                - 技术：编程、软件、AI、数据库、网络、系统架构、开发工具等
                - 学习：学习方法、考试、课程、教育、读书、语言学习等
                - 工作：职场、管理、沟通、效率、团队、会议、项目等
                - 生活：日常、旅行、美食、家居、购物、穿搭、娱乐等
                - 健康：运动、饮食、医疗、心理、养生、睡眠等
                - 财经：投资、理财、股票、保险、经济趋势等
                - 文化：历史、艺术、文学、音乐、电影、传统等
                - 其他：无法归入以上类别的
                
                要求：
                1. knowledgeTitle: 10-25字的知识库标题
                2. knowledgeCategory: 必须从上述8个分类中选择最合适的一个，不要用"其他"除非确实无法归类
                3. knowledgePoints: 3-6个知识点数组
                4. 每个知识点的title用动宾结构，content写80-200字
                5. 如果无知识点可提取，knowledgePoints为空数组[]
                """.formatted(noteTitle, noteContent);
    }

    /**
     * 解析知识点提取响应
     */
    private KnowledgeExtractionResponse parseKnowledgeExtractionResponse(String response) {
        if (response == null || response.isBlank()) {
            return null;
        }

        try {
            String cleanedResponse = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            int startIndex = cleanedResponse.indexOf("{");
            int endIndex = cleanedResponse.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                cleanedResponse = cleanedResponse.substring(startIndex, endIndex + 1);
            }

            if (!isValidJsonStructure(cleanedResponse)) {
                log.warn("JSON结构无效，尝试修复截断的JSON");
                cleanedResponse = repairTruncatedJson(cleanedResponse);
                
                if (!isValidJsonStructure(cleanedResponse)) {
                    log.warn("修复后JSON仍无效，响应内容: {}", cleanedResponse.substring(0, Math.min(200, cleanedResponse.length())));
                    return null;
                }
            }

            cleanedResponse = cleanedResponse
                    .replaceAll(",\\s*}", "}")
                    .replaceAll(",\\s*]", "]")
                    .replaceAll("\"\\s+\"", "\", \"")
                    .replaceAll("\"\\s*:\\s*:", "\":")
                    .replaceAll("\"\\s*:\\s*:\\s*\"", "\": \"")
                    .replaceAll("\"([a-zA-Z]+)\"\\s+\"", "\"$1\": \"")
                    .replaceAll("\"t\"\\s*:", "\"title\":")
                    .replaceAll("\"co[a-z]*\"\\s*:", "\"content\":")
                    .replaceAll("\\s+", " ");

            cleanedResponse = fixBrokenJsonStructure(cleanedResponse);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            KnowledgeExtractionResponse result = mapper.readValue(cleanedResponse, KnowledgeExtractionResponse.class);
            log.info("解析知识点成功 - 标题: {}, 分类: {}, 知识点数量: {}", 
                    result.getKnowledgeTitle(), 
                    result.getKnowledgeCategory(), 
                    result.getKnowledgePoints() != null ? result.getKnowledgePoints().size() : 0);
            return result;
        } catch (Exception e) {
            log.error("解析知识点提取响应失败，原始响应: {}", response.substring(0, Math.min(500, response.length())));
            log.error("错误详情: {}", e.getMessage());
            return null;
        }
    }

    private boolean isValidJsonStructure(String json) {
        if (json == null || json.length() < 50) {
            return false;
        }

        int braceCount = 0;
        int bracketCount = 0;
        int quoteCount = 0;
        boolean valid = true;

        for (int i = 0; i < json.length() && valid; i++) {
            char c = json.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;
            else if (c == '[') bracketCount++;
            else if (c == ']') bracketCount--;
            else if (c == '"') quoteCount++;

            if (braceCount < 0 || bracketCount < 0) {
                valid = false;
            }
        }

        return valid && braceCount == 0 && bracketCount == 0 && quoteCount % 2 == 0;
    }

    private String fixBrokenJsonStructure(String json) {
        StringBuilder fixed = new StringBuilder();
        char[] chars = json.toCharArray();
        boolean inString = false;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (c == '"' && (i == 0 || chars[i - 1] != '\\')) {
                inString = !inString;
                fixed.append(c);
            } else if (!inString) {
                if (c == ':' && i + 1 < chars.length && chars[i + 1] == ':') {
                    continue;
                }
                fixed.append(c);
            } else {
                fixed.append(c);
            }
        }

        return fixed.toString();
    }

    private String repairTruncatedJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        StringBuilder repaired = new StringBuilder(json);
        int braceCount = 0;
        int bracketCount = 0;
        boolean inString = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') braceCount++;
                else if (c == '}') braceCount--;
                else if (c == '[') bracketCount++;
                else if (c == ']') bracketCount--;
            }
        }

        if (inString) {
            repaired.append("\"");
        }

        int bracketsToAdd = bracketCount;
        int bracesToAdd = braceCount;

        while (bracketCount > 0) {
            repaired.append("]");
            bracketCount--;
        }

        while (braceCount > 0) {
            repaired.append("}");
            braceCount--;
        }

        if (bracketsToAdd > 0 || bracesToAdd > 0 || inString) {
            log.info("修复截断JSON，添加了 {} 个 ] 和 {} 个 }} 和 {} 个 \"", 
                    bracketsToAdd, bracesToAdd, inString ? 1 : 0);
        }

        return repaired.toString();
    }

    @Override
    public AiSemanticChatResponse semanticChat(Long userId, AiSemanticChatRequest request) {
        log.info("AI语义化问答，用户ID：{}，问题：{}", userId, request.getQuestion());

        List<Note> allNotes = noteMapper.selectByUserId(userId);
        List<Knowledge> allKnowledge = knowledgeMapper.selectByUserId(userId);

        String intent = recognizeIntent(request.getQuestion());
        List<String> keywords = extractKeywordsFromQuestion(request.getQuestion());

        List<SearchResultResponse> keywordResults = semanticSearch(
                request.getQuestion(),
                keywords,
                allNotes,
                allKnowledge
        );

        List<SearchResultResponse> vectorResults = new ArrayList<>();
        try {
            List<Note> vectorNotes = searchByVectorWithThreshold(userId, request.getQuestion(), 0.5, 5);
            for (Note note : vectorNotes) {
                boolean alreadyExists = keywordResults.stream()
                        .anyMatch(r -> r.getType().equals("note") && r.getId().equals(note.getId()));
                if (!alreadyExists) {
                    vectorResults.add(SearchResultResponse.fromNote(note));
                }
            }
            log.info("语义问答向量搜索找到 {} 条结果，去重后新增 {} 条", vectorNotes.size(), vectorResults.size());
        } catch (Exception e) {
            log.warn("语义问答向量搜索失败: {}", e.getMessage());
        }

        List<SearchResultResponse> matchedResults = new ArrayList<>();
        matchedResults.addAll(keywordResults);
        matchedResults.addAll(vectorResults);

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个智能助手，请基于用户的笔记和知识库回答问题。\n\n");
        promptBuilder.append("用户问题：").append(request.getQuestion()).append("\n\n");

        if (request.getContext() != null && !request.getContext().isEmpty()) {
            promptBuilder.append("当前上下文：\n").append(request.getContext()).append("\n\n");
        }

        if (!matchedResults.isEmpty()) {
            promptBuilder.append("从用户的知识库中找到以下相关内容：\n\n");
            int maxResults = Math.min(matchedResults.size(), 5);
            for (int i = 0; i < maxResults; i++) {
                SearchResultResponse r = matchedResults.get(i);
                promptBuilder.append("【").append(r.getType().equals("note") ? "笔记" : "知识库").append("】");
                promptBuilder.append(r.getTitle()).append("\n");
                if (r.getContent() != null) {
                    String content = r.getContent();
                    if (content.length() > 400) {
                        content = content.substring(0, 400) + "...";
                    }
                    promptBuilder.append(content).append("\n");
                }
                promptBuilder.append("\n");
            }
        } else {
            promptBuilder.append("注意：在用户的知识库中没有找到直接相关的内容。\n\n");
        }

        promptBuilder.append("请按照以下要求回答：\n");
        promptBuilder.append("1. 如果知识库中有相关内容，优先基于知识库内容回答，并在回答末尾标注【来源：知识库】\n");
        promptBuilder.append("2. 如果知识库中没有相关内容，可以基于通用知识回答，并在回答末尾标注【来源：通用知识】\n");
        promptBuilder.append("3. 回答要简洁、准确、有条理\n");
        promptBuilder.append("4. 如果问题涉及多个方面，请使用正确的序号分点说明（如：1. 2. 3.）\n");
        promptBuilder.append("5. 直接输出回答内容，不要输出任何提示语或建议\n");

        try {
            String answer = aiProviderService.generateResponse(promptBuilder.toString());

            List<AiSemanticChatResponse.RelatedContent> relatedContents = new ArrayList<>();
            if (Boolean.TRUE.equals(request.getReturnRelatedContent())) {
                for (int i = 0; i < Math.min(matchedResults.size(), 3); i++) {
                    SearchResultResponse r = matchedResults.get(i);
                    relatedContents.add(AiSemanticChatResponse.RelatedContent.builder()
                            .id(r.getId())
                            .type(r.getType())
                            .title(r.getTitle())
                            .summary(r.getContent())
                            .relevance(0.8 - i * 0.1)
                            .build());
                }
            }

            String sourceType = matchedResults.isEmpty() ? "ai" : "knowledge";

            return AiSemanticChatResponse.builder()
                    .answer(answer)
                    .sourceType(sourceType)
                    .relatedContents(relatedContents)
                    .keywords(keywords)
                    .intent(intent)
                    .confidence(0.85)
                    .build();
        } catch (Exception e) {
            log.error("AI语义化问答失败：{}", e.getMessage());
            throw new BusinessException(500, "AI服务暂时不可用，请稍后再试");
        }
    }

    @Override
    public AiSearchResponse intelligentSearch(Long userId, String query) {
        log.info("AI智能搜索增强版，用户ID：{}，查询：{}", userId, query);

        List<Note> allNotes = noteMapper.selectByUserId(userId);
        List<Knowledge> allKnowledge = knowledgeMapper.selectByUserId(userId);

        String intent = recognizeIntent(query);
        List<String> expandedTerms = expandSearchTerms(query);

        List<SearchResultResponse> results = semanticSearch(query, expandedTerms, allNotes, allKnowledge);

        if (results.isEmpty()) {
            String synonymPrompt = String.format(
                    "用户搜索\"%s\"没有找到结果。请生成3个可能的同义词或相关词，每行一个，只返回词语：",
                    query
            );
            try {
                String synonyms = aiProviderService.generateResponseWithoutThinking(synonymPrompt);
                List<String> synonymList = parseExpandedTerms(synonyms);
                results = semanticSearch(query, synonymList, allNotes, allKnowledge);
                expandedTerms.addAll(synonymList);
            } catch (Exception e) {
                log.warn("同义词扩展失败：{}", e.getMessage());
            }
        }

        results.sort((a, b) -> {
            int aScore = calculateRelevanceScore(a.getTitle(), a.getContent(), null, null, expandedTerms);
            int bScore = calculateRelevanceScore(b.getTitle(), b.getContent(), null, null, expandedTerms);
            return Integer.compare(bScore, aScore);
        });

        return AiSearchResponse.builder()
                .results(results)
                .expandedTerms(expandedTerms)
                .total(results.size())
                .build();
    }

    private String recognizeIntent(String question) {
        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("什么是") || lowerQuestion.contains("什么是") || lowerQuestion.contains("解释")) {
            return "definition";
        } else if (lowerQuestion.contains("如何") || lowerQuestion.contains("怎么") || lowerQuestion.contains("怎样")) {
            return "howto";
        } else if (lowerQuestion.contains("为什么") || lowerQuestion.contains("原因")) {
            return "why";
        } else if (lowerQuestion.contains("区别") || lowerQuestion.contains("对比") || lowerQuestion.contains("比较")) {
            return "compare";
        } else if (lowerQuestion.contains("例子") || lowerQuestion.contains("案例")) {
            return "example";
        } else if (lowerQuestion.contains("查找") || lowerQuestion.contains("搜索") || lowerQuestion.contains("找")) {
            return "search";
        } else {
            return "general";
        }
    }

    private List<String> extractKeywordsFromQuestion(String question) {
        String prompt = String.format("""
                请从以下问题中提取关键词，用于搜索相关知识库内容。
                
                问题：%s
                
                要求：
                1. 提取3-5个最重要的关键词
                2. 每个关键词占一行
                3. 只返回关键词，不要编号、不要解释
                4. 关键词应该是名词或专业术语
                
                关键词：
                """, question);

        try {
            String response = aiProviderService.generateResponse(prompt);
            return parseExpandedTerms(response);
        } catch (Exception e) {
            log.warn("关键词提取失败：{}", e.getMessage());
            return List.of(question);
        }
    }

    @Override
    public String generateSummaryForNote(Long noteId, Long userId, String title, String content) {
        return generateSummaryForNote(noteId, userId, title, content, "keypoints", "medium");
    }

    @Override
    public String generateSummaryForNote(Long noteId, Long userId, String title, String content, String style, String length) {
        log.info("为笔记生成AI摘要，笔记ID：{}，用户ID：{}，风格：{}，长度：{}", noteId, userId, style, length);

        SummaryRequest request = new SummaryRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setStyle(style != null ? style : "keypoints");
        request.setLength(length != null ? length : "medium");

        String prompt = buildSummaryPrompt(request);

        String summaryContent;
        try {
            summaryContent = aiProviderService.generateResponse(prompt);
            if (summaryContent == null || summaryContent.isBlank()) {
                log.error("AI返回的摘要内容为空");
                throw new BusinessException(500, "AI生成摘要失败，请重试");
            }
        } catch (Exception e) {
            log.error("调用AI服务失败：{}", e.getMessage());
            throw new BusinessException(500, "AI服务暂时不可用，请稍后再试");
        }

        AiSummary aiSummary = new AiSummary();
        aiSummary.setUserId(userId);
        aiSummary.setNoteId(noteId);
        aiSummary.setSummaryContent(summaryContent);
        aiSummary.setSummaryType("note");
        aiSummary.setSummaryStyle(style != null ? style : "keypoints");
        aiSummary.setSummaryLength(length != null ? length : "medium");
        aiSummary.setOriginalWordCount(content != null ? content.length() : 0);
        aiSummary.setSummaryWordCount(summaryContent.length());
        aiSummary.setCreateTime(LocalDateTime.now());

        aiSummaryMapper.insert(aiSummary);
        log.info("AI摘要保存成功，笔记ID：{}，摘要ID：{}", noteId, aiSummary.getId());

        return summaryContent;
    }

    @Override
    public void generateAndSaveEmbedding(Long noteId, String text) {
        try {
            log.info("为笔记生成向量，笔记ID: {}", noteId);
            float[] embedding = aiProviderService.generateEmbedding(text);
            if (embedding != null && embedding.length > 0) {
                noteMapper.updateEmbedding(noteId, embedding);
                log.info("笔记向量生成成功，笔记ID: {}, 向量维度: {}", noteId, embedding.length);
            } else {
                log.warn("笔记向量生成失败，返回空向量，笔记ID: {}", noteId);
            }
        } catch (Exception e) {
            log.error("生成笔记向量失败，笔记ID: {}, 错误: {}", noteId, e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void batchGenerateEmbeddings(Long userId, int batchSize) {
        log.info("批量生成笔记向量，用户ID: {}, 批次大小: {}", userId, batchSize);
        List<Note> notes = noteMapper.selectNotesWithoutEmbedding(userId, batchSize);
        int successCount = 0;
        int failCount = 0;

        for (Note note : notes) {
            try {
                String text = buildEmbeddingText(note);
                float[] embedding = aiProviderService.generateEmbedding(text);
                if (embedding != null && embedding.length > 0) {
                    noteMapper.updateEmbedding(note.getId(), embedding);
                    successCount++;
                } else {
                    failCount++;
                }
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("批量生成向量失败，笔记ID: {}, 错误: {}", note.getId(), e.getMessage());
                failCount++;
            }
        }

        log.info("批量生成向量完成，成功: {}, 失败: {}", successCount, failCount);
    }

    @Override
    public List<Note> searchByVector(Long userId, String query, int limit) {
        log.info("向量搜索，用户ID: {}, 查询: {}, 限制: {}", userId, query, limit);
        try {
            float[] queryEmbedding = aiProviderService.generateEmbedding(query);
            if (queryEmbedding == null || queryEmbedding.length == 0) {
                log.warn("查询向量生成失败");
                return List.of();
            }
            return noteMapper.searchByVector(userId, queryEmbedding, limit);
        } catch (Exception e) {
            log.error("向量搜索失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<Note> searchByVectorWithThreshold(Long userId, String query, double threshold, int limit) {
        log.info("向量搜索（带阈值），用户ID: {}, 查询: {}, 阈值: {}, 限制: {}", userId, query, threshold, limit);
        try {
            float[] queryEmbedding = aiProviderService.generateEmbedding(query);
            if (queryEmbedding == null || queryEmbedding.length == 0) {
                log.warn("查询向量生成失败");
                return List.of();
            }
            return noteMapper.searchByVectorWithThreshold(userId, queryEmbedding, threshold, limit);
        } catch (Exception e) {
            log.error("向量搜索失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public float[] generateEmbedding(String text) {
        return aiProviderService.generateEmbedding(text);
    }

    private String buildEmbeddingText(Note note) {
        StringBuilder sb = new StringBuilder();
        if (note.getTitle() != null) {
            sb.append(note.getTitle()).append("\n\n");
        }
        if (note.getContent() != null) {
            String content = note.getContent();
            if (content.length() > 2000) {
                content = content.substring(0, 2000);
            }
            sb.append(content);
        }
        return sb.toString();
    }
}
