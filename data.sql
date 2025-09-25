CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID,主键',
                       uid VARCHAR(36) NOT NULL UNIQUE COMMENT '业务唯一标识符(例如UUID)',
                       user_name VARCHAR(50) NOT NULL COMMENT '用户名称',
                       password VARCHAR(255) NOT NULL COMMENT '密码(存储加密后的哈希值)',
                       phone_number VARCHAR(20) NOT NULL COMMENT '手机号',
                       email VARCHAR(100) UNIQUE COMMENT '邮箱',
                       wechat_id VARCHAR(50) UNIQUE COMMENT '微信号',
                       student_id VARCHAR(20) UNIQUE COMMENT '学号',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                       INDEX idx_uid (uid),
                       INDEX idx_phone (phone_number),
                       INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE model_endpoints (
                                 model_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '模型唯一标识（自增）',
                                 model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
                                 model_description TEXT NULL COMMENT '模型描述',
                                 model_type ENUM('ollama', 'llama', 'ragflow') NOT NULL COMMENT '模型类型',
                                 class_name VARCHAR(100) NULL COMMENT '所属班级名称',
                                 teacher_name VARCHAR(100) NULL COMMENT '所属教师名称',
                                 endpoint_config TEXT NOT NULL COMMENT '端点配置（包含url、api_url、api_id等信息）',
                                 api_token VARCHAR(500) NULL COMMENT '接口token',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                                 INDEX idx_model_type (model_type),
                                 INDEX idx_class_name (class_name),
                                 INDEX idx_teacher_name (teacher_name),
                                 INDEX idx_created_at (created_at)
) COMMENT='模型端点配置表';


CREATE TABLE chat_records (
                              record_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '聊天记录ID（自增）',
                              user_id VARCHAR(100) NOT NULL COMMENT '用户ID',
                              session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
                              model_id INT NOT NULL COMMENT '使用的模型ID',
                              message_type ENUM('user', 'assistant', 'system') NOT NULL COMMENT '消息类型',
                              message_content TEXT NOT NULL COMMENT '消息内容',
                              message_tokens INT NULL COMMENT '消息token数量',
                              response_content TEXT NULL COMMENT '回复内容（仅assistant类型有值）',
                              response_tokens INT NULL COMMENT '回复token数量',
                              total_tokens INT NULL COMMENT '总token数量',
                              message_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '消息时间戳',
                              is_deleted TINYINT DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                              INDEX idx_user_id (user_id),
                              INDEX idx_session_id (session_id),
                              INDEX idx_model_id (model_id),
                              INDEX idx_message_timestamp (message_timestamp),
                              INDEX idx_user_session (user_id, session_id)
) COMMENT '聊天记录表';
CREATE TABLE chat_sessions (
                               session_id      VARCHAR(100) PRIMARY KEY COMMENT '会话ID（唯一标识）',
                               user_id         VARCHAR(100) NOT NULL COMMENT '用户ID',
                               session_title   VARCHAR(255) NULL COMMENT '会话标题（可由用户自定义或自动生成）',
                               model_id        INT NOT NULL COMMENT '使用的模型ID',
                               last_message    TEXT NULL COMMENT '最近一条消息内容（可用于会话列表预览）',
                               message_count   INT DEFAULT 0 COMMENT '消息数量',
                               last_active_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最近活跃时间',
                               is_deleted      TINYINT DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
                               created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

                               INDEX idx_user_id (user_id),
                               INDEX idx_model_id (model_id),
                               INDEX idx_last_active (last_active_at)
) COMMENT '聊天会话索引表（Tab级）';
