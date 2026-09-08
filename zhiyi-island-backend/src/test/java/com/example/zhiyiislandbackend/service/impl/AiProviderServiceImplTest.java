package com.example.zhiyiislandbackend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiProviderServiceImplTest {
    private AiProviderServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new AiProviderServiceImpl();
        ReflectionTestUtils.setField(service, "baseUrl", "https://api.deepseek.com");
        ReflectionTestUtils.setField(service, "apiKey", "test-key");
        ReflectionTestUtils.setField(service, "model", "deepseek-v4-flash");
        ReflectionTestUtils.setField(service, "temperature", 0.7);
        ReflectionTestUtils.setField(service, "maxTokens", 4096);
        ReflectionTestUtils.setField(service, "provider", "deepseek");
        ReflectionTestUtils.setField(service, "thinkingEnabled", false);
        ReflectionTestUtils.setField(service, "embeddingEnabled", false);
        server = MockRestServiceServer.bindTo(
                (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate")).build();
    }

    @Test
    void deepSeekUsesItsThinkingParameterAndConfiguredModel() {
        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("deepseek-v4-flash"))
                .andExpect(jsonPath("$.thinking.type").value("disabled"))
                .andExpect(jsonPath("$.enable_thinking").doesNotExist())
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}", MediaType.APPLICATION_JSON));
        assertEquals("ok", service.generateResponse("synthetic test"));
        server.verify();
    }

    @Test
    void siliconFlowRetainsItsExistingThinkingParameter() {
        ReflectionTestUtils.setField(service, "provider", "siliconflow");
        ReflectionTestUtils.setField(service, "thinkingEnabled", true);
        server.expect(requestTo("https://api.deepseek.com/chat/completions"))
                .andExpect(jsonPath("$.enable_thinking").value(true))
                .andExpect(jsonPath("$.thinking").doesNotExist())
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}", MediaType.APPLICATION_JSON));
        assertEquals("ok", service.generateResponse("synthetic test"));
        server.verify();
    }

    @Test
    void disabledEmbeddingsDoNotSendAnHttpRequest() {
        assertNull(service.generateEmbedding("synthetic test"));
        server.verify();
    }

    @Test
    void enabledEmbeddingsStillParseVectors() {
        ReflectionTestUtils.setField(service, "embeddingEnabled", true);
        ReflectionTestUtils.setField(service, "embeddingModel", "test-embedding");
        server.expect(requestTo("https://api.deepseek.com/embeddings"))
                .andExpect(jsonPath("$.model").value("test-embedding"))
                .andRespond(withSuccess("{\"data\":[{\"embedding\":[0.1,0.2]}]}", MediaType.APPLICATION_JSON));
        assertArrayEquals(new float[]{0.1f, 0.2f}, service.generateEmbedding("synthetic test"));
        server.verify();
    }
}