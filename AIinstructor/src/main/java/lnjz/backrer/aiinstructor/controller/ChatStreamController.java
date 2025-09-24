package lnjz.backrer.aiinstructor.controller;

import lnjz.backrer.aiinstructor.service.ChatStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatStreamController {

    private final ChatStreamService chatStreamService;

    public ChatStreamController(ChatStreamService chatStreamService) {
        this.chatStreamService = chatStreamService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String msg) {
        log.info("[ChatStreamController] >>> 收到前端请求, msg={}", msg);
        return chatStreamService.streamChat(msg)
                .doOnSubscribe(sub -> log.info("[ChatStreamController] >>> SSE 建立连接"))
                .doOnError(err -> log.error("[ChatStreamController] !!! SSE 异常", err))
                .doOnComplete(() -> log.info("[ChatStreamController] >>> SSE 完成推送"));
    }
}
