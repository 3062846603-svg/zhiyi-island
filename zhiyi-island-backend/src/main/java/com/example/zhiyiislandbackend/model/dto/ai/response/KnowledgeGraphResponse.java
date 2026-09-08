package com.example.zhiyiislandbackend.model.dto.ai.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 知识图谱响应DTO
 * 返回知识图谱的节点和边数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeGraphResponse {
    /**
     * 节点列表
     */
    private List<Node> nodes;
    /**
     * 边列表
     */
    private List<Edge> edges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Node {
        private Integer id;
        private String label;
        private Integer level;
        private String color;
        private Integer x;
        private Integer y;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Edge {
        private Integer from;
        private Integer to;
    }
}
