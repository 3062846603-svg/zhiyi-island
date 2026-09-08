package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.common.constant.MessageConstants;
import com.example.zhiyiislandbackend.config.MinioConfig;
import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.mapper.NoteImageMapper;
import com.example.zhiyiislandbackend.mapper.NoteMapper;
import com.example.zhiyiislandbackend.model.dto.note.request.NoteRequest;
import com.example.zhiyiislandbackend.model.dto.note.request.SummaryOptionRequest;
import com.example.zhiyiislandbackend.model.entity.Note;
import com.example.zhiyiislandbackend.model.entity.NoteImage;
import com.example.zhiyiislandbackend.model.enums.NoteCategoryEnum;
import com.example.zhiyiislandbackend.model.enums.NoteStatusEnum;
import com.example.zhiyiislandbackend.model.enums.NotificationTypeEnum;
import com.example.zhiyiislandbackend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final NoteImageMapper noteImageMapper;
    private final NotificationService notificationService;
    private final KnowledgeService knowledgeService;
    private final FileService fileService;
    private final MinioConfig minioConfig;
    private final AiService aiService;

    @Override
    @Transactional
    public Note create(Long userId, NoteRequest request) {
        log.info("创建笔记开始，用户ID：{}，标题：{}", userId, request.getTitle());

        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setCategory(request.getCategory() != null ? NoteCategoryEnum.fromCode(request.getCategory()) : NoteCategoryEnum.OTHER);
        note.setKnowledgeId(request.getKnowledgeId());
        note.setStatus(request.getStatus() != null ? NoteStatusEnum.fromCode(request.getStatus()) : NoteStatusEnum.PUBLISHED);
        note.setWordCount(calculateWordCount(request.getContent()));
        note.setSource("用户创建");
        note.setCreateTime(LocalDateTime.now());
        note.setUpdateTime(LocalDateTime.now());

        noteMapper.insert(note);
        log.info("创建笔记成功，笔记ID：{}", note.getId());

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            saveNoteImages(note.getId(), request.getImages());
        }

        boolean isDraft = note.getStatus() == NoteStatusEnum.DRAFT;

        if (!isDraft && request.getKnowledgeId() != null) {
            knowledgeService.updateNoteCount(request.getKnowledgeId());
        }

        if (isDraft) {
            notificationService.createNotification(
                    userId,
                    MessageConstants.Notification.DRAFT_CREATE_TITLE,
                    MessageConstants.Notification.DRAFT_CREATE_CONTENT_PREFIX + note.getTitle() + MessageConstants.Notification.DRAFT_CREATE_CONTENT_SUFFIX,
                    NotificationTypeEnum.NOTE
            );
        } else {
            notificationService.createNotification(
                    userId,
                    MessageConstants.Notification.NOTE_CREATE_TITLE,
                    MessageConstants.Notification.NOTE_CREATE_CONTENT_PREFIX + note.getTitle() + MessageConstants.Notification.NOTE_CREATE_CONTENT_SUFFIX,
                    NotificationTypeEnum.NOTE
            );

            knowledgeService.processNoteKnowledgeExtraction(note.getId(), userId);
        }

        return note;
    }

    @Override
    @Transactional
    public Note update(Long id, NoteRequest request) {
        log.info("更新笔记开始，笔记ID：{}", id);

        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(400, MessageConstants.Error.NOTE_NOT_FOUND);
        }

        Long oldKnowledgeId = note.getKnowledgeId();
        Long newKnowledgeId = request.getKnowledgeId();

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setCategory(NoteCategoryEnum.fromCode(request.getCategory()));
        note.setKnowledgeId(newKnowledgeId);
        if (request.getStatus() != null) {
            note.setStatus(NoteStatusEnum.fromCode(request.getStatus()));
        }
        note.setUpdateTime(LocalDateTime.now());

        noteMapper.updateById(note);

        if (request.getImages() != null) {
            List<NoteImage> oldImages = noteImageMapper.selectByNoteId(id);
            noteImageMapper.deleteByNoteId(id);
            for (NoteImage oldImage : oldImages) {
                deleteImageFile(oldImage.getImageUrl());
            }
            if (!request.getImages().isEmpty()) {
                saveNoteImages(id, request.getImages());
            }
        }

        log.info("更新笔记成功，笔记ID：{}", id);

        if (oldKnowledgeId != null && !oldKnowledgeId.equals(newKnowledgeId)) {
            knowledgeService.updateNoteCount(oldKnowledgeId);
        }
        if (newKnowledgeId != null && !newKnowledgeId.equals(oldKnowledgeId)) {
            knowledgeService.updateNoteCount(newKnowledgeId);
        }

        return note;
    }

    private void saveNoteImages(Long noteId, List<String> imageUrls) {
        List<NoteImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            NoteImage image = new NoteImage();
            image.setNoteId(noteId);
            image.setImageUrl(imageUrls.get(i));
            image.setCreateTime(LocalDateTime.now());
            images.add(image);
        }
        noteImageMapper.insertBatch(images);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        log.info("删除笔记开始，笔记ID：{}，用户ID：{}", id, userId);

        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(400, MessageConstants.Error.NOTE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(403, MessageConstants.Error.NO_PERMISSION_DELETE);
        }

        List<NoteImage> images = noteImageMapper.selectByNoteId(id);
        noteImageMapper.deleteByNoteId(id);
        log.info("删除笔记图片记录成功，笔记ID：{}，图片数量：{}", id, images.size());

        for (NoteImage image : images) {
            deleteImageFile(image.getImageUrl());
        }

        Long knowledgeId = note.getKnowledgeId();
        noteMapper.deleteById(id);
        log.info("删除笔记成功，笔记ID：{}", id);

        if (knowledgeId != null) {
            knowledgeService.updateNoteCount(knowledgeId);
        }
    }

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        try {
            String endpoint = minioConfig.getPublicUrl();
            String bucketName = minioConfig.getBucketName();
            String prefix = endpoint + "/" + bucketName + "/";
            if (imageUrl.startsWith(prefix)) {
                String objectName = imageUrl.substring(prefix.length());
                fileService.deleteFile(bucketName, objectName);
                log.info("删除图片文件成功：{}", objectName);
            }
        } catch (Exception e) {
            log.warn("删除图片文件失败：{}，错误：{}", imageUrl, e.getMessage());
        }
    }

    @Override
    public Note getById(Long id) {
        log.debug("查询笔记，笔记ID：{}", id);
        return noteMapper.selectById(id);
    }

    @Override
    public List<Note> getByUserId(Long userId) {
        log.debug("查询用户笔记列表，用户ID：{}", userId);
        return noteMapper.selectByUserId(userId);
    }

    @Override
    public List<Note> getByUserIdAndCategory(Long userId, String category) {
        log.debug("查询用户分类笔记列表，用户ID：{}，分类：{}", userId, category);
        return noteMapper.selectByUserIdAndCategory(userId, category);
    }

    @Override
    public List<Note> getByKnowledgeId(Long knowledgeId) {
        log.debug("查询知识库笔记列表，知识库ID：{}", knowledgeId);
        return noteMapper.selectByKnowledgeId(knowledgeId);
    }

    @Override
    public List<Note> search(Long userId, String keyword) {
        log.info("搜索笔记，用户ID：{}，关键词：{}", userId, keyword);
        return noteMapper.searchByKeyword(userId, keyword);
    }

    @Override
    @Transactional
    public void updateKnowledgeId(Long id, Long knowledgeId, Long userId) {
        log.info("更新笔记所属知识库，笔记ID：{}，知识库ID：{}，用户ID：{}", id, knowledgeId, userId);

        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(400, MessageConstants.Error.NOTE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(403, MessageConstants.Error.NO_PERMISSION_OPERATION);
        }

        Long oldKnowledgeId = note.getKnowledgeId();
        noteMapper.updateKnowledgeId(id, knowledgeId);

        if (oldKnowledgeId != null && !oldKnowledgeId.equals(knowledgeId)) {
            knowledgeService.updateNoteCount(oldKnowledgeId);
        }
        if (knowledgeId != null && !knowledgeId.equals(oldKnowledgeId)) {
            knowledgeService.updateNoteCount(knowledgeId);
        }

        log.info("更新笔记所属知识库成功，笔记ID：{}", id);
    }

    @Override
    public int countByUserId(Long userId) {
        return noteMapper.countByUserId(userId);
    }

    @Override
    public int countByKnowledgeId(Long knowledgeId) {
        return noteMapper.countByKnowledgeId(knowledgeId);
    }

    @Override
    @Transactional
    public Note generateSummary(Long id, Long userId) {
        return generateSummary(id, userId, new SummaryOptionRequest());
    }

    @Override
    @Transactional
    public Note generateSummary(Long id, Long userId, SummaryOptionRequest options) {
        log.info("生成AI摘要开始，笔记ID：{}，用户ID：{}，风格：{}，长度：{}", id, userId, 
                options != null ? options.getStyle() : "keypoints", 
                options != null ? options.getLength() : "medium");

        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(400, MessageConstants.Error.NOTE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(403, MessageConstants.Error.NO_PERMISSION_OPERATION);
        }

        String content = note.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(400, MessageConstants.Error.NOTE_CONTENT_EMPTY);
        }

        String style = options != null ? options.getStyle() : "keypoints";
        String length = options != null ? options.getLength() : "medium";
        
        aiService.generateSummaryForNote(id, userId, note.getTitle(), content, style, length);

        log.info("生成AI摘要成功，笔记ID：{}", id);
        return note;
    }

    @Override
    public List<Note> getDrafts(Long userId) {
        log.debug("查询用户草稿列表，用户ID：{}", userId);
        return noteMapper.selectDraftsByUserId(userId);
    }

    @Override
    public int countDrafts(Long userId) {
        return noteMapper.countDraftsByUserId(userId);
    }

    @Override
    @Transactional
    public void publishDraft(Long id, Long userId) {
        log.info("发布草稿，笔记ID：{}，用户ID：{}", id, userId);

        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(400, MessageConstants.Error.NOTE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(403, MessageConstants.Error.NO_PERMISSION_OPERATION);
        }

        noteMapper.updateStatus(id, 1);
        log.info("发布草稿成功，笔记ID：{}", id);

        notificationService.createNotification(
                userId,
                MessageConstants.Notification.NOTE_CREATE_TITLE,
                MessageConstants.Notification.NOTE_CREATE_CONTENT_PREFIX + note.getTitle() + MessageConstants.Notification.NOTE_CREATE_CONTENT_SUFFIX,
                NotificationTypeEnum.NOTE
        );

        knowledgeService.processNoteKnowledgeExtraction(id, userId);
    }

    /**
     * 计算内容的字数
     * 统计中文字符数和非中文单词数
     *
     * @param content 内容字符串
     * @return 字数
     */
    private int calculateWordCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        int count = 0;
        boolean inWord = false;
        for (char c : content.toCharArray()) {
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                count++;
            } else if (Character.isLetterOrDigit(c)) {
                if (!inWord) {
                    count++;
                    inWord = true;
                }
            } else {
                inWord = false;
            }
        }
        return count;
    }
}
