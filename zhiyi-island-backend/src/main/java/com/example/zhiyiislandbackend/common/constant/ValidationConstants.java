package com.example.zhiyiislandbackend.common.constant;

/**
 * 验证常量定义类
 * 包含笔记验证、文件大小限制等常量
 */
public class ValidationConstants {

    private ValidationConstants() {
    }

    /**
     * 笔记验证相关常量
     */
    public static final class Note {
        /** 标题不能为空 */
        public static final String TITLE_NOT_BLANK = "标题不能为空";
        /** 标题长度限制 */
        public static final String TITLE_SIZE = "标题长度不能超过200个字符";
        /** 内容不能为空 */
        public static final String CONTENT_NOT_BLANK = "内容不能为空";
        /** 分类长度限制 */
        public static final String CATEGORY_SIZE = "分类长度不能超过50个字符";

        private Note() {
        }
    }

    /**
     * 文件验证相关常量
     */
    public static final class File {
        /** 头像最大大小（5MB） */
        public static final int AVATAR_MAX_SIZE = 5 * 1024 * 1024;
        /** 笔记图片最大大小（10MB） */
        public static final int NOTE_IMAGE_MAX_SIZE = 10 * 1024 * 1024;

        private File() {
        }
    }
}
