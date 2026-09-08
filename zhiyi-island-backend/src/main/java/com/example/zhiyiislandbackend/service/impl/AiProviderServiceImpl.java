package com.example.zhiyiislandbackend.service.impl;

import com.example.zhiyiislandbackend.service.AiProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI服务提供者实现类
 * 支持DeepSeek对话和硅基流动对话/向量嵌入功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiProviderServiceImpl implements AiProviderService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.siliconflow.base-url:https://api.siliconflow.cn/v1}")
    private String baseUrl;

    @Value("${spring.ai.siliconflow.api-key}")
    private String apiKey;

    @Value("${spring.ai.siliconflow.model:Qwen/Qwen2.5-7B-Instruct}")
    private String model;

    @Value("${spring.ai.siliconflow.knowledge-model:Qwen/Qwen2.5-32B-Instruct}")
    private String knowledgeModel;

    @Value("${spring.ai.siliconflow.embedding-model:BAAI/bge-large-zh-v1.5}")
    private String embeddingModel;

    @Value("${spring.ai.siliconflow.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.siliconflow.options.max-tokens:2048}")
    private Integer maxTokens;

    @Value("${spring.ai.siliconflow.provider:siliconflow}")
    private String provider;

    @Value("${spring.ai.siliconflow.thinking-enabled:true}")
    private boolean thinkingEnabled;

    @Value("${spring.ai.siliconflow.embedding-enabled:true}")
    private boolean embeddingEnabled;

    /**
     * 使用默认模型生成AI响应
     *
     * @param prompt 输入提示词
     * @return AI生成的响应文本
     */
    @Override
    public String generateResponse(String prompt) {
        return generateResponse(prompt, null);
    }

    /**
     * 使用默认模型和自定义温度生成AI响应
     *
     * @param prompt            输入提示词
     * @param customTemperature 自定义温度参数
     * @return AI生成的响应文本
     */
    @Override
    public String generateResponse(String prompt, Double customTemperature) {
        return doGenerateResponse(prompt, model, customTemperature != null ? customTemperature : temperature);
    }

    /**
     * 使用指定模型和温度生成AI响应
     *
     * @param prompt            输入提示词
     * @param modelName         模型名称
     * @param customTemperature 自定义温度参数
     * @return AI生成的响应文本
     */
    @Override
    public String generateResponseWithModel(String prompt, String modelName, Double customTemperature) {
        return doGenerateResponse(prompt, modelName, customTemperature != null ? customTemperature : 0.1, true);
    }

    /**
     * 使用默认模型生成AI响应（禁用深度思考）
     * 适用于智能搜索等不需要深度推理的场景，可提升响应速度
     *
     * @param prompt 输入提示词
     * @return AI生成的响应文本
     */
    @Override
    public String generateResponseWithoutThinking(String prompt) {
        return doGenerateResponse(prompt, model, temperature, false);
    }

    /**
     * 执行AI响应生成的核心逻辑
     * 调用硅基流动Chat Completions API
     *
     * @param prompt           输入提示词
     * @param modelName        模型名称
     * @param temperatureValue 温度参数
     * @return AI生成的响应文本
     */
    private String doGenerateResponse(String prompt, String modelName, Double temperatureValue) {
        return doGenerateResponse(prompt, modelName, temperatureValue, true);
    }

    /**
     * 执行AI响应生成的核心逻辑
     * 调用硅基流动Chat Completions API
     *
     * @param prompt           输入提示词
     * @param modelName        模型名称
     * @param temperatureValue 温度参数
     * @param enableThinking   是否启用深度思考（GLM-5等模型支持）
     * @return AI生成的响应文本
     */
    private String doGenerateResponse(String prompt, String modelName, Double temperatureValue, boolean enableThinking) {
        try {
            String url = baseUrl + "/chat/completions";

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(message);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("temperature", temperatureValue);
            requestBody.put("max_tokens", maxTokens);
            if ("deepseek".equals(provider)) {
                requestBody.put("thinking", Map.of("type",
                        enableThinking && thinkingEnabled ? "enabled" : "disabled"));
            } else {
                requestBody.put("enable_thinking", enableThinking && thinkingEnabled);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null && response.containsKey("choices")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> firstChoice = choices.get(0);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> messageObj = (Map<String, Object>) firstChoice.get("message");
                    if (messageObj != null) {
                        return (String) messageObj.get("content");
                    }
                }
            }
            log.warn("AI API返回格式异常: {}", response);
            return null;
        } catch (Exception e) {
            log.error("调用AI API失败: {}", e.getMessage(), e);
            throw new RuntimeException("调用AI服务失败: " + e.getMessage());
        }
    }

    /**
     * 生成文本的向量嵌入
     * 调用硅基流动Embeddings API，用于语义搜索和相似度计算
     *
     * @param text 输入文本
     * @return 向量嵌入数组
     */
    @Override
    public float[] generateEmbedding(String text) {
        if (!embeddingEnabled) {
            log.debug("当前AI配置未启用向量嵌入");
            return null;
        }
        try {
            String url = baseUrl + "/embeddings";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModel);
            requestBody.put("input", text);
            requestBody.put("encoding_format", "float");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            if (response != null && response.containsKey("data")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");
                if (!dataList.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    List<Number> embeddingList = (List<Number>) dataList.get(0).get("embedding");
                    float[] embedding = new float[embeddingList.size()];
                    for (int i = 0; i < embeddingList.size(); i++) {
                        embedding[i] = embeddingList.get(i).floatValue();
                    }
                    log.debug("生成embedding成功，维度: {}", embedding.length);
                    return embedding;
                }
            }
            log.warn("硅基流动Embedding API返回格式异常: {}", response);
            return null;
        } catch (Exception e) {
            log.error("调用硅基流动Embedding API失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成向量失败: " + e.getMessage());
        }
    }

    /**
     * 检查AI服务是否可用
     * 通过调用模型列表接口验证服务连通性
     *
     * @return 服务是否可用
     */
    @Override
    public boolean isServiceAvailable() {
        try {
            String url = baseUrl + "/models";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            return true;
        } catch (Exception e) {
            log.error("AI服务不可用: {}", e.getMessage());
            return false;
        }
    }
}
