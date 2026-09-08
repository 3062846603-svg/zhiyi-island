-- =============================================
-- 知忆岛数据库初始化脚本 (PostgreSQL版本)
-- 优化版：精简表结构，移除无业务字段
-- 支持向量搜索（pgvector扩展）
-- 最后更新：2026年
-- =============================================

-- 注意：请先手动创建数据库
-- CREATE DATABASE zhiyi_island WITH ENCODING='UTF8';
-- \c zhiyi_island

-- 启用pgvector扩展（用于向量搜索）
CREATE EXTENSION IF NOT EXISTS vector;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS "user"
(
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(100),
    nickname        VARCHAR(50),
    avatar          VARCHAR(500),
    phone           VARCHAR(20),
    status          SMALLINT  DEFAULT 1,
    last_login_time TIMESTAMP,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_username ON "user" (username);
CREATE INDEX idx_user_email ON "user" (email);
CREATE INDEX idx_user_status ON "user" (status);

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".id IS '用户ID';
COMMENT ON COLUMN "user".username IS '用户名';
COMMENT ON COLUMN "user".password IS '密码（加密存储）';
COMMENT ON COLUMN "user".email IS '邮箱';
COMMENT ON COLUMN "user".nickname IS '昵称';
COMMENT ON COLUMN "user".avatar IS '头像URL';
COMMENT ON COLUMN "user".phone IS '手机号';
COMMENT ON COLUMN "user".status IS '状态：1-正常，0-禁用，2-待激活';
COMMENT ON COLUMN "user".last_login_time IS '最后登录时间';
COMMENT ON COLUMN "user".create_time IS '创建时间';
COMMENT ON COLUMN "user".update_time IS '更新时间';

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 用户表更新时间触发器
CREATE TRIGGER update_user_updated_at
    BEFORE UPDATE
    ON "user"
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 知识库表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    category    SMALLINT,
    item_count  INT       DEFAULT 0,
    note_count  INT       DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_knowledge_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_knowledge_user_id ON knowledge (user_id);
CREATE INDEX idx_knowledge_category ON knowledge (category);

COMMENT ON TABLE knowledge IS '知识库表';
COMMENT ON COLUMN knowledge.id IS '知识库ID';
COMMENT ON COLUMN knowledge.user_id IS '用户ID';
COMMENT ON COLUMN knowledge.title IS '标题';
COMMENT ON COLUMN knowledge.description IS '描述';
COMMENT ON COLUMN knowledge.category IS '分类';
COMMENT ON COLUMN knowledge.item_count IS '知识条目数';
COMMENT ON COLUMN knowledge.note_count IS '笔记数量';

-- 知识库表更新时间触发器
CREATE TRIGGER update_knowledge_updated_at
    BEFORE UPDATE
    ON knowledge
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 笔记表
-- =============================================
CREATE TABLE IF NOT EXISTS note
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    knowledge_id BIGINT       NULL,

    title        VARCHAR(200) NOT NULL,
    content      TEXT,

    category     SMALLINT,
    status       SMALLINT  DEFAULT 1,

    word_count   INT       DEFAULT 0,
    source       VARCHAR(255),

    embedding    vector(1024),

    create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_note_knowledge FOREIGN KEY (knowledge_id) REFERENCES knowledge (id) ON DELETE SET NULL
);

CREATE INDEX idx_note_user_id ON note (user_id);
CREATE INDEX idx_note_knowledge_id ON note (knowledge_id);
CREATE INDEX idx_note_category ON note (category);
CREATE INDEX idx_note_status ON note (status);
CREATE INDEX idx_note_create_time ON note (create_time);
CREATE INDEX idx_note_update_time ON note (update_time);

CREATE INDEX idx_note_embedding ON note USING hnsw (embedding vector_cosine_ops);

COMMENT ON TABLE note IS '笔记表';
COMMENT ON COLUMN note.id IS '笔记ID';
COMMENT ON COLUMN note.user_id IS '用户ID';
COMMENT ON COLUMN note.knowledge_id IS '所属知识库ID，NULL表示独立笔记';
COMMENT ON COLUMN note.title IS '标题';
COMMENT ON COLUMN note.content IS '笔记内容（支持长文本）';
COMMENT ON COLUMN note.category IS '分类：1-学习笔记，2-工作记录，3-生活随笔，4-技术文档，5-读书笔记，6-项目总结，7-会议记录，8-其他';
COMMENT ON COLUMN note.status IS '状态：0-草稿，1-已发布，2-已归档，3-已删除';
COMMENT ON COLUMN note.word_count IS '字数统计';
COMMENT ON COLUMN note.source IS '笔记来源：manual-手动创建，import-导入，ai-AI生成';
COMMENT ON COLUMN note.embedding IS '向量表示（用于语义搜索）';
COMMENT ON COLUMN note.create_time IS '创建时间';
COMMENT ON COLUMN note.update_time IS '更新时间';

-- 笔记表更新时间触发器
CREATE TRIGGER update_note_updated_at
    BEFORE UPDATE
    ON note
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 笔记图片表
-- =============================================
CREATE TABLE IF NOT EXISTS note_image
(
    id          BIGSERIAL PRIMARY KEY,
    note_id     BIGINT       NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_note_image_note FOREIGN KEY (note_id) REFERENCES note (id) ON DELETE CASCADE
);

CREATE INDEX idx_note_image_note_id ON note_image (note_id);

COMMENT ON TABLE note_image IS '笔记图片表';
COMMENT ON COLUMN note_image.id IS '图片ID';
COMMENT ON COLUMN note_image.note_id IS '笔记ID';
COMMENT ON COLUMN note_image.image_url IS '图片URL';
COMMENT ON COLUMN note_image.create_time IS '创建时间';

