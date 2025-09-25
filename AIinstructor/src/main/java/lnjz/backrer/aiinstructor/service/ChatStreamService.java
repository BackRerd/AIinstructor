package lnjz.backrer.aiinstructor.service;

import lnjz.backrer.aiinstructor.entity.ChatSessions;
import lnjz.backrer.aiinstructor.entity.ModelEndpoints;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j   // <--- 加上 Lombok 的日志注解
@Service
public class ChatStreamService {

    private final WebClient insecureWebClient;
    @Autowired
    private ChatRecordsService chatRecordsService;

    public ChatStreamService(WebClient insecureWebClient) {
        this.insecureWebClient = insecureWebClient;
    }

    public Flux<String> streamChat(String userMsg, ModelEndpoints modelEndpoints, String chatSessions) {
        // 使用 Map 构建 JSON 结构
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelEndpoints.getModelName());

        // 构建消息数组
        List<Map<String, String>> messages = new ArrayList<>();

        // 添加会话历史
        if (chatSessions != null) {
            chatRecordsService.getChatRecordsByRecords(chatSessions).forEach(chatRecords ->{
                        messages.add(Map.of("role", "user", "content", chatRecords.getMessageContent()));
                        messages.add(Map.of("role", chatRecords.getMessageType().toString(), "content", chatRecords.getResponseContent()));
                    }
            );
        }else {
            messages.add(Map.of("role", "system", "content", "你是一个建校学院的AI智能问答助手。"));
        }

        messages.add(Map.of("role", "user", "content", userMsg));
        requestBody.put("messages", messages);
        requestBody.put("stream", true);

        log.info("当前使用模型为: {}", modelEndpoints.getModelName());
        return insecureWebClient.post()
                .uri(modelEndpoints.getEndpointConfig())
                .header("Authorization", modelEndpoints.getApiToken())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody) // WebClient 会自动序列化 Map 为 JSON
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .map(dataBuffer -> StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString())
                .flatMap(chunk -> Flux.fromArray(chunk.split("\n")))
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.replaceFirst("^data:", "").trim())
                .takeUntil(line -> "[DONE]".equals(line))
                .map(this::processChunk);
    }

    private String processChunk(String raw) {
        if ("[DONE]".equals(raw)) return raw;
        return raw;
    }

}
