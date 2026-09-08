package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.model.dto.knowledge.request.KnowledgeItemRequest;
import com.example.zhiyiislandbackend.model.dto.knowledge.request.KnowledgeRequest;
import com.example.zhiyiislandbackend.model.dto.ai.response.KnowledgeExtractionResponse;
import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.model.entity.KnowledgeItem;
import com.example.zhiyiislandbackend.model.entity.Note;
import com.example.zhiyiislandbackend.model.enums.KnowledgeCategoryEnum;
import com.example.zhiyiislandbackend.model.enums.NotificationTypeEnum;
import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.mapper.KnowledgeItemMapper;
import com.example.zhiyiislandbackend.mapper.KnowledgeMapper;
import com.example.zhiyiislandbackend.mapper.NoteMapper;
import com.example.zhiyiislandbackend.service.AiService;
import com.example.zhiyiislandbackend.service.KnowledgeService;
import com.example.zhiyiislandbackend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库服务实现类
 * 实现知识库和知识条目相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeMapper knowledgeMapper;
    private final KnowledgeItemMapper knowledgeItemMapper;
    private final NoteMapper noteMapper;
    private final NotificationService notificationService;
    private final AiService aiService;

    @Value("${app.ai.auto-extract-knowledge:true}")
    private boolean autoExtractEnabled;

    /**
     * 创建知识库
     * 初始化知识库信息并保存到数据库
     */
    @Override
    public Knowledge create(Long userId, KnowledgeRequest request) {
        log.info("创建知识库开始，用户ID：{}，标题：{}", userId, request.getTitle());

        Knowledge knowledge = new Knowledge();
        knowledge.setUserId(userId);
        knowledge.setTitle(request.getTitle());
        knowledge.setDescription(request.getDescription() != null ? request.getDescription() : "");
        knowledge.setCategory(request.getCategory() != null ? KnowledgeCategoryEnum.fromCode(request.getCategory()) : KnowledgeCategoryEnum.OTHER);
        knowledge.setItemCount(0);
        knowledge.setNoteCount(0);
        knowledge.setCreateTime(LocalDateTime.now());
        knowledge.setUpdateTime(LocalDateTime.now());

        knowledgeMapper.insert(knowledge);
        log.info("创建知识库成功，知识库ID：{}", knowledge.getId());

        notificationService.createNotification(
                userId,
                "知识库创建成功",
                "您的新知识库《" + knowledge.getTitle() + "》已成功创建。",
                NotificationTypeEnum.KNOWLEDGE
        );

        return knowledge;
    }

    /**
     * 更新知识库
     * 更新知识库的标题、描述和分类
     */
    @Override
    public Knowledge update(Long id, KnowledgeRequest request) {
        log.info("更新知识库开始，知识库ID：{}", id);

        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new BusinessException(400, "知识库不存在");
        }

        knowledge.setTitle(request.getTitle());
        knowledge.setDescription(request.getDescription() != null ? request.getDescription() : "");
        knowledge.setCategory(request.getCategory() != null ? KnowledgeCategoryEnum.fromCode(request.getCategory()) : KnowledgeCategoryEnum.OTHER);
        knowledge.setUpdateTime(LocalDateTime.now());

        knowledgeMapper.updateById(knowledge);
        log.info("更新知识库成功，知识库ID：{}", id);
        return knowledge;
    }

    /**
     * 删除知识库
     * 验证用户权限后删除知识库
     */
    @Override
    public void delete(Long id, Long userId) {
        log.info("删除知识库开始，知识库ID：{}，用户ID：{}", id, userId);

        Knowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new BusinessException(400, "知识库不存在");
        }
        if (!knowledge.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此知识库");
        }

        knowledgeMapper.deleteById(id);
        log.info("删除知识库成功，知识库ID：{}", id);
    }

    /**
     * 根据ID获取知识库
     */
    @Override
    public Knowledge getById(Long id) {
        log.debug("查询知识库，知识库ID：{}", id);
        return knowledgeMapper.selectById(id);
    }

    /**
     * 获取用户的所有知识库
     */
    @Override
    public List<Knowledge> getByUserId(Long userId) {
        log.debug("查询用户知识库列表，用户ID：{}", userId);
        return knowledgeMapper.selectByUserId(userId);
    }

    /**
     * 获取用户指定分类的知识库
     */
    @Override
    public List<Knowledge> getByUserIdAndCategory(Long userId, String category) {
        log.debug("查询用户分类知识库列表，用户ID：{}，分类：{}", userId, category);
        return knowledgeMapper.selectByUserIdAndCategory(userId, category);
    }

    /**
     * 创建知识条目
     * 验证知识库权限后创建条目，并更新知识库的条目计数
     */
    @Override
    @Transactional
    public KnowledgeItem createItem(Long knowledgeId, Long userId, KnowledgeItemRequest request) {
        log.info("创建知识条目开始，知识库ID：{}，用户ID：{}", knowledgeId, userId);

        Knowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge == null) {
            throw new BusinessException(400, "知识库不存在");
        }
        if (!knowledge.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权在此知识库创建条目");
        }

        if (request.getNoteId() != null) {
            checkCircularReference(knowledgeId, request.getNoteId());
        }

        KnowledgeItem item = new KnowledgeItem();
        item.setKnowledgeId(knowledgeId);
        item.setTitle(request.getTitle());
        item.setContent(request.getContent() != null ? request.getContent() : "");
        item.setSource(request.getSource() != null ? request.getSource() : "用户创建");
        item.setNoteId(request.getNoteId());
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());

        knowledgeItemMapper.insert(item);

        int count = knowledgeItemMapper.countByKnowledgeId(knowledgeId);
        knowledgeMapper.updateItemCount(knowledgeId, count);

        log.info("创建知识条目成功，条目ID：{}", item.getId());

        notificationService.createNotification(
                userId,
                "知识条目添加成功",
                "您在《" + knowledge.getTitle() + "》中添加了新条目《" + item.getTitle() + "》。",
                NotificationTypeEnum.KNOWLEDGE
        );

        return item;
    }

    /**
     * 更新知识条目
     * 验证权限后更新条目的标题、内容和来源
     */
    @Override
    public KnowledgeItem updateItem(Long itemId, Long userId, KnowledgeItemRequest request) {
        log.info("更新知识条目开始，条目ID：{}", itemId);

        KnowledgeItem item = knowledgeItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(400, "知识条目不存在");
        }

        Knowledge knowledge = knowledgeMapper.selectById(item.getKnowledgeId());
        if (knowledge == null || !knowledge.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权更新此知识条目");
        }

        if (request.getNoteId() != null) {
            checkCircularReference(item.getKnowledgeId(), request.getNoteId());
        }

        item.setTitle(request.getTitle());
        item.setContent(request.getContent() != null ? request.getContent() : "");
        item.setSource(request.getSource() != null ? request.getSource() : "用户创建");
        item.setNoteId(request.getNoteId());
        item.setUpdateTime(LocalDateTime.now());

        knowledgeItemMapper.updateById(item);
        log.info("更新知识条目成功，条目ID：{}", itemId);
        return item;
    }

    /**
     * 删除知识条目
     * 验证权限后删除条目，并更新知识库的条目计数
     */
    @Override
    @Transactional
    public void deleteItem(Long itemId, Long userId) {
        log.info("删除知识条目开始，条目ID：{}，用户ID：{}", itemId, userId);

        KnowledgeItem item = knowledgeItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(400, "知识条目不存在");
        }

        Knowledge knowledge = knowledgeMapper.selectById(item.getKnowledgeId());
        if (knowledge == null || !knowledge.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此知识条目");
        }

        Long knowledgeId = item.getKnowledgeId();
        knowledgeItemMapper.deleteById(itemId);

        int count = knowledgeItemMapper.countByKnowledgeId(knowledgeId);
        knowledgeMapper.updateItemCount(knowledgeId, count);

        log.info("删除知识条目成功，条目ID：{}", itemId);
    }

    /**
     * 获取知识库下的所有知识条目
     */
    @Override
    public List<KnowledgeItem> getItemsByKnowledgeId(Long knowledgeId) {
        log.debug("查询知识库条目列表，知识库ID：{}", knowledgeId);
        return knowledgeItemMapper.selectByKnowledgeId(knowledgeId);
    }

    /**
     * 获取来源于某笔记的所有知识条目
     */
    @Override
    public List<KnowledgeItem> getItemsByNoteId(Long noteId) {
        log.debug("查询笔记来源的知识条目列表，笔记ID：{}", noteId);
        return knowledgeItemMapper.selectByNoteId(noteId);
    }

    /**
     * 更新知识库笔记数量
     */
    @Override
    public void updateNoteCount(Long knowledgeId) {
        log.debug("更新知识库笔记数量，知识库ID：{}", knowledgeId);
        int count = noteMapper.countByKnowledgeId(knowledgeId);
        knowledgeMapper.updateNoteCount(knowledgeId, count);
    }

    /**
     * 统计用户知识库数量
     */
    @Override
    public int countByUserId(Long userId) {
        return knowledgeMapper.countByUserId(userId);
    }

    /**
     * 统计用户知识条目数量
     */
    @Override
    public int countItemsByUserId(Long userId) {
        return knowledgeItemMapper.countByUserId(userId);
    }

    /**
     * 检查循环引用
     * 防止出现：KnowledgeItem.noteId -> Note -> Knowledge -> KnowledgeItem 的循环
     *
     * @param knowledgeId 知识库ID
     * @param noteId      笔记ID
     */
    private void checkCircularReference(Long knowledgeId, Long noteId) {
        var note = noteMapper.selectById(noteId);
        if (note == null) {
            return;
        }

        if (note.getKnowledgeId() != null && note.getKnowledgeId().equals(knowledgeId)) {
            throw new BusinessException(400, "检测到循环引用：该笔记已属于当前知识库，无法创建关联的知识条目");
        }
    }

    @Override
    @Async
    public void processNoteKnowledgeExtraction(Long noteId, Long userId) {
        if (!autoExtractEnabled) {
            log.debug("自动知识提取功能已禁用");
            return;
        }

        log.info("开始异步处理笔记知识提取，笔记ID：{}，用户ID：{}", noteId, userId);

        Note note = null;
        try {
            note = noteMapper.selectById(noteId);
        } catch (Exception e) {
            log.error("查询笔记失败，笔记ID：{}，错误：{}", noteId, e.getMessage());
            return;
        }

        if (note == null) {
            log.warn("笔记不存在，笔记ID：{}", noteId);
            return;
        }

        try {
            processKnowledgeExtractionInternal(noteId, userId, note);
        } catch (Exception e) {
            log.error("笔记知识提取失败，笔记ID：{}，错误：{}", noteId, e.getMessage(), e);
            notificationService.createNotification(
                    userId,
                    "知识提取失败",
                    "笔记《" + note.getTitle() + "》知识提取失败：" + e.getMessage(),
                    NotificationTypeEnum.AI
            );
        }
    }

    @Transactional
    private void processKnowledgeExtractionInternal(Long noteId, Long userId, Note note) {
        if (note.getContent() == null || note.getContent().isBlank()) {
            log.info("笔记内容为空，跳过知识提取，笔记ID：{}", noteId);
            return;
        }

        if (note.getContent().length() < 100) {
            log.info("笔记内容过短（{}字），跳过知识提取，笔记ID：{}", note.getContent().length(), noteId);
            return;
        }

        KnowledgeExtractionResponse extraction = aiService.extractKnowledge(
                note.getTitle(),
                note.getContent()
        );

        log.info("AI返回的分类: {}", extraction.getKnowledgeCategory());

        if (extraction.getKnowledgePoints() == null || extraction.getKnowledgePoints().isEmpty()) {
            log.info("未提取到知识点，笔记ID：{}", noteId);
            notificationService.createNotification(
                    userId,
                    "知识提取完成",
                    "笔记《" + note.getTitle() + "》未提取到合适的知识点",
                    NotificationTypeEnum.AI
            );
            return;
        }

        Knowledge knowledge;
        if (note.getKnowledgeId() != null) {
            knowledge = knowledgeMapper.selectById(note.getKnowledgeId());
            if (knowledge == null) {
                log.warn("笔记关联的知识库不存在，笔记ID：{}，知识库ID：{}", noteId, note.getKnowledgeId());
                knowledge = findOrCreateKnowledge(userId, extraction, note);
            } else {
                log.info("复用笔记现有知识库，知识库ID：{}，标题：{}", knowledge.getId(), knowledge.getTitle());
            }
        } else {
            knowledge = findOrCreateKnowledge(userId, extraction, note);
        }

        createKnowledgeItems(knowledge.getId(), noteId, extraction.getKnowledgePoints());

        if (note.getKnowledgeId() == null) {
            note.setKnowledgeId(knowledge.getId());
            noteMapper.updateById(note);
        }

        int noteCount = noteMapper.countByKnowledgeId(knowledge.getId());
        knowledgeMapper.updateNoteCount(knowledge.getId(), noteCount);

        try {
            String embeddingText = note.getTitle() + "\n" + note.getContent();
            aiService.generateAndSaveEmbedding(noteId, embeddingText);
        } catch (Exception e) {
            log.warn("生成embedding失败（不影响知识提取），笔记ID：{}，错误：{}", noteId, e.getMessage());
        }

        notificationService.createNotification(
                userId,
                "知识提取完成",
                String.format("笔记《%s》已提取%d个知识点到知识库《%s》",
                        note.getTitle(),
                        extraction.getKnowledgePoints().size(),
                        knowledge.getTitle()),
                NotificationTypeEnum.AI
        );

        log.info("笔记知识提取完成，笔记ID：{}，知识库ID：{}，知识点数量：{}",
                noteId, knowledge.getId(), extraction.getKnowledgePoints().size());
    }

    private Knowledge findOrCreateKnowledge(Long userId, KnowledgeExtractionResponse extraction, Note note) {
        List<Knowledge> existingKnowledge = knowledgeMapper.selectByUserId(userId);

        String targetTitle = extraction.getKnowledgeTitle();
        for (Knowledge k : existingKnowledge) {
            if (k.getTitle().equals(targetTitle)) {
                log.info("复用现有知识库，知识库ID：{}，标题：{}", k.getId(), k.getTitle());
                return k;
            }
        }

        Knowledge knowledge = new Knowledge();
        knowledge.setUserId(userId);
        knowledge.setTitle(targetTitle);
        knowledge.setDescription("由AI自动创建，来源于笔记《" + note.getTitle() + "》");
        knowledge.setCategory(KnowledgeCategoryEnum.fromDescription(extraction.getKnowledgeCategory()));
        knowledge.setItemCount(0);
        knowledge.setNoteCount(0);
        knowledge.setCreateTime(LocalDateTime.now());
        knowledge.setUpdateTime(LocalDateTime.now());

        knowledgeMapper.insert(knowledge);
        log.info("创建新知识库，知识库ID：{}，标题：{}", knowledge.getId(), knowledge.getTitle());

        return knowledge;
    }

    private void createKnowledgeItems(Long knowledgeId, Long noteId, List<KnowledgeExtractionResponse.KnowledgePoint> points) {
        for (KnowledgeExtractionResponse.KnowledgePoint point : points) {
            KnowledgeItem item = new KnowledgeItem();
            item.setKnowledgeId(knowledgeId);
            item.setNoteId(noteId);
            item.setTitle(point.getTitle());
            item.setContent(point.getContent());
            item.setSource(point.getSource() != null ? point.getSource() : "AI自动提取");
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());

            knowledgeItemMapper.insert(item);
        }

        int itemCount = knowledgeItemMapper.countByKnowledgeId(knowledgeId);
        knowledgeMapper.updateItemCount(knowledgeId, itemCount);

        log.info("创建知识条目完成，知识库ID：{}，条目数量：{}", knowledgeId, points.size());
    }
}
