package com.example.zhiyiislandbackend.common.constant;

/**
 * 业务常量定义类
 * 包含文件存储、图表等业务相关的常量
 */
public class BusinessConstants {

    private BusinessConstants() {
    }

    /**
     * 图表相关常量
     */
    public static final class Diagram {
        /** 图片类型 */
        public static final String TYPE_IMAGE = "image";

        private Diagram() {
        }
    }

    /**
     * 文件存储相关常量
     */
    public static final class File {
        /** 头像存储路径前缀 */
        public static final String AVATAR_PREFIX = "avatar/";
        /** 笔记图片存储路径前缀 */
        public static final String NOTE_IMAGE_PREFIX = "note-images/";
        /** 日期格式（用于文件路径） */
        public static final String DATE_FORMAT = "yyyy/MM/dd";

        private File() {
        }
    }
}
