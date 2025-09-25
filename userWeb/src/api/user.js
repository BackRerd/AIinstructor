import axios from "axios";

const instance = axios.create({
    baseURL: 'http://localhost:8080/',
    timeout: 60000
})
instance.interceptors.request.use(
    (config) => {
        // 从 localStorage 或其他地方获取 token
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');

        // 如果 token 存在，添加到请求头
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const FetchModelEndpoints = async (pageNum = 1, pageSize = 10, keyword = "") => {
    try {
        const q = `?pageNum=${pageNum}&pageSize=${pageSize}` + (keyword ? `&keyword=${encodeURIComponent(keyword)}` : "");
        const response = await instance.get(`/api/model-endpoints/page${q}`);
        // console.log(response.data);
        return response.data.data; // { records: [...], total, current, pages, size }
    } catch (error) {
        console.error("FetchModelEndpoints 请求失败：", error);
        throw error;
    }
};
export const GetModelEndpoints = async (modelId = 1) => {
    try {
        const response = await instance.get(`/api/model-endpoints/${modelId}`);
        // 返回 response.data.data（按你示例）
        console.log(response.data.data)
        return response.data.data;
    } catch (error) {
        console.error("GetModelEndpoints 请求失败：", error);
        throw error;
    }
};

// 获取聊天会话分页数据
export const FetchChatSessions = async (pageNum = 1, pageSize = 10, userId = null, modelId = null, title = null, startTime = null, endTime = null) => {
    try {
        let queryParams = `?pageNum=${pageNum}&pageSize=${pageSize}`;
        
        if (userId) queryParams += `&userId=${encodeURIComponent(userId)}`;
        if (modelId) queryParams += `&modelId=${modelId}`;
        if (title) queryParams += `&title=${encodeURIComponent(title)}`;
        if (startTime) queryParams += `&startTime=${encodeURIComponent(startTime)}`;
        if (endTime) queryParams += `&endTime=${encodeURIComponent(endTime)}`;
        
        const response = await instance.get(`/api/chat-sessions/page${queryParams}`);
        return response.data.data; // { records: [...], total, current, pages, size }
    } catch (error) {
        console.error("FetchChatSessions 请求失败：", error);
        throw error;
    }
};

// 删除聊天会话
export const DeleteChatSession = async (sessionId) => {
    try {
        console.log("调用删除API，sessionId:", sessionId);
        const response = await instance.delete(`/api/chat-sessions/${sessionId}`);
        console.log("删除API响应:", response.data);
        return response.data;
    } catch (error) {
        console.error("DeleteChatSession 请求失败：", error);
        throw error;
    }
};

// 更新会话标题
export const UpdateSessionTitle = async (sessionId, title) => {
    try {
        const response = await instance.patch(`/api/chat-sessions/${sessionId}/title?title=${encodeURIComponent(title)}`);
        return response.data;
    } catch (error) {
        console.error("UpdateSessionTitle 请求失败：", error);
        throw error;
    }
};

// 更新会话活跃时间
export const UpdateSessionActivity = async (sessionId, lastMessage = null) => {
    try {
        let url = `/api/chat-sessions/${sessionId}/activity`;
        if (lastMessage) {
            url += `?lastMessage=${encodeURIComponent(lastMessage)}`;
        }
        const response = await instance.patch(url);
        return response.data;
    } catch (error) {
        console.error("UpdateSessionActivity 请求失败：", error);
        throw error;
    }
};

// 创建新的聊天会话
export const CreateChatSession = async (sessionData) => {
    try {
        const response = await instance.post('/api/chat-sessions', sessionData);
        return response.data;
    } catch (error) {
        console.error("CreateChatSession 请求失败：", error);
        throw error;
    }
};

// 获取会话详情
export const GetChatSession = async (sessionId) => {
    try {
        const response = await instance.get(`/api/chat-sessions/${sessionId}`);
        return response.data;
    } catch (error) {
        console.error("GetChatSession 请求失败：", error);
        throw error;
    }
};

// 增加消息计数
export const IncrementMessageCount = async (sessionId) => {
    try {
        const response = await instance.post(`/api/chat-sessions/${sessionId}/increment-message-count`);
        return response.data;
    } catch (error) {
        console.error("IncrementMessageCount 请求失败：", error);
        throw error;
    }
};
/**
 * 插入聊天记录
 * @param {string} userId - 用户ID
 * @param {string} sessionId - 会话ID
 * @param {number} modelId - 模型ID
 * @param {string} messagesType - 消息类型
 * @param {string} messageContent - 消息内容
 * @param {string} responseContent - 回复内容
 * @param {number} messageTokens - 消息token数量（可选）
 * @param {number} responseTokens - 回复token数量（可选）
 * @param {number} totalTokens - 总token数量（可选）
 * @returns {Promise<Object>} 插入结果
 */
export const InsertRecords = async (
    userId,
    sessionId,
    modelId,
    messagesType,
    messageContent,
    responseContent,
    messageTokens = 0,
    responseTokens = 0,
    totalTokens = 0
) => {
    try {
        // 构造请求数据
        const requestData = {
            userId: userId,
            sessionId: sessionId,
            modelId: modelId,
            messageType: messagesType,
            messageContent: messageContent,
            messageTokens: messageTokens,
            responseContent: responseContent,
            responseTokens: responseTokens,
            totalTokens: totalTokens,
            messageTimestamp: new Date().toISOString() // 自动生成当前时间戳
        };

        // 发送请求
        const response = await instance.post(`/api/chat-records`, requestData);

        // 检查响应是否成功
        if (response.data && response.data.success) {
            console.log("聊天记录插入成功:", response.data.message);
            return response.data;
        } else {
            throw new Error(response.data?.message || "插入失败");
        }
    } catch (error) {
        console.error("InsertRecords 请求失败:", error);

        // 根据错误类型提供更详细的错误信息
        if (error.response) {
            // 服务器返回了错误状态码
            const errorMessage = error.response.data?.message || `服务器错误: ${error.response.status}`;
            console.error("服务器响应错误:", errorMessage);
            throw new Error(errorMessage);
        } else if (error.request) {
            // 请求已发出但没有收到响应
            console.error("网络错误: 无法连接到服务器");
            throw new Error("网络连接失败，请检查网络设置");
        } else {
            // 其他错误
            throw new Error(error.message || "未知错误");
        }
    }
};

// 根据会话ID获取聊天记录
export const GetChatRecordsBySession = async (sessionId) => {
    try {
        const response = await instance.get(`/api/chat-records/getRecordBySession?sessionId=${sessionId}`);
        return response.data;
    } catch (error) {
        console.error("GetChatRecordsBySession 请求失败：", error);
        throw error;
    }
};