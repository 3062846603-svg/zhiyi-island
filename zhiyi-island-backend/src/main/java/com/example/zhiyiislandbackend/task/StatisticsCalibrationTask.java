package com.example.zhiyiislandbackend.task;

import com.example.zhiyiislandbackend.model.entity.Knowledge;
import com.example.zhiyiislandbackend.mapper.KnowledgeItemMapper;
import com.example.zhiyiislandbackend.mapper.KnowledgeMapper;
import com.example.zhiyiislandbackend.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据统计校准定时任务
 * 定期校准知识库的笔记数量和知识条目数量统计
 * 防止因直接操作数据库导致的统计不准确
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsCalibrationTask {

    private final KnowledgeMapper knowledgeMapper;
    private final NoteMapper noteMapper;
    private final KnowledgeItemMapper knowledgeItemMapper;

    /**
     * 校准知识库统计
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void calibrateKnowledgeStatistics() {
        log.info("开始执行知识库统计校准任务");

        try {
            calibrateNoteCount();
            calibrateItemCount();

            log.info("知识库统计校准任务执行完成");
        } catch (Exception e) {
            log.error("知识库统计校准任务执行失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 校准知识库的笔记数量统计
     */
    private void calibrateNoteCount() {
        log.info("开始校准知识库笔记数量统计");

        int correctedCount = 0;

        List<Knowledge> knowledgeList = knowledgeMapper.selectAll();

        for (Knowledge knowledge : knowledgeList) {
            int actualCount = noteMapper.countByKnowledgeId(knowledge.getId());
            if (actualCount != knowledge.getNoteCount()) {
                knowledgeMapper.updateNoteCount(knowledge.getId(), actualCount);
                correctedCount++;
                log.debug("校准知识库笔记数量，知识库ID：{}，原数量：{}，实际数量：{}",
                        knowledge.getId(), knowledge.getNoteCount(), actualCount);
            }
        }

        log.info("知识库笔记数量校准完成，共校准{}个知识库", correctedCount);
    }

    /**
     * 校准知识库的知识条目数量统计
     */
    private void calibrateItemCount() {
        log.info("开始校准知识库知识条目数量统计");

        int correctedCount = 0;

        List<Knowledge> knowledgeList = knowledgeMapper.selectAll();

        for (Knowledge knowledge : knowledgeList) {
            int actualCount = knowledgeItemMapper.countByKnowledgeId(knowledge.getId());
            if (actualCount != knowledge.getItemCount()) {
                knowledgeMapper.updateItemCount(knowledge.getId(), actualCount);
                correctedCount++;
                log.debug("校准知识库知识条目数量，知识库ID：{}，原数量：{}，实际数量：{}",
                        knowledge.getId(), knowledge.getItemCount(), actualCount);
            }
        }

        log.info("知识库知识条目数量校准完成，共校准{}个知识库", correctedCount);
    }
}
