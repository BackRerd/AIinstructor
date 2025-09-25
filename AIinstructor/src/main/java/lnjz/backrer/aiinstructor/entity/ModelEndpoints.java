package lnjz.backrer.aiinstructor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 模型端点配置表
 * @TableName model_endpoints
 */
@TableName(value ="model_endpoints")
@Data
public class ModelEndpoints {
    /**
     * 模型唯一标识（自增）
     */
    @TableId(type = IdType.AUTO)
    private Integer modelId;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型描述
     */
    private String modelDescription;

    /**
     * 模型类型
     */
    private Object modelType;

    /**
     * 所属班级名称
     */
    private String className;

    /**
     * 所属教师名称
     */
    private String teacherName;

    /**
     * 端点配置（包含url、api_url、api_id等信息）
     */
    private String endpointConfig;

    /**
     * 接口token
     */
    private String apiToken;

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
        ModelEndpoints other = (ModelEndpoints) that;
        return (this.getModelId() == null ? other.getModelId() == null : this.getModelId().equals(other.getModelId()))
            && (this.getModelName() == null ? other.getModelName() == null : this.getModelName().equals(other.getModelName()))
            && (this.getModelDescription() == null ? other.getModelDescription() == null : this.getModelDescription().equals(other.getModelDescription()))
            && (this.getModelType() == null ? other.getModelType() == null : this.getModelType().equals(other.getModelType()))
            && (this.getClassName() == null ? other.getClassName() == null : this.getClassName().equals(other.getClassName()))
            && (this.getTeacherName() == null ? other.getTeacherName() == null : this.getTeacherName().equals(other.getTeacherName()))
            && (this.getEndpointConfig() == null ? other.getEndpointConfig() == null : this.getEndpointConfig().equals(other.getEndpointConfig()))
            && (this.getApiToken() == null ? other.getApiToken() == null : this.getApiToken().equals(other.getApiToken()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getModelId() == null) ? 0 : getModelId().hashCode());
        result = prime * result + ((getModelName() == null) ? 0 : getModelName().hashCode());
        result = prime * result + ((getModelDescription() == null) ? 0 : getModelDescription().hashCode());
        result = prime * result + ((getModelType() == null) ? 0 : getModelType().hashCode());
        result = prime * result + ((getClassName() == null) ? 0 : getClassName().hashCode());
        result = prime * result + ((getTeacherName() == null) ? 0 : getTeacherName().hashCode());
        result = prime * result + ((getEndpointConfig() == null) ? 0 : getEndpointConfig().hashCode());
        result = prime * result + ((getApiToken() == null) ? 0 : getApiToken().hashCode());
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
        sb.append(", modelId=").append(modelId);
        sb.append(", modelName=").append(modelName);
        sb.append(", modelDescription=").append(modelDescription);
        sb.append(", modelType=").append(modelType);
        sb.append(", className=").append(className);
        sb.append(", teacherName=").append(teacherName);
        sb.append(", endpointConfig=").append(endpointConfig);
        sb.append(", apiToken=").append(apiToken);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}