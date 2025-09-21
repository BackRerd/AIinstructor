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
          <el-select v-model="currentModel" placeholder="选择AI模型" @change="handleModelChange" size="small">
            <el-option v-for="item in modelOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>

        <el-scrollbar class="session-list">
          <div v-for="session in sessionList" :key="session.id" @click="switchSession(session.id)"
               :class="['session-item', { 'active': currentSessionId === session.id }]">
            <div class="session-item-content">
              <span class="session-model-badge">{{ getModelLabel(session.model) }}</span>
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
            <el-tag type="info">{{ getModelLabel(currentSession.model) }}</el-tag>
            <span> 对话中</span>
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

// 响应式数据与状态管理
const userInput = ref('')
const isLoading = ref(false)
const currentModel = ref('deepseek-v3')
const currentSessionId = ref(null)
const sessionList = ref([])
const scrollbarRef = ref(null)
const isMobile = ref(window.innerWidth < 768)
const showSidebar = ref(false)

// 可切换的AI模型配置
const modelOptions = [
  { value: 'deepseek-v3', label: 'DeepSeek-V3 (通用对话)' },
  { value: '敬请期待', label: '制作ing' },
  // { value: 'qwen-max', label: 'Qwen-Max (通义千问)' },
  // { value: 'gpt-4', label: 'GPT-4 (OpenAI)' }
]

// 当前会话 computed
const currentSession = computed(() => {
  return sessionList.value.find(session => session.id === currentSessionId.value)
})

// 创建新会话
const createNewSession = () => {
  const newSessionId = Date.now().toString()
  const newSession = {
    id: newSessionId,
    model: currentModel.value,
    messages: [
      { id: 1, role: 'assistant', content: `你好！我是${getModelLabel(currentModel.value)}，有什么可以帮你的？` }
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
const handleModelChange = (newModel) => {
  if (currentSession.value) {
    currentSession.value.model = newModel
    currentSession.value.messages.push({
      id: Date.now(),
      role: 'assistant',
      content: `已切换至${getModelLabel(newModel)}，继续为您服务。`
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
    const simulatedResponse = await simulateAIResponse(userMessage, currentSession.value.model)
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

// 模拟AI回复
const simulateAIResponse = (userMessage, model) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const responses = {
        'deepseek-v3': `我是DeepSeek-V3，你刚才说："${userMessage}"。这是一个很棒的话题！我很乐意深入探讨它。`,
        'deepseek-r1': `（DeepSeek-R1推理中）关于"${userMessage}"，我认为可以从逻辑推理的角度分析几个关键点：`,
        'qwen-max': `👋 你好呀！我是Qwen-Max。你提到的"${userMessage}"很有意思呢，让我来帮你看看~`,
        'gpt-4': `As GPT-4, I understand you said: "${userMessage}". This is a complex topic that involves multiple considerations.`
      }
      resolve(responses[model] || `谢谢你的消息："${userMessage}"。我还在学习中，请多多指教。`)
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
const getModelLabel = (modelValue) => {
  const model = modelOptions.find(item => item.value === modelValue)
  return model ? model.label : modelValue
}

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

onMounted(() => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('touchstart', handleTouchStart)
  window.addEventListener('touchend', handleTouchEnd)

  if (sessionList.value.length === 0) {
    createNewSession()
  }
})
</script>

<style scoped>
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