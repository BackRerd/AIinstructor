package lnjz.backrer.aiinstructor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lnjz.backrer.aiinstructor.entity.ChatSessions;
import lnjz.backrer.aiinstructor.entity.Result;
import lnjz.backrer.aiinstructor.service.ChatSessionsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@RestController
@RequestMapping("/api/chat-sessions")
public class ChatSessionController {

    @Autowired
    private ChatSessionsService chatSessionService;

    /**
     * 创建新的聊天会话
     * @param chatSession 会话实体
     * @return 创建结果
     */
    @PostMapping
    public Result<ChatSessions> create(@RequestBody ChatSessions chatSession) {
        boolean result = chatSessionService.save(chatSession);
        if (result) {
            return Result.success("创建会话成功", chatSession);
        }
        return Result.error("创建会话失败");
    }

    /**
     * 更新聊天会话
     * @param sessionId 会话ID
     * @param chatSession 会话实体
     * @return 更新结果
     */
    @PutMapping("/{sessionId}")
    public Result<ChatSessions> update(@PathVariable String sessionId, @RequestBody ChatSessions chatSession) {
        chatSession.setSessionId(sessionId);
        boolean result = chatSessionService.updateById(chatSession);
        if (result) {
            return Result.success("更新会话成功", chatSession);
        }
        return Result.error("更新会话失败");
    }

    /**
     * 获取单个会话详情
     * @param sessionId 会话ID
     * @return 会话详情
     */
    @GetMapping("/{sessionId}")
    public Result<ChatSessions> getById(@PathVariable String sessionId) {
        ChatSessions chatSession = chatSessionService.getById(sessionId);
        if (chatSession != null) {
            return Result.success("获取会话成功", chatSession);
        }
        return Result.error("会话不存在", 404);
    }

    /**
     * 删除会话（逻辑删除）
     * @param sessionId 会话ID
     * @return 删除结果
     */
    @DeleteMapping("/{sessionId}")
    public Result<Void> delete(@PathVariable String sessionId) {
        boolean result = chatSessionService.removeById(sessionId);
        if (result) {
            return Result.success("删除会话成功");
        }
        return Result.error("删除会话失败");
    }

    /**
     * 分页查询所有会话（支持多条件筛选）
     * @param userId 用户ID（可选）
     * @param modelId 模型ID（可选）
     * @param title 标题关键词（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<Page<ChatSessions>> pageQuery(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer modelId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Date startTime,
            @RequestParam(required = false) Date endTime,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        QueryWrapper<ChatSessions> queryWrapper = new QueryWrapper<>();

        // 构建查询条件
        if (userId != null && !userId.isEmpty()) {
            queryWrapper.eq("user_id", userId);
        }
        if (modelId != null) {
            queryWrapper.eq("model_id", modelId);
        }
        if (title != null && !title.isEmpty()) {
            queryWrapper.like("session_title", title);
        }
        if (startTime != null && endTime != null) {
            queryWrapper.between("last_active_at", startTime, endTime);
        } else if (startTime != null) {
            queryWrapper.ge("last_active_at", startTime);
        } else if (endTime != null) {
            queryWrapper.le("last_active_at", endTime);
        }

        // 排除已删除的，按最后活跃时间降序
//        queryWrapper.eq("is_deleted", 0)
//                .orderByDesc("last_active_at");

        Page<ChatSessions> page = new Page<>(pageNum, pageSize);
        Page<ChatSessions> result = chatSessionService.page(page, queryWrapper);

        return Result.success("查询成功", result);
    }

    /**
     * 更新会话标题
     * @param sessionId 会话ID
     * @param title 新标题
     * @return 更新结果
     */
    @PatchMapping("/{sessionId}/title")
    public Result<Void> updateTitle(
            @PathVariable String sessionId,
            @RequestParam String title) {

        ChatSessions chatSession = new ChatSessions();
        chatSession.setSessionId(sessionId);
        chatSession.setSessionTitle(title);

        boolean result = chatSessionService.updateById(chatSession);
        if (result) {
            return Result.success("更新标题成功");
        }
        return Result.error("更新标题失败");
    }

    /**
     * 更新最后活跃时间和最后消息
     * @param sessionId 会话ID
     * @param lastMessage 最后一条消息
     * @return 更新结果
     */
    @PatchMapping("/{sessionId}/activity")
    public Result<Void> updateActivity(
            @PathVariable String sessionId,
            @RequestParam(required = false) String lastMessage) {

        ChatSessions chatSession = new ChatSessions();
        chatSession.setSessionId(sessionId);
        chatSession.setLastActiveAt(new java.util.Date());

        if (lastMessage != null) {
            chatSession.setLastMessage(lastMessage);
        }

        boolean result = chatSessionService.updateById(chatSession);
        if (result) {
            return Result.success("更新活跃时间成功");
        }
        return Result.error("更新活跃时间失败");
    }

    /**
     * 增加消息计数
     * @param sessionId 会话ID
     * @return 更新结果
     */
    @PostMapping("/{sessionId}/increment-message-count")
    public Result<Void> incrementMessageCount(@PathVariable String sessionId) {
        ChatSessions chatSession = chatSessionService.getById(sessionId);
        if (chatSession == null) {
            return Result.error("会话不存在", 404);
        }

        chatSession.setMessageCount(chatSession.getMessageCount() + 1);
        boolean result = chatSessionService.updateById(chatSession);
        if (result) {
            return Result.success("增加消息计数成功");
        }
        return Result.error("增加消息计数失败");
    }
}