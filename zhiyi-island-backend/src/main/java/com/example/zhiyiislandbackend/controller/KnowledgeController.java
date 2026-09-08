package com.example.zhiyiislandbackend.controller;

import com.example.zhiyiislandbackend.common.result.Result;
import com.example.zhiyiislandbackend.model.dto.knowledge.request.KnowledgeItemRequest;
import com.example.zhiyiislandbackend.model.dto.knowledge.request.KnowledgeRequest;
import com.example.zhiyiislandbackend.model.dto.knowledge.response.KnowledgeResponse;
import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.KnowledgeItem;
import com.example.zhiyiislandbackend.service.KnowledgeService;
import com.example.zhiyiislandbackend.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库控制器
 * 处理知识库和知识条目相关的HTTP请求
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 创建知识库
     *
     * @param request 知识库请求体
     * @return 创建成功的知识库信息
     */
    @PostMapping
    public Result<KnowledgeResponse> create(@Valid @RequestBody KnowledgeRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到创建知识库请求，用户ID：{}", userId);
        Knowledge knowledge = knowledgeService.create(userId, request);
        return Result.success("创建成功", KnowledgeResponse.fromEntity(knowledge));
    }

    /**
     * 更新知识库
     *
     * @param id      知识库ID
     * @param request 知识库请求体
     * @return 更新成功的知识库信息
     */
    @PutMapping("/{id}")
    public Result<KnowledgeResponse> update(@PathVariable Long id, @Valid @RequestBody KnowledgeRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到更新知识库请求，知识库ID：{}，用户ID：{}", id, userId);
        Knowledge knowledge = knowledgeService.update(id, request);
        return Result.success("更新成功", KnowledgeResponse.fromEntity(knowledge));
    }

    /**
     * 删除知识库
     *
     * @param id 知识库ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到删除知识库请求，知识库ID：{}，用户ID：{}", id, userId);
        knowledgeService.delete(id, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取用户知识库数量
     *
     * @return 知识库数量
     */
    @GetMapping("/count")
    public Result<Integer> count() {
        Long userId = SecurityUtil.getRequiredUserId();
        int count = knowledgeService.countByUserId(userId);
        return Result.success(count);
    }

    /**
     * 获取用户知识条目数量
     *
     * @return 知识条目数量
     */
    @GetMapping("/item-count")
    public Result<Integer> itemCount() {
        Long userId = SecurityUtil.getRequiredUserId();
        int count = knowledgeService.countItemsByUserId(userId);
        return Result.success(count);
    }

    /**
     * 根据ID获取知识库详情（包含知识条目）
     *
     * @param id 知识库ID
     * @return 知识库详情
     */
    @GetMapping("/{id}")
    public Result<KnowledgeResponse> getById(@PathVariable Long id) {
        Knowledge knowledge = knowledgeService.getById(id);
        List<KnowledgeItem> items = knowledgeService.getItemsByKnowledgeId(id);
        return Result.success(KnowledgeResponse.fromEntityWithItems(knowledge, items));
    }

    /**
     * 获取知识库列表
     *
     * @param category 分类（可选）
     * @return 知识库列表
     */
    @GetMapping("/list")
    public Result<List<KnowledgeResponse>> list(@RequestParam(required = false) String category) {
        Long userId = SecurityUtil.getRequiredUserId();
        List<Knowledge> knowledgeList;
        if (category != null && !category.isEmpty()) {
            knowledgeList = knowledgeService.getByUserIdAndCategory(userId, category);
        } else {
            knowledgeList = knowledgeService.getByUserId(userId);
        }
        List<KnowledgeResponse> responseList = knowledgeList.stream()
                .map(KnowledgeResponse::fromEntity)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 创建知识条目
     *
     * @param knowledgeId 知识库ID
     * @param request     知识条目请求体
     * @return 创建成功的知识条目信息
     */
    @PostMapping("/{knowledgeId}/item")
    public Result<KnowledgeResponse.KnowledgeItemResponse> createItem(@PathVariable Long knowledgeId,
                                                                      @Valid @RequestBody KnowledgeItemRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到创建知识条目请求，知识库ID：{}，用户ID：{}", knowledgeId, userId);
        KnowledgeItem item = knowledgeService.createItem(knowledgeId, userId, request);
        return Result.success("创建成功", KnowledgeResponse.KnowledgeItemResponse.fromEntity(item));
    }

    /**
     * 更新知识条目
     *
     * @param itemId  知识条目ID
     * @param request 知识条目请求体
     * @return 更新成功的知识条目信息
     */
    @PutMapping("/item/{itemId}")
    public Result<KnowledgeResponse.KnowledgeItemResponse> updateItem(@PathVariable Long itemId,
                                                                      @Valid @RequestBody KnowledgeItemRequest request) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到更新知识条目请求，条目ID：{}，用户ID：{}", itemId, userId);
        KnowledgeItem item = knowledgeService.updateItem(itemId, userId, request);
        return Result.success("更新成功", KnowledgeResponse.KnowledgeItemResponse.fromEntity(item));
    }

    /**
     * 删除知识条目
     *
     * @param itemId 知识条目ID
     * @return 删除结果
     */
    @DeleteMapping("/item/{itemId}")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        Long userId = SecurityUtil.getRequiredUserId();
        log.info("收到删除知识条目请求，条目ID：{}，用户ID：{}", itemId, userId);
        knowledgeService.deleteItem(itemId, userId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取知识库下的所有知识条目
     *
     * @param knowledgeId 知识库ID
     * @return 知识条目列表
     */
    @GetMapping("/{knowledgeId}/items")
    public Result<List<KnowledgeResponse.KnowledgeItemResponse>> getItems(@PathVariable Long knowledgeId) {
        List<KnowledgeItem> items = knowledgeService.getItemsByKnowledgeId(knowledgeId);
        List<KnowledgeResponse.KnowledgeItemResponse> responseList = items.stream()
                .map(KnowledgeResponse.KnowledgeItemResponse::fromEntity)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }

    /**
     * 获取来源于某笔记的所有知识条目
     *
     * @param noteId 笔记ID
     * @return 知识条目列表
     */
    @GetMapping("/note/{noteId}/items")
    public Result<List<KnowledgeResponse.KnowledgeItemResponse>> getItemsByNoteId(@PathVariable Long noteId) {
        log.info("收到获取笔记关联知识条目请求，笔记ID：{}", noteId);
        List<KnowledgeItem> items = knowledgeService.getItemsByNoteId(noteId);
        List<KnowledgeResponse.KnowledgeItemResponse> responseList = items.stream()
                .map(KnowledgeResponse.KnowledgeItemResponse::fromEntity)
                .collect(Collectors.toList());
        return Result.success(responseList);
    }
}
