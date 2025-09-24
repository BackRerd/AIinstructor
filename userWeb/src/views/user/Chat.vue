<template>
  <div :class="['app-container', { 'mobile-layout': isMobile }]">
    <!-- 移动端头部 -->
    <div v-if="isMobile" class="mobile-header">
      <el-button :icon="Menu" @click="showSidebar = !showSidebar" text />
      <h3>AI对话助手</h3>
      <el-button :icon="Plus" @click="createNewSession" text />
    </div>

    <el-container class="main-layout" direction="horizontal">
      <!-- 会话列表侧边栏 -->
      <el-aside :width="isMobile ? '100%' : '280px'" :class="['session-sidebar', { 'active': showSidebar || !isMobile }]">
        <div class="sidebar-header" v-if="!isMobile">
          <h2>AI对话助手</h2>
          <el-button type="primary" @click="createNewSession" :icon="Plus" plain>新对话</el-button>
        </div>

        <div class="model-selector">
          <span>选择模型:</span>
          <el-select v-model="currentModel" placeholder="选择AI模型" @change="handleModelChange" size="small" :loading="loadingModels">
            <el-option v-for="item in modelOptions" :key="item.modelId" :label="item.modelName" :value="item.modelId">
              <span style="float: left">{{ item.modelName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.modelType }}</span>
            </el-option>
          </el-select>
        </div>

        <el-scrollbar class="session-list">
          <div v-for="session in sessionList" :key="session.id" @click="switchSession(session.id)"
               :class="['session-item', { 'active': currentSessionId === session.id }]">
            <div class="session-item-content">
              <span class="session-model-badge">{{ getModelName(session.modelId) }}</span>
              <p class="session-preview">{{ session.preview }}</p>
            </div>
            <el-button size="small" :icon="Delete" @click.stop="removeSession(session.id)" text />
          </div>
        </el-scrollbar>
      </el-aside>

      <!-- 主聊天区域 -->
      <el-main class="chat-main" v-if="currentSession">
        <div class="chat-header">
          <div class="current-model-info">
            <span>与 </span>
            <el-tag type="info">{{ getModelName(currentSession.modelId) }}</el-tag>
            <span> 对话中</span>
          </div>
          <div class="model-description" v-if="getModelDescription(currentSession.modelId)">
            {{ getModelDescription(currentSession.modelId) }}
          </div>
        </div>

        <el-scrollbar class="message-scrollbar" ref="scrollbarRef" @scroll="handleScroll">
          <div class="message-list">
            <div v-for="message in currentSession.messages" :key="message.id" class="message-wrapper"
                 :class="message.role">
              <div class="message-bubble">
                <div class="message-avatar">
                  <el-avatar v-if="message.role === 'user'">用</el-avatar>
                  <el-avatar v-else style="background-color: #67c23a;">AI</el-avatar>
                </div>
                <div class="message-content">
                  <div class="message-text" v-html="renderMessageContent(message.content)"></div>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>

        <div class="input-area">
          <div class="input-box">
            <el-input v-model="userInput" :autosize="{ minRows: 1, maxRows: 4 }" type="textarea" placeholder="请输入您的问题..."
                      @keyup.enter.native="handleSend" :disabled="isLoading" />
            <el-button type="primary" @click="handleSend" :loading="isLoading" :disabled="!userInput.trim()" class="send-button">发送
            </el-button>
          </div>
          <div class="input-tips">Enter发送，Shift+Enter换行</div>
        </div>
      </el-main>
    </el-container>

    <!-- 移动端遮罩层 -->
    <div v-if="isMobile && showSidebar" class="mobile-mask" @click="showSidebar = false"></div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElScrollbar } from 'element-plus'
import { Plus, Delete, Menu } from '@element-plus/icons-vue'
import axios from "axios";
import { FetchModelEndpoints } from "@/api/user.js";

