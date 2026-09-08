package com.example.zhiyiislandbackend.common.constant;

/**
 * 消息常量定义类
 * 包含成功消息、错误消息和通知消息等
 */
public class MessageConstants {

    private MessageConstants() {
    }

    /**
     * 成功消息常量
     */
    public static final class Success {
        /** 创建成功 */
        public static final String CREATE = "创建成功";
        /** 更新成功 */
        public static final String UPDATE = "更新成功";
        /** 删除成功 */
        public static final String DELETE = "删除成功";
        /** 操作成功 */
        public static final String OPERATION = "操作成功";
        /** 生成成功 */
        public static final String GENERATE = "生成成功";
        /** 上传成功 */
        public static final String UPLOAD = "上传成功";

        private Success() {
        }
    }

    /**
     * 错误消息常量
     */
    public static final class Error {
        /** 笔记不存在 */
        public static final String NOTE_NOT_FOUND = "笔记不存在";
        /** 无权删除此笔记 */
        public static final String NO_PERMISSION_DELETE = "无权删除此笔记";
        /** 无权操作此笔记 */
        public static final String NO_PERMISSION_OPERATION = "无权操作此笔记";
        /** 笔记内容为空 */
        public static final String NOTE_CONTENT_EMPTY = "笔记内容为空，无法生成摘要";
        /** 文件不能为空 */
        public static final String FILE_EMPTY = "文件不能为空";
        /** 头像大小超限 */
        public static final String AVATAR_SIZE_EXCEEDED = "头像文件大小不能超过5MB";
        /** 只能上传图片 */
        public static final String IMAGE_ONLY = "只能上传图片文件";
        /** 头像上传失败 */
        public static final String AVATAR_UPLOAD_FAILED = "头像上传失败：";
        /** 文件上传失败 */
        public static final String FILE_UPLOAD_FAILED = "文件上传失败：";
        /** 文件删除失败 */
        public static final String FILE_DELETE_FAILED = "文件删除失败：";

        private Error() {
        }
    }

    /**
     * 通知消息常量
     */
    public static final class Notification {
        /** 笔记创建成功标题 */
        public static final String NOTE_CREATE_TITLE = "笔记创建成功";
        /** 笔记创建成功内容前缀 */
        public static final String NOTE_CREATE_CONTENT_PREFIX = "您的新笔记《";
        /** 笔记创建成功内容后缀 */
        public static final String NOTE_CREATE_CONTENT_SUFFIX = "》已成功创建。";

        /** 草稿保存成功标题 */
        public static final String DRAFT_CREATE_TITLE = "草稿保存成功";
        /** 草稿保存成功内容前缀 */
        public static final String DRAFT_CREATE_CONTENT_PREFIX = "您的草稿《";
        /** 草稿保存成功内容后缀 */
        public static final String DRAFT_CREATE_CONTENT_SUFFIX = "》已成功保存。";

        private Notification() {
        }
    }
}
