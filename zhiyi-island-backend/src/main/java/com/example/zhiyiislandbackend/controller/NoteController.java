package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.constant.MessageConstants;
import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.mapper.AiSummaryMapper;
import com.example.zhiyiislandbackend.mapper.NoteImageMapper;
import com.example.zhiyiislandbackend.model.dto.note.request.NoteRequest;
import com.example.zhiyiislandbackend.model.dto.note.request.SummaryOptionRequest;
import com.example.zhiyiislandbackend.model.dto.note.response.AiSummaryResponse;
import com.example.zhiyiislandbackend.model.dto.note.response.NoteResponse;
import com.example.zhiyiislandbackend.model.entity.AiSummary;
import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.Note;
import com.example.zhiyiislandbackend.model.entity.NoteImage;
import com.example.zhiyiislandbackend.model.enums.NoteCategoryEnum;
import com.example.zhiyiislandbackend.service.KnowledgeService;
import com.example.zhiyiislandbackend.service.NoteService;
import com.example.zhiyiislandbackend.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 笔记控制器
 * 处理笔记相关的HTTP请求，包括笔记的增删改查、草稿管理和AI摘要生成
 */
@Slf4j
@RestController
@RequestMapping("/api/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final KnowledgeService knowledgeService;
    private final NoteImageMapper noteImageMapper;
    private final AiSummaryMapper aiSummaryMapper;

    /**
     * 创建笔记
     *
     * @param request 笔记请求体，包含标题、内容、分类等信息
     * @return 创建成功的笔记信息
     */
    @PostMapping
    public Result<NoteResponse> create(@Valid @RequestBody NoteRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到创建笔记请求，用户ID：{}", userId);
        Note note = noteService.create(userId, request);
        return Result.success(MessageConstants.Success.CREATE, convertToResponse(note));
    }

    /**
     * 更新笔记
     *
     * @param id      笔记ID
     * @param request 笔记请求体，包含要更新的字段
     * @return 更新成功的笔记信息
     */
    @PutMapping("/{id}")
    public Result<NoteResponse> update(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到更新笔记请求，笔记ID：{}，用户ID：{}", id, userId);
        Note note = noteService.update(id, request);
        return Result.success(MessageConstants.Success.UPDATE, convertToResponse(note));
    }

    /**
     * 删除笔记
     *
     * @param id 笔记ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到删除笔记请求，笔记ID：{}，用户ID：{}", id, userId);
        noteService.delete(id, userId);
        return Result.success(MessageConstants.Success.DELETE, null);
    }

    /**
     * 根据ID获取笔记详情
     *
     * @param id 笔记ID
     * @return 笔记详情信息
     */
    @GetMapping("/{id}")
    public Result<NoteResponse> getById(@PathVariable Long id) {
        Note note = noteService.getById(id);
        return Result.success(convertToResponse(note));
    }

    /**
     * 获取笔记列表
     *
     * @param category 分类（可选），用于筛选指定分类的笔记
     * @return 笔记列表
     */
    @GetMapping("/list")
    public Result<List<NoteResponse>> list(@RequestParam(required = false) String category) {
        Long userId = SecurityUtil.getRequiredUserId();
        List<Note> notes;
        if (category != null && !category.isEmpty()) {
            notes = noteService.getByUserIdAndCategory(userId, category);
        } else {
            notes = noteService.getByUserId(userId);
        }
        List<NoteResponse> responseList = notes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 根据知识库ID获取笔记列表
     *
     * @param knowledgeId 知识库ID
     * @return 该知识库下的笔记列表
     */
    @GetMapping("/knowledge/{knowledgeId}")
    public Result<List<NoteResponse>> getByKnowledgeId(@PathVariable Long knowledgeId) {
        log.info("收到获取知识库笔记列表请求，知识库ID：{}", knowledgeId);
        List<Note> notes = noteService.getByKnowledgeId(knowledgeId);
        List<NoteResponse> responseList = notes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 搜索笔记
     *
     * @param keyword 搜索关键词，匹配标题和内容
     * @return 匹配的笔记列表
     */
    @GetMapping("/search")
    public Result<List<NoteResponse>> search(@RequestParam String keyword) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到搜索笔记请求，用户ID：{}，关键词：{}", userId, keyword);
        List<Note> notes = noteService.search(userId, keyword);
        List<NoteResponse> responseList = notes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 更新笔记的知识库关联
     *
     * @param id      笔记ID
     * @param request 知识库更新请求，包含新的知识库ID
     * @return 更新结果
     */
    @PutMapping("/{id}/knowledge")
    public Result<Void> updateKnowledgeId(@PathVariable Long id, @RequestBody KnowledgeUpdateRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到更新笔记知识库请求，笔记ID：{}，知识库ID：{}，用户ID：{}", id, request.getKnowledgeId(), userId);
        noteService.updateKnowledgeId(id, request.getKnowledgeId(), userId);
        return Result.success(MessageConstants.Success.OPERATION, null);
    }

    /**
     * 获取用户笔记数量
     *
     * @return 笔记数量
     */
    @GetMapping("/count")
    public Result<Integer> count() {
        Long userId = SecurityUtil.getRequiredUserId();
        int count = noteService.countByUserId(userId);
        return Result.success(count);
    }

    /**
     * 生成AI摘要
     * 根据笔记内容生成智能摘要，支持选择摘要风格和长度
     *
     * @param id      笔记ID
     * @param options 摘要选项（可选），包含风格和长度设置
     * @return 包含AI摘要的笔记信息
     */
    @PostMapping("/{id}/summary")
    public Result<NoteResponse> generateSummary(@PathVariable Long id, @RequestBody(required = false) SummaryOptionRequest options) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到生成AI摘要请求，笔记ID：{}，用户ID：{}，选项：{}", id, userId, options);
        Note note = noteService.generateSummary(id, userId, options);
        return Result.success(MessageConstants.Success.GENERATE, convertToResponse(note));
    }

    /**
     * 获取草稿列表
     *
     * @return 用户的所有草稿笔记列表
     */
    @GetMapping("/drafts")
    public Result<List<NoteResponse>> getDrafts() {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到获取草稿列表请求，用户ID：{}", userId);
        List<Note> notes = noteService.getDrafts(userId);
        List<NoteResponse> responseList = notes.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 获取草稿数量
     *
     * @return 草稿数量
     */
    @GetMapping("/drafts/count")
    public Result<Integer> countDrafts() {
        Long userId = SecurityUtil.getRequiredUserId();
        int count = noteService.countDrafts(userId);
        return Result.success(count);
    }

    /**
     * 发布草稿
     * 将草稿状态改为已发布
     *
     * @param id 草稿ID
     * @return 发布结果
     */
    @PutMapping("/{id}/publish")
    public Result<Void> publishDraft(@PathVariable Long id) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到发布草稿请求，笔记ID：{}，用户ID：{}", id, userId);
        noteService.publishDraft(id, userId);
        return Result.success(MessageConstants.Success.OPERATION, null);
    }

    /**
     * 获取所有笔记分类
     *
     * @return 分类列表，包含编码和描述
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = Arrays.stream(NoteCategoryEnum.values())
                .map(c -> Map.<String, Object>of("code", c.getCode(), "description", c.getDescription()))
                .collect(Collectors.toList());
        return Result.success(categories);
    }

    /**
     * 将笔记实体转换为响应对象
     * 包含关联的知识库名称、图片列表和AI摘要
     *
     * @param note 笔记实体
     * @return 笔记响应对象
     */
    private NoteResponse convertToResponse(Note note) {
        if (note == null) return null;

        NoteResponse response = NoteResponse.fromEntity(note);

        if (note.getKnowledgeId() != null) {
            Knowledge knowledge = knowledgeService.getById(note.getKnowledgeId());
            if (knowledge != null) {
                response.setKnowledgeName(knowledge.getTitle());
            }
        }

        List<NoteImage> images = noteImageMapper.selectByNoteId(note.getId());
        if (images != null && !images.isEmpty()) {
            response.setImages(images.stream()
                    .map(NoteImage::getImageUrl)
                    .collect(Collectors.toList()));
        }

        AiSummary latestSummary = aiSummaryMapper.selectLatestByNoteId(note.getId());
        if (latestSummary != null) {
            response.setAiSummary(AiSummaryResponse.fromEntity(latestSummary));
        }

        return response;
    }

    /**
     * 知识库更新请求
     * 用于更新笔记关联的知识库
     */
    @lombok.Data
    public static class KnowledgeUpdateRequest {
        /** 知识库ID，设为null表示取消关联 */
        private Long knowledgeId;
    }
}
