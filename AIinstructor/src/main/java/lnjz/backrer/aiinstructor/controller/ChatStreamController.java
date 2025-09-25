package lnjz.backrer.aiinstructor.controller;

import lnjz.backrer.aiinstructor.service.ChatStreamService;
import lnjz.backrer.aiinstructor.service.ModelEndpointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatStreamController {

    private final ChatStreamService chatStreamService;
    @Autowired
    private ModelEndpointsService modelEndpointsService;

    public ChatStreamController(ChatStreamService chatStreamService) {
        this.chatStreamService = chatStreamService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String msg,@RequestParam Integer modelId,@RequestParam String sessionId) {
        log.info("[ChatStreamController] >>> 收到前端请求, msg={}", msg);
        return chatStreamService.streamChat(msg,modelEndpointsService.getById(modelId),sessionId)
                .doOnSubscribe(sub -> log.info("[ChatStreamController] >>> SSE 建立连接"))
                .doOnError(err -> log.error("[ChatStreamController] !!! SSE 异常", err))
                .doOnComplete(() -> log.info("[ChatStreamController] >>> SSE 完成推送"));
    }
}
