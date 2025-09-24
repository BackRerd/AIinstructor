import axios from 'axios'

const instance = axios.create({
    baseURL: 'https://frp-boy.com:55166/api/v1',
    timeout: 60000
})

// 这是普通非流式请求的示例
export const sendChatMessage = (payload) => {
    return instance.post('/chats_openai/10ff38a296b911f094fe0242ac120003/chat/completions', payload)
}
