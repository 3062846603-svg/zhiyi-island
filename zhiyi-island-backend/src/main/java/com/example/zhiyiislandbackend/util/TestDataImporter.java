package com.example.zhiyiislandbackend.util;

import com.example.zhiyiislandbackend.model.dto.note.request.NoteRequest;
import com.example.zhiyiislandbackend.model.entity.Note;
import com.example.zhiyiislandbackend.service.NoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class TestDataImporter implements CommandLineRunner {

    private final NoteService noteService;
    private final ObjectMapper objectMapper;

    @Value("${app.test-data.import-enabled:false}")
    private boolean importEnabled;

    private static final Long TEST_USER_ID = 1L;
    private static final String DATA_FILE = "test-notes-data.json";

    public TestDataImporter(NoteService noteService, ObjectMapper objectMapper) {
        this.noteService = noteService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (importEnabled) {
            importTestData();
        }
    }

    private void importTestData() throws IOException {
        ClassPathResource resource = new ClassPathResource(DATA_FILE);
        List<TestNoteData> notesData = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<TestNoteData>>() {
                }
        );

        Collections.shuffle(notesData);

        System.out.println("开始导入测试数据，共 " + notesData.size() + " 条笔记（已打乱顺序）");

        int successCount = 0;
        for (TestNoteData data : notesData) {
            try {
                NoteRequest request = convertToRequest(data);
                Note note = noteService.create(TEST_USER_ID, request);
                successCount++;
                if (successCount % 20 == 0) {
                    System.out.println("已导入 " + successCount + " 条笔记");
                }
            } catch (Exception e) {
                System.err.println("导入笔记失败: " + data.getTitle() + ", 错误: " + e.getMessage());
            }
        }

        System.out.println("测试数据导入完成，成功导入 " + successCount + " 条笔记");
    }

    private NoteRequest convertToRequest(TestNoteData data) {
        NoteRequest request = new NoteRequest();
        request.setTitle(data.getTitle());
        request.setContent(data.getContent());
        request.setCategory(data.getCategory());
        request.setStatus(1);
        return request;
    }

    @Setter
    @Getter
    public static class TestNoteData {
        private String title;
        private String content;
        private Integer category;

    }
}
