package lnjz.backrer.aiinstructor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 聊天会话索引表（Tab级）
 * @TableName chat_sessions
 */
@TableName(value ="chat_sessions")
@Data
public class ChatSessions {
    /**
     * 会话ID（唯一标识）
     */
    @TableId
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话标题（可由用户自定义或自动生成）
     */
    private String sessionTitle;

    /**
     * 使用的模型ID
     */
    private Integer modelId;

    /**
     * 最近一条消息内容（可用于会话列表预览）
     */
    private String lastMessage;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 最近活跃时间
     */
    private Date lastActiveAt;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        ChatSessions other = (ChatSessions) that;
        return (this.getSessionId() == null ? other.getSessionId() == null : this.getSessionId().equals(other.getSessionId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getSessionTitle() == null ? other.getSessionTitle() == null : this.getSessionTitle().equals(other.getSessionTitle()))
            && (this.getModelId() == null ? other.getModelId() == null : this.getModelId().equals(other.getModelId()))
            && (this.getLastMessage() == null ? other.getLastMessage() == null : this.getLastMessage().equals(other.getLastMessage()))
            && (this.getMessageCount() == null ? other.getMessageCount() == null : this.getMessageCount().equals(other.getMessageCount()))
            && (this.getLastActiveAt() == null ? other.getLastActiveAt() == null : this.getLastActiveAt().equals(other.getLastActiveAt()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSessionId() == null) ? 0 : getSessionId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getSessionTitle() == null) ? 0 : getSessionTitle().hashCode());
        result = prime * result + ((getModelId() == null) ? 0 : getModelId().hashCode());
        result = prime * result + ((getLastMessage() == null) ? 0 : getLastMessage().hashCode());
        result = prime * result + ((getMessageCount() == null) ? 0 : getMessageCount().hashCode());
        result = prime * result + ((getLastActiveAt() == null) ? 0 : getLastActiveAt().hashCode());
        result = prime * result + ((getIsDeleted() == null) ? 0 : getIsDeleted().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", sessionId=").append(sessionId);
        sb.append(", userId=").append(userId);
        sb.append(", sessionTitle=").append(sessionTitle);
        sb.append(", modelId=").append(modelId);
        sb.append(", lastMessage=").append(lastMessage);
        sb.append(", messageCount=").append(messageCount);
        sb.append(", lastActiveAt=").append(lastActiveAt);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}