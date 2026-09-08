package com.example.zhiyiislandbackend.service;

/**
 * AI提供商服务接口
 * 定义与AI模型交互的核心操作，包括文本生成和向量嵌入
 */
public interface AiProviderService {

    /**
     * 使用默认参数生成AI响应
     *
     * @param prompt 输入提示词
     * @return AI生成的响应文本
     */
    String generateResponse(String prompt);

    /**
     * 使用指定温度参数生成AI响应
     *
     * @param prompt      输入提示词
     * @param temperature 温度参数（控制随机性，0-1之间）
     * @return AI生成的响应文本
     */
    String generateResponse(String prompt, Double temperature);

    /**
     * 使用指定模型和温度参数生成AI响应
     *
     * @param prompt      输入提示词
     * @param model       模型名称
     * @param temperature 温度参数（控制随机性，0-1之间）
     * @return AI生成的响应文本
     */
    String generateResponseWithModel(String prompt, String model, Double temperature);

    /**
     * 使用默认模型生成AI响应（禁用深度思考）
     * 适用于智能搜索等不需要深度推理的场景，可提升响应速度
     *
     * @param prompt 输入提示词
     * @return AI生成的响应文本
     */
    String generateResponseWithoutThinking(String prompt);

    /**
     * 生成文本的向量嵌入
     *
     * @param text 输入文本
     * @return 向量嵌入数组
     */
    float[] generateEmbedding(String text);

    /**
     * 检查AI服务是否可用
     *
     * @return 服务是否可用
     */
    boolean isServiceAvailable();
}
