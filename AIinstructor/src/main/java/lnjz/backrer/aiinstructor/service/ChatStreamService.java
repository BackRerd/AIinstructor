package lnjz.backrer.aiinstructor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@Slf4j   // <--- 加上 Lombok 的日志注解
@Service
public class ChatStreamService {

    private final WebClient insecureWebClient;

    public ChatStreamService(WebClient insecureWebClient) {
        this.insecureWebClient = insecureWebClient;
    }

    public Flux<String> streamChat(String userMsg) {
        String body = """
        {
          "model": "qwen3:8b",
          "messages": [
            {"role": "system", "content": "你是一个乐于助人的AI助手。"},
            {"role": "user", "content": "%s"}
          ],
          "stream": true
        }
    """.formatted(userMsg);

        return insecureWebClient.post()
                .uri("https://frp-boy.com:55166/api/v1/chats_openai/10ff38a296b911f094fe0242ac120003/chat/completions")
                .header("Authorization", "Bearer ragflow-A4ODdlOGUyNGU1NTExZjBhZTVlMDI0Mm")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .map(dataBuffer -> StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString())
                .flatMap(chunk -> Flux.fromArray(chunk.split("\n")))
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.replaceFirst("^data:", "").trim())
                .takeUntil(line -> "[DONE]".equals(line))
                // 🔹 这里处理数据
                .map(this::processChunk);
//                .doOnNext(line -> log.info("[ChatStreamService] <<< 处理后发送前端: {}", line));
    }

    // 业务处理函数
    private String processChunk(String raw) {
        if ("[DONE]".equals(raw)) return raw;
        return raw;
    }

}