-- =============================================
-- AI摘要表
-- =============================================
CREATE TABLE IF NOT EXISTS ai_summary
(
    id                  BIGSERIAL PRIMARY KEY,
    note_id             BIGINT NULL,
    user_id             BIGINT NOT NULL,

    summary_content     TEXT   NOT NULL,
    summary_type        VARCHAR(20) DEFAULT 'auto',
    summary_style       VARCHAR(30) DEFAULT 'keypoints',
    summary_length      VARCHAR(20) DEFAULT 'medium',

    original_word_count INT         DEFAULT 0,
    summary_word_count  INT         DEFAULT 0,

    create_time         TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_summary_note FOREIGN KEY (note_id) REFERENCES note (id) ON DELETE CASCADE,
    CONSTRAINT fk_summary_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_summary_note_id ON ai_summary (note_id);
CREATE INDEX idx_ai_summary_user_id ON ai_summary (user_id);
CREATE INDEX idx_ai_summary_create_time ON ai_summary (create_time);

COMMENT ON TABLE ai_summary IS 'AI摘要表';
COMMENT ON COLUMN ai_summary.summary_type IS '摘要类型：auto-自动，manual-手动触发';
COMMENT ON COLUMN ai_summary.summary_style IS '摘要风格：keypoints-要点，paragraph-段落，outline-大纲';
COMMENT ON COLUMN ai_summary.summary_length IS '摘要长度：short-简短，medium-中等，long-详细';

-- =============================================
-- 知识条目表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_item
(
    id           BIGSERIAL PRIMARY KEY,
    knowledge_id BIGINT       NOT NULL,
    note_id      BIGINT       NULL,
    title        VARCHAR(200) NOT NULL,
    content      TEXT,
    source       VARCHAR(255),
    create_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_item_knowledge FOREIGN KEY (knowledge_id) REFERENCES knowledge (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_note FOREIGN KEY (note_id) REFERENCES note (id) ON DELETE SET NULL
);

CREATE INDEX idx_knowledge_item_knowledge_id ON knowledge_item (knowledge_id);
CREATE INDEX idx_knowledge_item_note_id ON knowledge_item (note_id);

COMMENT ON TABLE knowledge_item IS '知识条目表';

-- 知识条目表更新时间触发器
CREATE TRIGGER update_knowledge_item_updated_at
    BEFORE UPDATE
    ON knowledge_item
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- 搜索历史表
-- =============================================
CREATE TABLE IF NOT EXISTS search_history
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    keyword     VARCHAR(255) NOT NULL,
    search_type VARCHAR(20) DEFAULT 'all',
    create_time TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_search_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_search_history_user_id ON search_history (user_id);
CREATE INDEX idx_search_history_create_time ON search_history (create_time);
CREATE INDEX idx_search_history_keyword ON search_history (keyword);

COMMENT ON TABLE search_history IS '搜索历史表';
COMMENT ON COLUMN search_history.search_type IS '搜索类型：all-全部，note-笔记，knowledge-知识库';

-- =============================================
-- 导入记录表
-- =============================================
CREATE TABLE IF NOT EXISTS import_record
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_type       VARCHAR(50)  NOT NULL,
    file_size       BIGINT    DEFAULT 0,
    status          SMALLINT  DEFAULT 0,
    note_count      INT       DEFAULT 0,
    knowledge_count INT       DEFAULT 0,
    error_message   TEXT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_import_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_import_record_user_id ON import_record (user_id);
CREATE INDEX idx_import_record_status ON import_record (status);
CREATE INDEX idx_import_record_create_time ON import_record (create_time);

COMMENT ON TABLE import_record IS '导入记录表';
COMMENT ON COLUMN import_record.file_type IS '文件类型：pdf/word/markdown/txt';
COMMENT ON COLUMN import_record.status IS '状态：0-处理中，1-成功，2-失败';

-- =============================================
-- 导出记录表
-- =============================================
CREATE TABLE IF NOT EXISTS export_record
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    export_type VARCHAR(100) NOT NULL,
    format      VARCHAR(50)  NOT NULL,
    file_size   BIGINT    DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_export_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_export_record_user_id ON export_record (user_id);
CREATE INDEX idx_export_record_create_time ON export_record (create_time);

COMMENT ON TABLE export_record IS '导出记录表';
COMMENT ON COLUMN export_record.export_type IS '导出类型：note/knowledge/all';
COMMENT ON COLUMN export_record.format IS '导出格式：json/markdown';

-- =============================================
-- 通知表
-- =============================================
CREATE TABLE IF NOT EXISTS notification
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    title       VARCHAR(200) NOT NULL,
    content     TEXT,
    type        SMALLINT  DEFAULT 1,
    is_read     SMALLINT  DEFAULT 0,
    read_time   TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE
);

CREATE INDEX idx_notification_user_id ON notification (user_id);
CREATE INDEX idx_notification_is_read ON notification (is_read);
CREATE INDEX idx_notification_type ON notification (type);
CREATE INDEX idx_notification_create_time ON notification (create_time);

COMMENT ON TABLE notification IS '通知表';
COMMENT ON COLUMN notification.type IS '通知类型：1-系统通知，2-笔记通知，3-AI通知，4-知识库通知';
COMMENT ON COLUMN notification.is_read IS '是否已读：0-未读，1-已读';

-- =============================================
-- 初始化完成
-- =============================================
SELECT '知忆岛数据库初始化完成 - PostgreSQL版本（支持向量搜索）' AS message;