// 响应式数据与状态管理
const userInput = ref('')
const isLoading = ref(false)
const currentModel = ref(null) // 改为存储modelId
const currentSessionId = ref(null)
const sessionList = ref([])
const scrollbarRef = ref(null)
const isMobile = ref(window.innerWidth < 768)
const showSidebar = ref(false)
const loadingModels = ref(false)
const availableModels = ref([]) // 存储从接口获取的模型列表

// 获取模型列表
const loadModels = async () => {
  loadingModels.value = true
  try {
    const modelData = await FetchModelEndpoints()
    availableModels.value = modelData.records || []

    // 设置默认模型
    if (availableModels.value.length > 0) {
      currentModel.value = availableModels.value[0].modelId
    }
  } catch (error) {
    ElMessage.error('加载模型列表失败')
    console.error('加载模型失败:', error)

    // 设置默认模型选项
    availableModels.value = [
      { modelId: '模型加载失败！', modelName: '请检测网络！', modelDescription: '模型加载失败！', modelType: 'deepseek' }
    ]
    currentModel.value = 'deepseek-v3'
  } finally {
    loadingModels.value = false
  }
}

// 计算模型选项
const modelOptions = computed(() => {
  return availableModels.value.map(model => ({
    modelId: model.modelId,
    modelName: model.modelName,
    modelType: model.modelType,
    modelDescription: model.modelDescription
  }))
})

// 根据modelId获取模型名称
const getModelName = (modelId) => {
  const model = availableModels.value.find(m => m.modelId === modelId)
  return model ? model.modelName : '未知模型'
}
const getModelType = (modelId) => {
  const model = availableModels.value.find(m => m.modelId === modelId)
  return model ? model.modelType : '未知类型'
}
// 根据modelId获取模型描述
const getModelDescription = (modelId) => {
  const model = availableModels.value.find(m => m.modelId === modelId)
  return model ? model.modelDescription : ''
}
// 根据 modelId 获取模型端点配置
const getModelEndpoint = (modelId) => {
  const model = availableModels.value.find(m => m.modelId === modelId)
  return model ? model.endpointConfig : ''
}
// 根据 modelId 获取 API Token
const getModelApiToken = (modelId) => {
  const model = availableModels.value.find(m => m.modelId === modelId)
  return model ? model.apiToken : ''
}
// 根据 modelId 获取完整模型对象
const getModelById = (modelId) => {
  return availableModels.value.find(m => m.modelId === modelId) || null
}


// 当前会话 computed
const currentSession = computed(() => {
  return sessionList.value.find(session => session.id === currentSessionId.value)
})

// 创建新会话
const createNewSession = () => {
  if (!currentModel.value) return

  const newSessionId = Date.now().toString()
  const newSession = {
    id: newSessionId,
    modelId: currentModel.value,
    messages: [
      { id: 1, role: 'assistant', content: `你好！我是${getModelName(currentModel.value)}，${getModelDescription(currentModel.value) || '有什么可以帮你的？'}` }
    ],
    preview: '新对话',
    createdAt: new Date()
  }
  sessionList.value.unshift(newSession)
  currentSessionId.value = newSessionId
  if (isMobile.value) {
    showSidebar.value = false
  }
}

// 切换会话
const switchSession = (sessionId) => {
  currentSessionId.value = sessionId
  if (isMobile.value) {
    showSidebar.value = false
  }
}

// 删除会话
const removeSession = (sessionId) => {
  const index = sessionList.value.findIndex(session => session.id === sessionId)
  if (index !== -1) {
    sessionList.value.splice(index, 1)
    if (sessionList.value.length === 0) {
      createNewSession()
    } else if (currentSessionId.value === sessionId) {
      currentSessionId.value = sessionList.value[0].id
    }
  }
}

// 切换AI模型
const handleModelChange = (newModelId) => {
  console.log(getModelApiToken(newModelId))
  if (currentSession.value) {
    currentSession.value.modelId = newModelId
    currentSession.value.messages.push({
      id: Date.now(),
      role: 'assistant',
      content: `已切换至${getModelName(newModelId)}，${getModelDescription(newModelId) || '继续为您服务。'}`
    })
    updateSessionPreview(currentSession.value)

  }
}

