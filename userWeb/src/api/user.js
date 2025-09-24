import axios from "axios";

const instance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 60000
})

export const FetchModelEndpoints = async (pageNum = 1, pageSize = 10, keyword = "") => {
    try {
        const q = `?pageNum=${pageNum}&pageSize=${pageSize}` + (keyword ? `&keyword=${encodeURIComponent(keyword)}` : "");
        const response = await instance.get(`/api/model-endpoints/page${q}`);
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
