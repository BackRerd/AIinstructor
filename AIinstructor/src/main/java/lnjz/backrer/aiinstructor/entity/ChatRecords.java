package lnjz.backrer.aiinstructor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 聊天记录表
 * @TableName chat_records
 */
@TableName(value ="chat_records")
@Data
public class ChatRecords {
    /**
     * 聊天记录ID（自增）
     */
    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 使用的模型ID
     */
    private Integer modelId;

    /**
     * 消息类型
     */
    private Object messageType;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息token数量
     */
    private Integer messageTokens;

    /**
     * 回复内容（仅assistant类型有值）
     */
    private String responseContent;

    /**
     * 回复token数量
     */
    private Integer responseTokens;

    /**
     * 总token数量
     */
    private Integer totalTokens;

    /**
     * 消息时间戳
     */
    private Date messageTimestamp;

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
        ChatRecords other = (ChatRecords) that;
        return (this.getRecordId() == null ? other.getRecordId() == null : this.getRecordId().equals(other.getRecordId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getSessionId() == null ? other.getSessionId() == null : this.getSessionId().equals(other.getSessionId()))
            && (this.getModelId() == null ? other.getModelId() == null : this.getModelId().equals(other.getModelId()))
            && (this.getMessageType() == null ? other.getMessageType() == null : this.getMessageType().equals(other.getMessageType()))
            && (this.getMessageContent() == null ? other.getMessageContent() == null : this.getMessageContent().equals(other.getMessageContent()))
            && (this.getMessageTokens() == null ? other.getMessageTokens() == null : this.getMessageTokens().equals(other.getMessageTokens()))
            && (this.getResponseContent() == null ? other.getResponseContent() == null : this.getResponseContent().equals(other.getResponseContent()))
            && (this.getResponseTokens() == null ? other.getResponseTokens() == null : this.getResponseTokens().equals(other.getResponseTokens()))
            && (this.getTotalTokens() == null ? other.getTotalTokens() == null : this.getTotalTokens().equals(other.getTotalTokens()))
            && (this.getMessageTimestamp() == null ? other.getMessageTimestamp() == null : this.getMessageTimestamp().equals(other.getMessageTimestamp()))
            && (this.getIsDeleted() == null ? other.getIsDeleted() == null : this.getIsDeleted().equals(other.getIsDeleted()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRecordId() == null) ? 0 : getRecordId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getSessionId() == null) ? 0 : getSessionId().hashCode());
        result = prime * result + ((getModelId() == null) ? 0 : getModelId().hashCode());
        result = prime * result + ((getMessageType() == null) ? 0 : getMessageType().hashCode());
        result = prime * result + ((getMessageContent() == null) ? 0 : getMessageContent().hashCode());
        result = prime * result + ((getMessageTokens() == null) ? 0 : getMessageTokens().hashCode());
        result = prime * result + ((getResponseContent() == null) ? 0 : getResponseContent().hashCode());
        result = prime * result + ((getResponseTokens() == null) ? 0 : getResponseTokens().hashCode());
        result = prime * result + ((getTotalTokens() == null) ? 0 : getTotalTokens().hashCode());
        result = prime * result + ((getMessageTimestamp() == null) ? 0 : getMessageTimestamp().hashCode());
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
        sb.append(", recordId=").append(recordId);
        sb.append(", userId=").append(userId);
        sb.append(", sessionId=").append(sessionId);
        sb.append(", modelId=").append(modelId);
        sb.append(", messageType=").append(messageType);
        sb.append(", messageContent=").append(messageContent);
        sb.append(", messageTokens=").append(messageTokens);
        sb.append(", responseContent=").append(responseContent);
        sb.append(", responseTokens=").append(responseTokens);
        sb.append(", totalTokens=").append(totalTokens);
        sb.append(", messageTimestamp=").append(messageTimestamp);
        sb.append(", isDeleted=").append(isDeleted);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}