// 发送消息
const handleSend = async () => {
  if (!userInput.value.trim() || isLoading.value) return

  const userMessage = userInput.value.trim()
  userInput.value = ''

  currentSession.value.messages.push({
    id: Date.now() + 1,
    role: 'user',
    content: userMessage
  })
  updateSessionPreview(currentSession.value)

  isLoading.value = true
  scrollToBottom()

  try {
    // 获取当前模型的配置信息
    const currentModelConfig = availableModels.value.find(m => m.modelId === currentSession.value.modelId)
    const simulatedResponse = await simulateAIResponse(userMessage, currentModelConfig)
    await typewriterEffect(simulatedResponse, currentSession.value)
  } catch (error) {
    currentSession.value.messages.push({
      id: Date.now() + 2,
      role: 'assistant',
      content: '抱歉，我暂时无法回应，请稍后再试。'
    })
    ElMessage.error('请求出错')
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

// 模拟AI回复（根据模型配置）
const simulateAIResponse = async (userMessage, modelConfig) => {
  if (!modelConfig) {
    return `谢谢你的消息："${userMessage}"。我还在学习中，请多多指教。`
  }

  // 如果有API配置，可以在这里进行真实API调用
  if (modelConfig.endpointConfig && modelConfig.apiToken) {
    try {
      // 示例：真实API调用
      const response = await axios.post(modelConfig.endpointConfig, {
        message: userMessage
      }, {
        headers: {
          'Authorization': modelConfig.apiToken,
          'Content-Type': 'application/json'
        }
      })
      return response.data.response || `我是${modelConfig.modelName}，已收到您的消息。`
    } catch (error) {
      console.error('API调用失败:', error)
      return `我是${modelConfig.modelName}，暂时无法处理您的请求。`
    }
  }

  // 模拟回复
  return new Promise((resolve) => {
    setTimeout(() => {
      const responses = {
        'ragflow': `我是${modelConfig.modelName}（RAGFlow专业模型），您刚才说："${userMessage}"。我会基于知识库为您提供专业解答。`,
        'deepseek': `我是${modelConfig.modelName}（DeepSeek模型），关于"${userMessage}"，我可以从多个角度为您分析：`,
        'default': `我是${modelConfig.modelName}，您提到："${userMessage}"。这是一个很好的问题，让我来帮您分析。`
      }

      resolve(responses[modelConfig.modelType] || responses.default)
    }, 1000)
  })
}

// 模拟打字机效果
const typewriterEffect = (text, session) => {
  return new Promise((resolve) => {
    const messageId = Date.now() + 3
    session.messages.push({
      id: messageId,
      role: 'assistant',
      content: ''
    })

    let index = 0
    const interval = setInterval(() => {
      if (index < text.length) {
        const message = session.messages.find(m => m.id === messageId)
        if (message) {
          message.content += text.charAt(index)
        }
        index++
        scrollToBottom()
      } else {
        clearInterval(interval)
        updateSessionPreview(session)
        resolve()
      }
    }, 30)
  })
}

// 工具函数
const updateSessionPreview = (session) => {
  const lastUserMessage = [...session.messages].reverse().find(msg => msg.role === 'user')
  if (lastUserMessage) {
    session.preview = lastUserMessage.content.length > 20 ?
        lastUserMessage.content.substring(0, 20) + '...' : lastUserMessage.content
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      const scrollbar = scrollbarRef.value
      scrollbar.scrollTo({
        top: scrollbar.wrapRef.scrollHeight,
        behavior: 'smooth'
      })
    }
  })
}

const handleScroll = ({ scrollTop, scrollHeight, clientHeight }) => {
  // 可以在这里实现滚动加载更多消息的逻辑
}

const renderMessageContent = (content) => {
  return content.replace(/\n/g, '<br>')
}

// 响应式布局处理
const handleResize = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    showSidebar.value = false
  }
}

