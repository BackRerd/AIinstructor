package lnjz.backrer.aiinstructor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lnjz.backrer.aiinstructor.entity.ChatRecords;
import lnjz.backrer.aiinstructor.entity.Result;
import lnjz.backrer.aiinstructor.service.ChatRecordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat-records")
public class ChatRecordsController {

    @Autowired
    private ChatRecordsService chatRecordsService;

    /**
     * 新增聊天记录
     */
    @PostMapping
    public Result<?> addChatRecord(@RequestBody ChatRecords chatRecord) {
        try {
            boolean success = chatRecordsService.save(chatRecord);
            if (success) {
                return Result.success("新增成功", chatRecord);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            return Result.error("新增异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID删除聊天记录（逻辑删除）
     */
    @DeleteMapping("/{recordId}")
    public Result<?> deleteChatRecord(@PathVariable Long recordId) {
        try {
            ChatRecords chatRecord = new ChatRecords();
            chatRecord.setRecordId(recordId);
            chatRecord.setIsDeleted(1); // 逻辑删除
            boolean success = chatRecordsService.updateById(chatRecord);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败，记录不存在");
            }
        } catch (Exception e) {
            return Result.error("删除异常: " + e.getMessage());
        }
    }

    /**
     * 批量删除聊天记录
     */
    @DeleteMapping("/batch")
    public Result<?> batchDeleteChatRecords(@RequestBody List<Long> recordIds) {
        try {
            // 批量逻辑删除
            List<ChatRecords> records = chatRecordsService.listByIds(recordIds);
            for (ChatRecords record : records) {
                record.setIsDeleted(1);
            }
            boolean success = chatRecordsService.updateBatchById(records);
            if (success) {
                return Result.success("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            return Result.error("批量删除异常: " + e.getMessage());
        }
    }

    /**
     * 更新聊天记录
     */
    @PutMapping
    public Result<?> updateChatRecord(@RequestBody ChatRecords chatRecord) {
        try {
            if (chatRecord.getRecordId() == null) {
                return Result.error("ID不能为空");
            }
            boolean success = chatRecordsService.updateById(chatRecord);
            if (success) {
                return Result.success("更新成功", chatRecord);
            } else {
                return Result.error("更新失败，记录不存在");
            }
        } catch (Exception e) {
            return Result.error("更新异常: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询聊天记录
     */
    @GetMapping("/{recordId}")
    public Result<?> getChatRecordById(@PathVariable Long recordId) {
        try {
            ChatRecords chatRecord = chatRecordsService.getById(recordId);
            if (chatRecord != null && chatRecord.getIsDeleted() == 0) {
                return Result.success("查询成功", chatRecord);
            } else {
                return Result.error("记录不存在或已被删除");
            }
        } catch (Exception e) {
            return Result.error("查询异常: " + e.getMessage());
        }
    }

    /**
     * 分页查询聊天记录
     */
    @GetMapping("/page")
    public Result<?> getChatRecordsPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Integer modelId,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        try {
            Page<ChatRecords> page = new Page<>(pageNum, pageSize);
            QueryWrapper<ChatRecords> queryWrapper = new QueryWrapper<>();

            // 只查询未删除的记录
            queryWrapper.eq("is_deleted", 0);

            // 添加查询条件
            if (userId != null && !userId.trim().isEmpty()) {
                queryWrapper.eq("user_id", userId.trim());
            }
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                queryWrapper.eq("session_id", sessionId.trim());
            }
            if (modelId != null) {
                queryWrapper.eq("model_id", modelId);
            }
            if (messageType != null && !messageType.trim().isEmpty()) {
                queryWrapper.eq("message_type", messageType.trim());
            }
            if (startTime != null && !startTime.trim().isEmpty()) {
                queryWrapper.ge("message_timestamp", startTime);
            }
            if (endTime != null && !endTime.trim().isEmpty()) {
                queryWrapper.le("message_timestamp", endTime);
            }

            // 按消息时间倒序排列
            queryWrapper.orderByDesc("message_timestamp");

            IPage<ChatRecords> result = chatRecordsService.page(page, queryWrapper);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            return Result.error("分页查询异常: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID和会话ID查询聊天记录
     */
    @GetMapping("/user-session")
    public Result<?> getChatRecordsByUserAndSession(
            @RequestParam String userId,
            @RequestParam String sessionId) {
        try {
            QueryWrapper<ChatRecords> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("session_id", sessionId)
                    .eq("is_deleted", 0)
                    .orderByAsc("message_timestamp"); // 按时间正序排列，便于查看对话流程

            List<ChatRecords> list = chatRecordsService.list(queryWrapper);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询异常: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询所有会话列表
     */
    @GetMapping("/user-sessions/{userId}")
    public Result<?> getUserSessions(@PathVariable String userId) {
        try {
            QueryWrapper<ChatRecords> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("DISTINCT session_id")
                    .eq("user_id", userId)
                    .eq("is_deleted", 0)
                    .orderByDesc("message_timestamp");

            List<ChatRecords> sessions = chatRecordsService.list(queryWrapper);
            return Result.success("查询成功", sessions);
        } catch (Exception e) {
            return Result.error("查询异常: " + e.getMessage());
        }
    }

    /**
     * 统计用户token使用情况
     */
    @GetMapping("/token-stats/{userId}")
    public Result<?> getTokenStatistics(@PathVariable String userId) {
        try {
            QueryWrapper<ChatRecords> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("SUM(total_tokens) as totalTokens")
                    .eq("user_id", userId)
                    .eq("is_deleted", 0);

            // 这里需要自定义SQL查询，简单的Wrapper可能无法满足复杂统计
            // 建议在Service层实现复杂的统计逻辑
            return Result.success("统计功能需要在Service层实现");
        } catch (Exception e) {
            return Result.error("统计异常: " + e.getMessage());
        }
    }
}