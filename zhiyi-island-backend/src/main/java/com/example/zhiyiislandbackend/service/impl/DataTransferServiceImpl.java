package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.exception.BusinessException;
import com.example.zhiyiislandbackend.mapper.*;
import com.example.zhiyiislandbackend.model.dto.export.request.ExportRequest;
import com.example.zhiyiislandbackend.model.dto.export.response.ExportDTO;
import com.example.zhiyiislandbackend.model.dto.export.response.ExportDataResponse;
import com.example.zhiyiislandbackend.model.dto.export.response.ExportRecordResponse;
import com.example.zhiyiislandbackend.model.dto.imports.request.ImportRequest;
import com.example.zhiyiislandbackend.model.dto.imports.response.ImportRecordResponse;
import com.example.zhiyiislandbackend.model.entity.*;
import com.example.zhiyiislandbackend.model.enums.*;
import com.example.zhiyiislandbackend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.pdf.BaseFont;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataTransferServiceImpl implements DataTransferService {

    private final ImportRecordMapper importRecordMapper;
    private final ExportRecordMapper exportRecordMapper;
    private final NoteMapper noteMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final KnowledgeItemMapper knowledgeItemMapper;
    private final AiSummaryMapper aiSummaryMapper;
    private final NotificationService notificationService;
    private final AiService aiService;
    private final AiProviderService aiProviderService;
    private final KnowledgeService knowledgeService;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;

    @Value("${spring.ai.siliconflow.knowledge-model:Pro/zai-org/GLM-5}")
    private String knowledgeModel;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 导入文件
     * 核心导入流程：
     * 1. 校验文件（大小、类型）
     * 2. 创建导入记录
     * 3. 提取文件文本内容
     * 4. AI智能解析（优先）或基础解析（回退）
     * 5. 自动创建知识库并关联笔记
     * 6. 可选：为笔记生成AI摘要
     * 7. 更新导入记录状态并发送通知
     *
     * @param userId  用户ID
     * @param file    上传的文件
     * @param request 导入请求选项
     * @return 导入记录响应
     */
    @Override
    @Transactional
    public ImportRecordResponse importFile(Long userId, MultipartFile file, ImportRequest request) {
        log.info("导入文件，用户ID：{}，文件名：{}", userId, file.getOriginalFilename());

        if (file.isEmpty()) {
            throw new BusinessException(400, "文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小不能超过10MB");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException(400, "文件名不能为空");
        }
        String fileType = getFileExtension(fileName);
        if (fileType.isEmpty()) {
            throw new BusinessException(400, "无法识别文件类型");
        }

        ImportRecord record = new ImportRecord();
        record.setUserId(userId);
        record.setFileName(fileName);
        record.setFileType(fileType);
        record.setFileSize(file.getSize());
        record.setStatus(ImportStatusEnum.PROCESSING);
        record.setNoteCount(0);
        record.setKnowledgeCount(0);
        record.setCreateTime(LocalDateTime.now());

        importRecordMapper.insert(record);

        try {
            String content = extractContent(file, fileType);

            List<Note> notes;
            Long knowledgeId = null;
            int knowledgeCount = 0;

            if (aiProviderService.isServiceAvailable()) {
                log.info("AI服务可用，使用AI解析文件内容");
                AiParsedResult aiResult = parseContentWithAi(content, fileName);
                notes = aiResult.getNotes();

                if (!notes.isEmpty()) {
                    Knowledge knowledge = findOrCreateKnowledgeForImport(
                            userId, aiResult.getSuggestedKnowledgeTitle(), aiResult.getSuggestedKnowledgeCategory(), fileName);
                    knowledgeId = knowledge.getId();
                    knowledgeCount = 1;
                    for (Note note : notes) {
                        note.setKnowledgeId(knowledgeId);
                    }
                    log.info("知识库标题：{}，分类：{}", knowledge.getTitle(), knowledge.getCategory());
                }
            } else {
                log.info("AI服务不可用，使用基础解析");
                Knowledge knowledge = findOrCreateKnowledgeForImport(userId, null, null, fileName);
                knowledgeId = knowledge.getId();
                knowledgeCount = 1;
                notes = parseToNotesBasic(userId, content, fileName, knowledgeId);
            }

            for (Note note : notes) {
                note.setUserId(userId);
                noteMapper.insert(note);
            }

            int noteCount = notes.size();
            List<Long> noteIds = notes.stream().map(Note::getId).toList();

            if (knowledgeId != null && noteCount > 0) {
                knowledgeService.updateNoteCount(knowledgeId);
            }

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (Long noteId : noteIds) {
                        knowledgeService.processNoteKnowledgeExtraction(noteId, userId);
                    }

                    if (Boolean.TRUE.equals(request.getAiSummary()) && noteCount > 0) {
                        generateSummariesForNotes(userId, notes);
                    }
                }
            });

            record.setStatus(ImportStatusEnum.SUCCESS);
            record.setNoteCount(noteCount);
            record.setKnowledgeCount(knowledgeCount);
            importRecordMapper.updateStatus(record.getId(), record.getStatus().getCode(),
                    record.getNoteCount(), record.getKnowledgeCount(), null);

            log.info("文件导入成功，笔记数：{}，知识库数：{}，AI摘要：{}", noteCount, knowledgeCount,
                    Boolean.TRUE.equals(request.getAiSummary()) ? "已生成" : "未生成");

            notificationService.createNotification(
                    userId,
                    "文件导入成功",
                    "文件《" + fileName + "》导入完成，共生成" + noteCount + "条笔记" +
                            (knowledgeCount > 0 ? "，已自动归类到知识库" : "") +
                            (Boolean.TRUE.equals(request.getAiSummary()) ? "，已生成AI摘要" : "") + "。",
                    NotificationTypeEnum.SYSTEM
            );

        } catch (Exception e) {
            log.error("文件导入失败：{}", e.getMessage(), e);
            String errorMsg = translateErrorMessage(e.getMessage());
            record.setStatus(ImportStatusEnum.FAILED);
            record.setErrorMessage(errorMsg);
            importRecordMapper.updateStatus(record.getId(), record.getStatus().getCode(),
                    0, 0, errorMsg);

            notificationService.createNotification(
                    userId,
                    "文件导入失败",
                    "文件《" + fileName + "》导入失败，原因：" + errorMsg,
                    NotificationTypeEnum.SYSTEM
            );
        }

        return convertToImportResponse(record);
    }

    /**
     * 获取导入历史
     * 查询指定用户的所有导入记录，按时间倒序排列
     *
     * @param userId 用户ID
     * @return 导入记录响应列表
     */
    @Override
    public List<ImportRecordResponse> getImportHistory(Long userId) {
        log.info("获取导入历史，用户ID：{}", userId);
        List<ImportRecord> records = importRecordMapper.selectByUserId(userId);
        return records.stream()
                .map(this::convertToImportResponse)
                .collect(Collectors.toList());
    }

    /**
     * 删除导入记录
     * 仅删除记录本身，不影响已导入的笔记和知识库数据
     *
     * @param userId   用户ID（用于权限校验）
     * @param recordId 导入记录ID
     */
    @Override
    public void deleteImportRecord(Long userId, Long recordId) {
        log.info("删除导入记录，用户ID：{}，记录ID：{}", userId, recordId);
        ImportRecord record = importRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(404, "记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此记录");
        }
        importRecordMapper.deleteById(recordId);
    }

    /**
     * 导出数据
     * 根据用户选择导出笔记和/或知识库数据，支持JSON和PDF格式
     *
     * @param userId  用户ID
     * @param request 导出请求，包含格式和导出范围选项
     * @return 导出数据响应，包含文件内容或下载信息
     */
    @Override
    @Transactional
    public ExportDataResponse exportData(Long userId, ExportRequest request) {
        log.info("导出数据，用户ID：{}，格式：{}", userId, request.getFormat());

        Map<String, Object> exportData = new HashMap<>();
        StringBuilder exportTypes = new StringBuilder();

        if (Boolean.TRUE.equals(request.getNotes())) {
            List<Note> notes = noteMapper.selectByUserId(userId);
            List<ExportDTO.NoteExport> noteExports = notes.stream()
                    .map(ExportDTO.NoteExport::fromEntity)
                    .collect(Collectors.toList());
            exportData.put("notes", noteExports);
            exportTypes.append("笔记,");
        }

        if (Boolean.TRUE.equals(request.getKnowledge())) {
            List<Knowledge> knowledgeList = knowledgeMapper.selectByUserId(userId);
            List<Map<String, Object>> knowledgeWithItems = new ArrayList<>();
            for (Knowledge knowledge : knowledgeList) {
                Map<String, Object> kw = new HashMap<>();
                kw.put("knowledge", ExportDTO.KnowledgeExport.fromEntity(knowledge));
                List<KnowledgeItem> items = knowledgeItemMapper.selectByKnowledgeId(knowledge.getId());
                List<ExportDTO.KnowledgeItemExport> itemExports = items.stream()
                        .map(ExportDTO.KnowledgeItemExport::fromEntity)
                        .collect(Collectors.toList());
                kw.put("items", itemExports);
                knowledgeWithItems.add(kw);
            }
            exportData.put("knowledge", knowledgeWithItems);
            exportTypes.append("知识库,");
        }

        if (Boolean.TRUE.equals(request.getAiSummaries())) {
            List<AiSummary> summaries = aiSummaryMapper.selectByUserId(userId, 100);
            List<ExportDTO.AiSummaryExport> summaryExports = summaries.stream()
                    .map(ExportDTO.AiSummaryExport::fromEntity)
                    .collect(Collectors.toList());
            exportData.put("aiSummaries", summaryExports);
            exportTypes.append("AI摘要,");
        }

        String content = null;
        byte[] binaryContent = null;
        String fileName;
        String contentType;
        boolean isBinary = false;

        if ("pdf".equalsIgnoreCase(request.getFormat())) {
            binaryContent = convertToPdf(exportData);
            fileName = "zhiyi-island-export-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf";
            contentType = "application/pdf";
            isBinary = true;
        } else if ("markdown".equalsIgnoreCase(request.getFormat())) {
            content = convertToMarkdown(exportData);
            fileName = "zhiyi-island-export-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".md";
            contentType = "text/markdown";
        } else if ("json".equalsIgnoreCase(request.getFormat()) || request.getFormat() == null) {
            try {
                content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
            } catch (Exception e) {
                log.error("JSON序列化失败：{}", e.getMessage());
                content = "{}";
            }
            fileName = "zhiyi-island-export-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".json";
            contentType = "application/json";
        } else {
            throw new BusinessException(400, "不支持的导出格式：" + request.getFormat() + "，目前支持 json、markdown 和 pdf 格式");
        }

        ExportRecord record = new ExportRecord();
        record.setUserId(userId);
        record.setExportType(!exportTypes.isEmpty() ? exportTypes.substring(0, exportTypes.length() - 1) : "全部数据");
        record.setFormat(request.getFormat());
        record.setFileSize((long) (isBinary ? binaryContent.length : content.length()));
        record.setCreateTime(LocalDateTime.now());
        exportRecordMapper.insert(record);

        ExportDataResponse response = new ExportDataResponse();
        response.setContent(content);
        response.setBinaryContent(binaryContent);
        response.setFileName(fileName);
        response.setContentType(contentType);
        response.setBinary(isBinary);
        return response;
    }

    /**
     * 获取导出历史
     * 查询指定用户的所有导出记录
     *
     * @param userId 用户ID
     * @return 导出记录响应列表
     */
    @Override
    public List<ExportRecordResponse> getExportHistory(Long userId) {
        log.info("获取导出历史，用户ID：{}", userId);
        List<ExportRecord> records = exportRecordMapper.selectByUserId(userId);
        return records.stream()
                .map(this::convertToExportResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据文件类型提取文本内容
     * 支持PDF、DOCX、TXT、MD、EPUB格式
     *
     * @param file     上传的文件
     * @param fileType 文件扩展名（不含点号）
     * @return 提取的纯文本内容
     * @throws Exception 文件读取或解析异常
     */
    private String extractContent(MultipartFile file, String fileType) throws Exception {
        return switch (fileType.toLowerCase()) {
            case "pdf" -> extractPdfContent(file);
            case "docx" -> extractDocxContent(file);
            case "txt", "md" -> extractTextContent(file);
            case "epub" -> extractEpubContent(file);
            default -> throw new BusinessException(400, "不支持的文件格式：" + fileType);
        };
    }

    /**
     * 从PDF文件提取文本内容
     *
     * @param file 上传的PDF文件
     * @return 提取的文本内容
     * @throws Exception 提取失败时抛出异常
     */
    private String extractPdfContent(MultipartFile file) throws Exception {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 从DOCX文件提取文本内容
     *
     * @param file 上传的DOCX文件
     * @return 提取的文本内容
     * @throws Exception 提取失败时抛出异常
     */
    private String extractDocxContent(MultipartFile file) throws Exception {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    /**
     * 从纯文本文件提取内容
     *
     * @param file 上传的文本文件
     * @return 提取的文本内容
     * @throws Exception 提取失败时抛出异常
     */
    private String extractTextContent(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    /**
     * 从EPUB电子书提取文本内容
     * EPUB本质上是一个ZIP压缩包，包含HTML文件
     *
     * @param file 上传的EPUB文件
     * @return 提取的文本内容
     * @throws Exception 提取失败时抛出异常
     */
    private String extractEpubContent(MultipartFile file) throws Exception {
        StringBuilder content = new StringBuilder();
        Map<String, String> htmlFiles = new HashMap<>();
        String opfPath = null;

        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (name.endsWith(".opf")) {
                    opfPath = name;
                } else if (name.endsWith(".html") || name.endsWith(".xhtml") || name.endsWith(".htm")) {
                    String htmlContent = readStreamContent(zis);
                    htmlFiles.put(name, htmlContent);
                } else if ("META-INF/container.xml".equals(name)) {
                    String containerXml = readStreamContent(zis);
                    opfPath = extractOpfPath(containerXml);
                }

                zis.closeEntry();
            }
        }

        if (opfPath != null) {
            List<String> orderedFiles = parseOpfForReadingOrder(opfPath, htmlFiles);
            for (String filePath : orderedFiles) {
                String htmlContent = htmlFiles.get(filePath);
                if (htmlContent != null) {
                    String text = extractTextFromHtml(htmlContent);
                    if (!text.isBlank()) {
                        content.append(text).append("\n\n");
                    }
                }
            }
        } else {
            for (String htmlContent : htmlFiles.values()) {
                String text = extractTextFromHtml(htmlContent);
                if (!text.isBlank()) {
                    content.append(text).append("\n\n");
                }
            }
        }

        return content.toString().trim();
    }

    /**
     * 读取ZIP输入流内容
     *
     * @param zis ZIP输入流
     * @return 读取的字符串内容
     * @throws Exception 读取失败时抛出异常
     */
    private String readStreamContent(ZipInputStream zis) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            baos.write(buffer, 0, len);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    /**
     * 从container.xml中提取OPF文件路径
     *
     * @param containerXml container.xml内容
     * @return OPF文件路径
     */
    private String extractOpfPath(String containerXml) {
        Pattern pattern = Pattern.compile("full-path\\s*=\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(containerXml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 解析OPF文件获取阅读顺序
     *
     * @param opfPath   OPF文件路径
     * @param htmlFiles HTML文件映射
     * @return 按阅读顺序排列的文件路径列表
     */
    private List<String> parseOpfForReadingOrder(String opfPath, Map<String, String> htmlFiles) {
        List<String> orderedFiles = new ArrayList<>();
        String opfDir = opfPath.contains("/") ? opfPath.substring(0, opfPath.lastIndexOf('/') + 1) : "";

        for (String filePath : htmlFiles.keySet()) {
            if (filePath.startsWith(opfDir)) {
                orderedFiles.add(filePath);
            }
        }

        return orderedFiles;
    }

    /**
     * 从HTML内容中提取纯文本
     * 移除HTML标签，保留文本内容
     *
     * @param html HTML内容
     * @return 纯文本内容
     */
    private String extractTextFromHtml(String html) {
        String text = html;
        text = text.replaceAll("<script[^>]*>.*?</script>", " ");
        text = text.replaceAll("<style[^>]*>.*?</style>", " ");
        text = text.replaceAll("<head[^>]*>.*?</head>", " ");
        text = text.replaceAll("<[^>]+>", " ");
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&amp;", "&");
        text = text.replaceAll("&quot;", "\"");
        text = text.replaceAll("\\s+", " ").trim();
        return text;
    }

    /**
     * 查找或创建导入用的知识库
     * 如果用户已有同名知识库则复用，否则创建新的
     * 与用户手动创建笔记时的 findOrCreateKnowledge 逻辑保持一致
     *
     * @param userId            用户ID
     * @param suggestedTitle    AI建议的知识库标题（可为null）
     * @param suggestedCategory AI建议的知识库分类（可为null）
     * @param fileName          文件名（用于回退标题）
     * @return 查找或创建的知识库
     */
    private Knowledge findOrCreateKnowledgeForImport(Long userId, String suggestedTitle,
                                                     KnowledgeCategoryEnum suggestedCategory, String fileName) {
        String title = suggestedTitle;
        if (title == null || title.isBlank()) {
            title = fileName;
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                title = fileName.substring(0, dotIndex);
            }
        }

        List<Knowledge> existingKnowledge = knowledgeMapper.selectByUserId(userId);
        for (Knowledge k : existingKnowledge) {
            if (k.getTitle().equals(title)) {
                log.info("复用现有知识库，知识库ID：{}，标题：{}", k.getId(), k.getTitle());
                return k;
            }
        }

        Knowledge knowledge = new Knowledge();
        knowledge.setUserId(userId);
        knowledge.setTitle(title);
        knowledge.setDescription("从文件《" + fileName + "》导入创建");
        knowledge.setCategory(suggestedCategory != null ? suggestedCategory : KnowledgeCategoryEnum.OTHER);
        knowledge.setItemCount(0);
        knowledge.setNoteCount(0);
        knowledge.setCreateTime(LocalDateTime.now());
        knowledge.setUpdateTime(LocalDateTime.now());

        knowledgeMapper.insert(knowledge);
        log.info("创建新知识库，知识库ID：{}，标题：{}", knowledge.getId(), knowledge.getTitle());

        notificationService.createNotification(
                userId,
                "知识库创建成功",
                "您的新知识库《" + knowledge.getTitle() + "》已成功创建。",
                NotificationTypeEnum.KNOWLEDGE
        );

        return knowledge;
    }

    /**
     * 基础解析：将文件内容按段落分割为笔记列表
     * 作为AI服务不可用时的回退方案，仅做简单的段落分割
     * 清理Markdown语法标记，保留纯文本内容
     *
     * @param userId      用户ID
     * @param content     文件提取的原始文本内容
     * @param fileName    文件名
     * @param knowledgeId 关联的知识库ID（可为null）
     * @return 笔记列表
     */
    private List<Note> parseToNotesBasic(Long userId, String content, String fileName, Long knowledgeId) {
        List<Note> notes = new ArrayList<>();

        String[] paragraphs = content.split("\n\n+");

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (paragraph.isEmpty()) continue;

            String cleanedContent = cleanMarkdownSyntax(paragraph);
            if (cleanedContent.isBlank()) continue;

            Note note = new Note();
            note.setUserId(userId);
            note.setKnowledgeId(knowledgeId);

            String title = extractTitle(cleanedContent, fileName, i);
            note.setTitle(cleanMarkdownSyntax(title));
            note.setContent(cleanedContent);
            note.setCategory(NoteCategoryEnum.OTHER);
            note.setStatus(NoteStatusEnum.PUBLISHED);
            note.setWordCount(calculateWordCount(cleanedContent));
            note.setSource("文件导入 - " + fileName);
            note.setCreateTime(LocalDateTime.now());
            note.setUpdateTime(LocalDateTime.now());

            notes.add(note);
        }

        return notes;
    }

    /**
     * 清理Markdown语法标记
     * 去除#、**、*、`、>、-等Markdown符号，保留纯文本内容
     *
     * @param text 包含Markdown语法的文本
     * @return 清理后的纯文本
     */
    private String cleanMarkdownSyntax(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String cleaned = text;

        cleaned = cleaned.replaceAll("^#{1,6}\\s+", "");
        cleaned = cleaned.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("\\*(.+?)\\*", "$1");
        cleaned = cleaned.replaceAll("__(.+?)__", "$1");
        cleaned = cleaned.replaceAll("_(.+?)_", "$1");
        cleaned = cleaned.replaceAll("`(.+?)`", "$1");
        cleaned = cleaned.replaceAll("```[\\w]*\\n?", "");
        cleaned = cleaned.replaceAll("```", "");
        cleaned = cleaned.replaceAll("^>\\s*", "");
        cleaned = cleaned.replaceAll("^[-*+]\\s+", "");
        cleaned = cleaned.replaceAll("^\\d+\\.\\s+", "");
        cleaned = cleaned.replaceAll("!\\[([^\\]]*)\\]\\([^)]+\\)", "$1");
        cleaned = cleaned.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
    }

    /**
     * 使用AI解析文件内容，智能拆分笔记并生成标题、分类等字段
     * AI会根据内容语义进行合理分段，生成合适的标题和分类
     * 对于超长内容，采用分段发送策略，确保不丢失内容
     *
     * @param content  文件提取的原始文本内容
     * @param fileName 文件名
     * @return AI解析结果，包含笔记列表和建议的知识库信息
     */
    private AiParsedResult parseContentWithAi(String content, String fileName) {
        log.info("使用AI解析文件内容，文件名：{}，内容长度：{}", fileName, content.length());

        if (content.length() <= 6000) {
            return parseContentWithAiSingle(content, fileName);
        }

        return parseContentWithAiChunked(content, fileName);
    }

    /**
     * 单次AI解析（内容较短时使用）
     * 将完整内容一次性发送给AI解析
     *
     * @param content  完整文件内容
     * @param fileName 文件名
     * @return AI解析结果
     */
    private AiParsedResult parseContentWithAiSingle(String content, String fileName) {
        String prompt = buildImportParsePrompt(content, fileName);

        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("AI解析导入内容（单次），尝试 {}/{}", attempt, maxRetries);
                String response = aiProviderService.generateResponseWithModel(prompt, knowledgeModel, 0.3);

                if (response == null || response.isBlank()) {
                    log.warn("AI返回空响应，尝试次数：{}", attempt);
                    continue;
                }

                AiParsedResult result = parseAiImportResponse(response, fileName);
                if (result != null && !result.getNotes().isEmpty()) {
                    log.info("AI解析成功，生成笔记数：{}，建议知识库标题：{}",
                            result.getNotes().size(), result.getSuggestedKnowledgeTitle());
                    return result;
                }

                log.warn("AI解析结果为空，尝试次数：{}", attempt);
            } catch (Exception e) {
                log.error("AI解析失败，尝试次数：{}，错误：{}", attempt, e.getMessage());
            }
        }

        log.warn("AI单次解析最终失败，回退到基础解析");
        return new AiParsedResult(parseToNotesBasic(null, content, fileName, null), null, null);
    }

    /**
     * 分段AI解析（内容较长时使用）
     * 将内容按段落分割为多个片段，分别发送给AI解析，最后合并结果
     * 每个片段独立解析，确保不丢失任何内容
     *
     * @param content  完整文件内容
     * @param fileName 文件名
     * @return 合并后的AI解析结果
     */
    private AiParsedResult parseContentWithAiChunked(String content, String fileName) {
        log.info("内容较长（{}字符），采用分段AI解析", content.length());

        String[] paragraphs = content.split("\n\n+");
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int chunkSize = 0;

        for (String paragraph : paragraphs) {
            if (chunkSize + paragraph.length() > 5000 && !currentChunk.isEmpty()) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
                chunkSize = 0;
            }
            if (!currentChunk.isEmpty()) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(paragraph);
            chunkSize += paragraph.length();
        }
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
        }

        log.info("内容分为 {} 个片段进行AI解析", chunks.size());

        List<Note> allNotes = new ArrayList<>();
        String knowledgeTitle = null;
        KnowledgeCategoryEnum knowledgeCategory = null;

        for (int i = 0; i < chunks.size(); i++) {
            log.info("解析第 {}/{} 片段", i + 1, chunks.size());
            String prompt = buildImportParsePrompt(chunks.get(i), fileName + "（片段" + (i + 1) + "）");

            int maxRetries = 2;
            AiParsedResult chunkResult = null;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    String response = aiProviderService.generateResponseWithModel(prompt, knowledgeModel, 0.3);
                    if (response != null && !response.isBlank()) {
                        chunkResult = parseAiImportResponse(response, fileName);
                        if (chunkResult != null && !chunkResult.getNotes().isEmpty()) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("分段AI解析失败，片段：{}，尝试：{}，错误：{}", i + 1, attempt, e.getMessage());
                }
            }

            if (chunkResult == null || chunkResult.getNotes().isEmpty()) {
                log.warn("片段 {} AI解析失败，使用基础解析", i + 1);
                allNotes.addAll(parseToNotesBasic(null, chunks.get(i), fileName, null));
            } else {
                allNotes.addAll(chunkResult.getNotes());
                if (i == 0) {
                    knowledgeTitle = chunkResult.getSuggestedKnowledgeTitle();
                    knowledgeCategory = chunkResult.getSuggestedKnowledgeCategory();
                }
            }
        }

        log.info("分段解析完成，共生成 {} 条笔记", allNotes.size());
        return new AiParsedResult(allNotes, knowledgeTitle, knowledgeCategory);
    }

    /**
     * 构建导入内容AI解析的提示词
     * 指导AI将任意格式的内容拆分为结构化的笔记数据
     *
     * @param content  文件内容
     * @param fileName 文件名
     * @return 提示词
     */
    private String buildImportParsePrompt(String content, String fileName) {
        int contentLength = content.length();
        int maxNotes = Math.max(5, Math.min(15, contentLength / 500));

        return """
                你是一个文档解析助手。请分析以下从文件中提取的内容，将其拆分为多条笔记。
                
                文件名：%s
                文件内容：
                %s
                
                要求：
                1. 根据内容语义合理拆分为1-%d条笔记，每条笔记应是一个完整的主题或段落
                2. 为每条笔记生成简洁的标题（5-30字）
                3. 为每条笔记选择最合适的分类
                4. 笔记内容应为纯文本，去除Markdown语法标记（如#、**、*、`等），保留实际内容
                5. 每条笔记内容应保持完整，不要过度拆分，单个主题的内容应合并为一条笔记
                6. 建议一个知识库标题和分类
                
                笔记分类（必须从以下选择）：
                - 学习笔记：学习相关内容、课程笔记、知识整理
                - 工作记录：工作相关、任务记录、进度跟踪
                - 生活随笔：日常生活、感悟、随笔
                - 技术文档：编程、技术、开发相关
                - 读书笔记：书籍阅读、读后感、书摘
                - 项目总结：项目相关、总结报告
                - 会议记录：会议、讨论、决策记录
                - 其他：无法归入以上类别
                
                知识库分类（必须从以下选择）：
                - 技术、学习、工作、生活、健康、财经、文化、其他
                
                只返回JSON，不要其他内容。格式如下：
                {
                  "suggestedKnowledgeTitle": "知识库标题（10-25字）",
                  "suggestedKnowledgeCategory": "知识库分类",
                  "notes": [
                    {
                      "title": "笔记标题",
                      "content": "笔记纯文本内容",
                      "category": "笔记分类"
                    }
                  ]
                }
                """.formatted(fileName, content, maxNotes);
    }

    /**
     * 解析AI返回的导入内容JSON
     *
     * @param response AI返回的JSON字符串
     * @param fileName 文件名（用于回退处理）
     * @return 解析结果
     */
    private AiParsedResult parseAiImportResponse(String response, String fileName) {
        try {
            String cleaned = response
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            int startIndex = cleaned.indexOf("{");
            int endIndex = cleaned.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                cleaned = cleaned.substring(startIndex, endIndex + 1);
            }

            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(cleaned);

            String suggestedTitle = null;
            if (root.has("suggestedKnowledgeTitle")) {
                suggestedTitle = root.get("suggestedKnowledgeTitle").asText();
            }

            KnowledgeCategoryEnum suggestedCategory = null;
            if (root.has("suggestedKnowledgeCategory")) {
                suggestedCategory = KnowledgeCategoryEnum.fromDescription(
                        root.get("suggestedKnowledgeCategory").asText());
            }

            List<Note> notes = new ArrayList<>();
            if (root.has("notes") && root.get("notes").isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode noteNode : root.get("notes")) {
                    String title = noteNode.has("title") ? noteNode.get("title").asText() : fileName;
                    String noteContent = noteNode.has("content") ? noteNode.get("content").asText() : "";
                    String categoryDesc = noteNode.has("category") ? noteNode.get("category").asText() : "其他";

                    if (noteContent.isBlank()) continue;

                    Note note = new Note();
                    note.setTitle(title.length() > 100 ? title.substring(0, 100) : title);
                    note.setContent(noteContent);
                    note.setCategory(NoteCategoryEnum.fromDescription(categoryDesc));
                    note.setStatus(NoteStatusEnum.PUBLISHED);
                    note.setWordCount(calculateWordCount(noteContent));
                    note.setSource("文件导入 - " + fileName);
                    note.setCreateTime(LocalDateTime.now());
                    note.setUpdateTime(LocalDateTime.now());

                    notes.add(note);
                }
            }

            return new AiParsedResult(notes, suggestedTitle, suggestedCategory);

        } catch (Exception e) {
            log.error("解析AI导入响应失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * AI解析结果内部类
     * 封装AI解析文件内容后的返回数据
     */
    private static class AiParsedResult {
        private final List<Note> notes;
        private final String suggestedKnowledgeTitle;
        private final KnowledgeCategoryEnum suggestedKnowledgeCategory;

        AiParsedResult(List<Note> notes, String suggestedKnowledgeTitle, KnowledgeCategoryEnum suggestedKnowledgeCategory) {
            this.notes = notes != null ? notes : new ArrayList<>();
            this.suggestedKnowledgeTitle = suggestedKnowledgeTitle;
            this.suggestedKnowledgeCategory = suggestedKnowledgeCategory;
        }

        List<Note> getNotes() {
            return notes;
        }

        String getSuggestedKnowledgeTitle() {
            return suggestedKnowledgeTitle;
        }

        KnowledgeCategoryEnum getSuggestedKnowledgeCategory() {
            return suggestedKnowledgeCategory;
        }
    }

    /**
     * 从内容中提取标题
     * 取内容第一行作为标题，超过50字则使用文件名+序号
     *
     * @param content  笔记内容
     * @param fileName 文件名（用于回退标题）
     * @param index    笔记序号（用于回退标题）
     * @return 提取的标题
     */
    private String extractTitle(String content, String fileName, int index) {
        String firstLine = content.split("\n")[0];
        if (firstLine.length() <= 50) {
            return firstLine;
        }
        return fileName + " - 笔记" + (index + 1);
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

    /**
     * 为导入的笔记批量生成AI摘要
     * 异步处理，失败不影响导入结果
     *
     * @param userId 用户ID
     * @param notes  笔记列表
     */
    private void generateSummariesForNotes(Long userId, List<Note> notes) {
        log.info("开始为导入的笔记生成AI摘要，用户ID：{}，笔记数量：{}", userId, notes.size());
        int successCount = 0;
        int failCount = 0;

        for (Note note : notes) {
            try {
                if (note.getContent() != null && !note.getContent().isBlank()) {
                    aiService.generateSummaryForNote(note.getId(), userId, note.getTitle(), note.getContent());
                    successCount++;
                    log.debug("笔记AI摘要生成成功，笔记ID：{}", note.getId());
                }
            } catch (Exception e) {
                failCount++;
                log.warn("笔记AI摘要生成失败，笔记ID：{}，错误：{}", note.getId(), e.getMessage());
            }
        }

        log.info("AI摘要生成完成，成功：{}，失败：{}", successCount, failCount);
    }

    /**
     * 获取文件扩展名（不含点号）
     * 例如 "document.pdf" 返回 "pdf"
     *
     * @param fileName 文件名
     * @return 文件扩展名，无扩展名时返回空字符串
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 将导入记录实体转换为响应DTO
     *
     * @param record 导入记录实体
     * @return 导入记录响应DTO
     */
    private ImportRecordResponse convertToImportResponse(ImportRecord record) {
        ImportRecordResponse response = new ImportRecordResponse();
        response.setId(record.getId());
        response.setFileName(record.getFileName());
        response.setFileType(record.getFileType());
        response.setFileSize(record.getFileSize());
        response.setStatus(record.getStatus() != null ? record.getStatus().getCode() : 0);
        response.setNoteCount(record.getNoteCount());
        response.setKnowledgeCount(record.getKnowledgeCount());
        response.setErrorMessage(record.getErrorMessage());
        response.setCreateTime(record.getCreateTime());
        return response;
    }

    /**
     * 将导出记录实体转换为响应DTO
     *
     * @param record 导出记录实体
     * @return 导出记录响应DTO
     */
    private ExportRecordResponse convertToExportResponse(ExportRecord record) {
        ExportRecordResponse response = new ExportRecordResponse();
        response.setId(record.getId());
        response.setExportType(record.getExportType());
        response.setFormat(record.getFormat());
        response.setFileSize(record.getFileSize());
        response.setCreateTime(record.getCreateTime());
        return response;
    }

    /**
     * 将导出数据转换为Markdown格式文本
     * 包含笔记、知识库、AI摘要等内容的格式化输出
     *
     * @param exportData 导出数据Map，key为数据类型，value为数据对象
     * @return Markdown格式的文本内容
     */
    private String convertToMarkdown(Map<String, Object> exportData) {
        StringBuilder md = new StringBuilder();
        md.append("# 知忆岛数据导出\n\n");
        md.append("导出时间：").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        if (exportData.containsKey("notes")) {
            md.append("## 笔记\n\n");
            @SuppressWarnings("unchecked")
            List<ExportDTO.NoteExport> notes = (List<ExportDTO.NoteExport>) exportData.get("notes");
            for (ExportDTO.NoteExport note : notes) {
                md.append("### ").append(note.getTitle()).append("\n\n");
                md.append("分类：").append(note.getCategory() != null ? note.getCategory() : "未分类").append("\n\n");
                md.append(note.getContent()).append("\n\n");
                md.append("---\n\n");
            }
        }

        if (exportData.containsKey("knowledge")) {
            md.append("## 知识库\n\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) exportData.get("knowledge");
            for (Map<String, Object> kw : knowledgeList) {
                ExportDTO.KnowledgeExport knowledge = (ExportDTO.KnowledgeExport) kw.get("knowledge");
                md.append("### ").append(knowledge.getTitle()).append("\n\n");
                md.append("分类：").append(knowledge.getCategory() != null ? knowledge.getCategory() : "未分类").append("\n\n");
                md.append("描述：").append(knowledge.getDescription() != null ? knowledge.getDescription() : "无").append("\n\n");

                @SuppressWarnings("unchecked")
                List<ExportDTO.KnowledgeItemExport> items = (List<ExportDTO.KnowledgeItemExport>) kw.get("items");
                if (items != null && !items.isEmpty()) {
                    md.append("**知识条目：**\n\n");
                    for (ExportDTO.KnowledgeItemExport item : items) {
                        md.append("- **").append(item.getTitle()).append("**\n");
                        md.append("  ").append(item.getContent()).append("\n\n");
                    }
                }
                md.append("---\n\n");
            }
        }

        if (exportData.containsKey("aiSummaries")) {
            md.append("## AI摘要\n\n");
            @SuppressWarnings("unchecked")
            List<ExportDTO.AiSummaryExport> summaries = (List<ExportDTO.AiSummaryExport>) exportData.get("aiSummaries");
            for (ExportDTO.AiSummaryExport summary : summaries) {
                md.append("### AI摘要\n\n");
                md.append(summary.getSummaryContent()).append("\n\n");
                md.append("---\n\n");
            }
        }

        return md.toString();
    }

    /**
     * 将导出数据转换为PDF二进制内容
     * 使用Thymeleaf模板 + Flying Saucer生成PDF
     *
     * @param exportData 导出数据Map
     * @return PDF文件的字节数组
     */
    private byte[] convertToPdf(Map<String, Object> exportData) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Context context = new Context();

            String exportTime = "导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            context.setVariable("exportTime", exportTime);

            if (exportData.containsKey("notes")) {
                @SuppressWarnings("unchecked")
                List<ExportDTO.NoteExport> notes = (List<ExportDTO.NoteExport>) exportData.get("notes");
                context.setVariable("notes", notes);
            }

            if (exportData.containsKey("knowledge")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> knowledgeList = (List<Map<String, Object>>) exportData.get("knowledge");
                List<KnowledgeExportView> knowledgeViews = new ArrayList<>();
                for (Map<String, Object> kw : knowledgeList) {
                    KnowledgeExportView view = new KnowledgeExportView();
                    view.setKnowledge((ExportDTO.KnowledgeExport) kw.get("knowledge"));
                    @SuppressWarnings("unchecked")
                    List<ExportDTO.KnowledgeItemExport> items = (List<ExportDTO.KnowledgeItemExport>) kw.get("items");
                    view.setItems(items);
                    knowledgeViews.add(view);
                }
                context.setVariable("knowledgeList", knowledgeViews);
            }

            if (exportData.containsKey("aiSummaries")) {
                @SuppressWarnings("unchecked")
                List<ExportDTO.AiSummaryExport> summaries = (List<ExportDTO.AiSummaryExport>) exportData.get("aiSummaries");
                context.setVariable("aiSummaries", summaries);
            }

            String htmlContent = templateEngine.process("export-pdf", context);
            log.debug("PDF HTML内容：{}", htmlContent);

            ITextRenderer renderer = new ITextRenderer();

            String fontPath = getChineseFontPath();
            if (fontPath != null) {
                renderer.getFontResolver().addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }

            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("PDF生成失败：{}", e.getMessage(), e);
            throw new BusinessException(500, "PDF生成失败：" + e.getMessage());
        }
    }

    @Setter
    @Getter
    public static class KnowledgeExportView {
        private ExportDTO.KnowledgeExport knowledge;
        private List<ExportDTO.KnowledgeItemExport> items;

    }

    /**
     * 获取中文字体路径
     * 优先使用系统字体
     *
     * @return 字体文件路径
     */
    private String getChineseFontPath() {
        String[] systemFontPaths = {
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/msyh.ttc",
                "/System/Library/Fonts/PingFang.ttc",
                "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
        };

        for (String fontPath : systemFontPaths) {
            java.io.File fontFile = new java.io.File(fontPath);
            if (fontFile.exists()) {
                log.info("使用系统字体：{}", fontPath);
                return fontPath;
            }
        }

        try {
            ClassPathResource fontResource = new ClassPathResource("fonts/SourceHanSansSC-Regular.ttf");
            if (fontResource.exists()) {
                java.io.File tempFont = java.io.File.createTempFile("font", ".ttf");
                try (InputStream is = fontResource.getInputStream();
                     java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFont)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
                tempFont.deleteOnExit();
                log.info("使用classpath字体");
                return tempFont.getAbsolutePath();
            }
        } catch (Exception e) {
            log.warn("加载classpath字体失败：{}", e.getMessage());
        }

        log.warn("未找到中文字体，PDF可能无法正确显示中文");
        return null;
    }

    /**
     * 将技术性错误信息翻译为用户友好的中文提示
     * 匹配常见异常关键词并返回对应的中文描述
     *
     * @param message 原始错误信息
     * @return 用户友好的错误提示
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
            return "文件未找到";
        }
        if (lowerMsg.contains("permission denied") || lowerMsg.contains("access denied")) {
            return "权限不足";
        }
        if (lowerMsg.contains("out of memory") || lowerMsg.contains("memory")) {
            return "内存不足";
        }
        if (lowerMsg.contains("file too large") || lowerMsg.contains("size")) {
            return "文件过大";
        }
        if (lowerMsg.contains("invalid") || lowerMsg.contains("format")) {
            return "文件格式无效";
        }
        if (lowerMsg.contains("empty")) {
            return "文件内容为空";
        }
        if (lowerMsg.contains("encoding") || lowerMsg.contains("charset")) {
            return "编码格式错误";
        }
        if (lowerMsg.contains("corrupt") || lowerMsg.contains("damaged")) {
            return "文件已损坏";
        }

        return "处理失败";
    }
}