// 触摸事件处理
const touchStartX = ref(0)
const handleTouchStart = (e) => {
  touchStartX.value = e.touches[0].clientX
}

const handleTouchEnd = (e) => {
  const touchEndX = e.changedTouches[0].clientX
  const diffX = touchEndX - touchStartX.value

  // 左滑显示侧边栏
  if (diffX < -50 && isMobile.value && !showSidebar.value) {
    showSidebar.value = true
  }

  // 右滑隐藏侧边栏
  if (diffX > 50 && isMobile.value && showSidebar.value) {
    showSidebar.value = false
  }
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('touchstart', handleTouchStart)
  window.addEventListener('touchend', handleTouchEnd)

  // 加载模型列表
  await loadModels()

  if (sessionList.value.length === 0 && currentModel.value) {
    createNewSession()
  }
})
</script>

<style scoped>
.model-description {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
  font-style: italic;
}
.app-container {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
}

.main-layout {
  height: 100%;
}

/* 移动端头部样式 */
.mobile-header {
  display: none;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  height: 50px;
  background-color: white;
  border-bottom: 1px solid #eaeaea;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.mobile-header h3 {
  margin: 0;
  font-size: 16px;
}

/* 会话侧边栏样式 */
.session-sidebar {
  border-right: 1px solid #eaeaea;
  display: flex;
  flex-direction: column;
  background-color: #f9fafb;
  transition: transform 0.3s ease;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #eaeaea;
  background-color: white;
}

.sidebar-header h2 {
  margin: 0 0 12px 0;
  font-size: 18px;
}

.model-selector {
  padding: 16px;
  background-color: white;
  border-bottom: 1px solid #eaeaea;
}

.model-selector span {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.session-list {
  flex: 1;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #edf2f7;
}

.session-item.active {
  background-color: #ebf5ff;
  border-right: 3px solid #409eff;
}

.session-item-content {
  flex: 1;
  overflow: hidden;
}

.session-model-badge {
  display: inline-block;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  background-color: #909399;
  color: white;
  margin-bottom: 4px;
}

.session-preview {
  margin: 0;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 主聊天区域样式 */
.chat-main {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0;
  background-color: white;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid #eaeaea;
  background-color: white;
}

.current-model-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.message-scrollbar {
  flex: 1;
  overflow-y: auto;
}

.message-list {
  padding: 16px;
}

.message-wrapper {
  margin-bottom: 20px;
}

.message-bubble {
  display: flex;
  max-width: 80%;
}

.message-wrapper.user .message-bubble {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
}

.message-content {
  flex: 1;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
}

.message-wrapper.user .message-text {
  background-color: #409eff;
  color: white;
}

.message-wrapper.assistant .message-text {
  background-color: #f5f7fa;
  color: #303133;
  border: 1px solid #eaeaea;
}

.input-area {
  padding: 16px;
  border-top: 1px solid #eaeaea;
  background-color: white;
}

.input-box {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.send-button {
  align-self: flex-end;
}

.input-tips {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

/* 移动端遮罩层 */
.mobile-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 999;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .mobile-header {
    display: flex;
  }

  .session-sidebar {
    position: absolute;
    width: 100% !important;
    height: 100%;
    z-index: 1000;
    transform: translateX(-100%);
  }

  .session-sidebar.active {
    transform: translateX(0);
  }

  .message-bubble {
    max-width: 90% !important;
  }

  .input-box {
    flex-direction: column;
  }

  .send-button {
    align-self: flex-end;
    margin-top: 8px;
  }

  /* 扩大移动端点击区域 */
  .session-item,
  .el-button,
  .el-input {
    min-height: 44px; /* 移动端推荐的最小点击区域 */
  }

  /* 移除hover效果 */
  .session-item:hover {
    background-color: inherit;
  }
}

/* 平板设备适配 */
@media screen and (min-width: 769px) and (max-width: 1024px) {
  .session-sidebar {
    width: 240px !important;
  }

  .message-bubble {
    max-width: 85%;
  }
}
</style>