<template>
  <div class="ai-chat-container">
    <!-- 移动端侧边栏切换按钮 -->
    <div class="mobile-sidebar-toggle" v-if="isMobile" @click="showSidebar = !showSidebar">
      <el-icon><Menu /></el-icon>
      <span>智能体选择</span>
    </div>

    <!-- 侧边栏遮罩层 -->
    <div class="sidebar-overlay" v-if="isMobile && showSidebar" @click="showSidebar = false"></div>

    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'mobile-sidebar': isMobile, 'sidebar-open': showSidebar }">
      <!-- 移动端关闭按钮 -->
      <div class="sidebar-header" v-if="isMobile">
        <h3>智能体选择</h3>
        <el-icon class="close-icon" @click="showSidebar = false"><Close /></el-icon>
      </div>

      <!-- 模型选择区域 -->
      <div class="model-section">
        <h3 v-if="!isMobile">AI模型</h3>
        <div class="model-selector">
          <el-select
              v-model="selectedModelId"
              placeholder="请选择AI模型"
              :loading="loadingModels"
              @change="handleModelChange"
              clearable
          >
            <el-option
                v-for="model in models"
                :key="model.modelId"
                :label="model.modelName"
                :value="model.modelId"
            >
              <div class="model-option">
                <span class="model-name">{{ model.modelName }}</span>
                <span class="model-type">{{ model.modelType }}</span>
              </div>
            </el-option>
          </el-select>

          <el-button
              type="primary"
              :loading="loadingModels"
              @click="loadModels"
              icon="Refresh"
              class="refresh-btn"
          >
            {{ isMobile ? '' : '刷新' }}
          </el-button>
        </div>

        <!-- 简化的模型信息展示 -->
        <div v-if="currentModel" class="model-info-simple">
          <div class="model-badge">
            <el-tag :type="getModelTypeTag(currentModel.modelType)" size="small">
              {{ currentModel.modelType }}
            </el-tag>
            <span class="model-update-time">{{ formatRelativeTime(currentModel.createdAt) }}</span>
          </div>
          <p class="model-description" :title="currentModel.modelDescription">
            {{ truncateDescription(currentModel.modelDescription) }}
          </p>
        </div>
      </div>

       设置区域
      <div class="settings-section">
        <h3 v-if="!isMobile">设置</h3>
        <div class="setting-item">
          <el-checkbox v-model="useStream" :disabled="!selectedModelId">
            使用流式输出
          </el-checkbox>
        </div>

        <div class="action-buttons">
          <el-button plain @click="clearMessages" :disabled="!selectedModelId" size="small">
            清空对话
          </el-button>
          <el-button plain @click="exportConversation" :disabled="messages.length === 0" size="small">
            导出对话
          </el-button>
          <el-button plain @click="showRaw = !showRaw" size="small">
            {{ showRaw ? '隐藏' : '显示' }}原始数据
          </el-button>
        </div>
      </div>

      <!-- 移动端操作提示 -->
      <div class="mobile-hint" v-if="isMobile">
        <el-icon><InfoFilled /></el-icon>
        <span>点击外部区域关闭</span>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="main-chat-area" :class="{ 'mobile-full': isMobile && showSidebar }">
      <!-- 当前模型指示器（移动端） -->
      <div class="current-model-indicator" v-if="isMobile && currentModel" @click="showSidebar = true">
        <div class="indicator-content">
          <el-icon><Robot /></el-icon>
          <span class="model-name">{{ currentModel.modelName }}</span>
          <el-tag :type="getModelTypeTag(currentModel.modelType)" size="small">
            {{ currentModel.modelType }}
          </el-tag>
        </div>
        <el-icon><ArrowRight /></el-icon>
      </div>

      <!-- 消息展示区域 -->
      <div class="messages-container" ref="chatContainer" @scroll="handleScroll">
        <div v-if="messages.length === 0" class="empty-message">
          <div class="welcome-section">
            <h2>欢迎使用AI助手</h2>
            <p>请选择一个AI模型开始对话</p>
            <div class="suggestions">
              <div class="suggestion" @click="setExampleQuestion('政府奖学金申报条件有哪些？')">
                <el-icon><Document /></el-icon>
                <span>政府奖学金申报条件有哪些？</span>
              </div>
              <div class="suggestion" @click="setExampleQuestion('2025年政府奖学金分配名额有多少？')">
                <el-icon><ChatDotRound /></el-icon>
                <span>2025年政府奖学金分配名额有多少？</span>
              </div>
              <div class="suggestion" @click="setExampleQuestion('洗浴在哪里？')">
                <el-icon><Lightning /></el-icon>
                <span>洗浴在哪里？</span>
              </div>
            </div>
          </div>
        </div>

        <div v-for="(m, idx) in messages" :key="m.id || idx" class="message-item" :class="m.role">
          <div class="message-avatar">
            <div v-if="m.role === 'user'" class="user-avatar">
              <el-icon><User /></el-icon>
            </div>
            <div v-else class="ai-avatar">
              <el-icon><Robot /></el-icon>
            </div>
          </div>
          <div class="message-content-wrapper">
            <div class="message-header">
              <span class="message-sender">{{ labelOf(m) }}</span>
              <span class="message-time" v-if="m.timestamp">
                {{ formatTime(m.timestamp) }}
              </span>
            </div>

            <!-- 用户消息 -->
            <div v-if="m.role === 'user'" class="message-content">
              <div class="user-message">
                {{ m.content }}
              </div>
            </div>

            <!-- AI消息 -->
            <div v-else class="message-content">
              <div class="ai-response">
                {{ m.content }}

                <!-- 可折叠的思考过程 -->
                <div v-if="m.reasoningContent" class="thinking-section">
                  <div class="thinking-toggle" @click="toggleThinking(idx)">
                    <el-icon>
                      <CaretRight v-if="!m.showThinking" />
                      <CaretBottom v-else />
                    </el-icon>
                    <span>{{ m.showThinking ? '隐藏思考过程' : '查看思考过程' }}</span>
                  </div>

                  <transition name="thinking-fade">
                    <div v-if="m.showThinking" class="thinking-content">
                      {{ m.reasoningContent }}
                    </div>
                  </transition>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 流式生成预览 -->
        <div v-if="isStreaming" class="stream-preview message-item ai">
          <div class="message-avatar">
            <div class="ai-avatar">
              <el-icon><Robot /></el-icon>
            </div>
          </div>
          <div class="message-content-wrapper">
            <div class="message-header">
              <span class="message-sender">AI (生成中)</span>
              <div class="streaming-indicator">
                <span class="indicator-dot"></span>
                <span>正在生成</span>
              </div>
            </div>

            <div v-if="previewReasoning" class="message-content reasoning">
              <div class="reasoning-label">思考过程（实时）</div>
              {{ previewReasoning }}
            </div>

            <div v-if="previewAnswer" class="message-content">
              <div class="ai-response">
                {{ previewAnswer }}
              </div>
            </div>

            <!-- 进度指示 -->
            <div class="progress-indicator" v-if="isStreaming">
              <el-progress
                  :percentage="streamProgress"
                  :status="progressStatus"
                  :show-text="false"
                  stroke-width="2"
              />
              <span class="progress-text">{{ streamProgress }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <el-input
              v-model="userInput"
              placeholder="请输入问题..."
              @keyup.enter="sendMessage"
              :disabled="sending || !selectedModelId"
              clearable
              type="textarea"
              :autosize="{ minRows: 1, maxRows: 4 }"
              class="message-input"
          />
          <div class="input-actions">
            <el-button
                type="primary"
                @click="sendMessage"
                :loading="sending"
                :disabled="!userInput.trim() || !selectedModelId"
                class="send-button"
            >
              <el-icon v-if="!sending"><Promotion /></el-icon>
              {{ sending ? '发送中...' : isMobile ? '' : '发送' }}
            </el-button>
            <el-button
                type="danger"
                @click="stopStream"
                v-if="controller"
                :disabled="!selectedModelId"
                class="stop-button"
            >
              <el-icon><CloseBold /></el-icon>
              {{ isMobile ? '' : '停止' }}
            </el-button>
          </div>
        </div>
        <div class="input-footer">
          <div class="info-text" v-if="!isMobile">
<!--            使用 Fetch API 接收流式数据-->
            <el-tooltip content="浏览器端 Axios 不支持 response body 流式读取" placement="top">
              <el-icon><InfoFilled /></el-icon>
            </el-tooltip>
            <div class="char-count" :class="{ 'near-limit': userInput.length > 450 }">
              最大字数限制:{{ userInput.length }}/500
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- 原始数据展示 -->
    <el-drawer v-model="showRaw" title="原始流数据" size="50%">
      <div class="raw-data-content">
        <div class="raw-data-header">
          <el-button @click="copyRawData" type="primary">复制</el-button>
        </div>
        <pre>{{ rawLog }}</pre>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted, onUnmounted, watch, nextTick, onBeforeUnmount} from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import {
  InfoFilled, Refresh, User, Promotion, CloseBold,
  Document, ChatDotRound, Lightning, Menu, Close, ArrowRight,
  CaretRight, CaretBottom
} from '@element-plus/icons-vue'
import { FetchModelEndpoints, GetModelEndpoints } from "@/api/user.js"

// ====== 响应式状态 ======
const userInput = ref('')
const messages = reactive([])
const sending = ref(false)
const useStream = ref(true)
const controller = ref(null)
const isStreaming = ref(false)
const showRaw = ref(false)
const rawLog = ref('')
const streamProgress = ref(0)
const models = ref([])
const selectedModelId = ref(null)
const loadingModels = ref(false)
const currentModel = ref(null)
const showSidebar = ref(false)
const isMobile = ref(false)

// 新增响应式变量
const autoScroll = ref(true)
const chatContainer = ref(null)

// 流式处理相关状态
const thinkingActive = ref(true)
const tempThinkingContent = ref('')
const reasoningBuffer = ref('')
const answerBuffer = ref('')

// ====== 计算属性 ======
const endpoint = computed(() => currentModel.value ? currentModel.value.endpointConfig : '')
const token = computed(() => currentModel.value ? currentModel.value.apiToken.replace('Bearer ', '') : '')

const previewReasoning = computed(() => {
  if (thinkingActive.value) {
    const combined = (reasoningBuffer.value || '') + (tempThinkingContent.value || '')
    return combined ? combined.trim() : ''
  }
  return reasoningBuffer.value ? reasoningBuffer.value.trim() : ''
})

const previewAnswer = computed(() => {
  return answerBuffer.value ? answerBuffer.value.trim() || '思考中...' : ''
})

const progressStatus = computed(() => {
  if (streamProgress.value >= 90) return 'success'
  if (streamProgress.value >= 70) return 'warning'
  return 'primary'
})

// ====== 方法 ======
const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) {
    showSidebar.value = false
  }
}

const truncateDescription = (description) => {
  if (!description) return ''
  return description.length > 100 ? description.substring(0, 100) + '...' : description
}

const formatRelativeTime = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diffInHours = Math.floor((now - date) / (1000 * 60 * 60))

  if (diffInHours < 1) return '刚刚'
  if (diffInHours < 24) return `${diffInHours}小时前`
  return `${Math.floor(diffInHours / 24)}天前`
}

const getModelTypeTag = (type) => {
  const typeMap = { ragflow: 'success', ollama: 'warning', llama: 'primary', openai: 'info' }
  return typeMap[type] || 'info'
}

const formatDate = (dateString) => new Date(dateString).toLocaleString('zh-CN')

const labelOf = (m) => {
  const labels = { reasoning: 'AI（思考过程）', answer: 'AI', user: '用户', system: '系统' }
  return labels[m.type] || labels[m.role] || '消息'
}

const formatTime = (timestamp) => new Date(timestamp).toLocaleTimeString()

// 新增方法：切换思考过程显示/隐藏[6](@ref)
const toggleThinking = (index) => {
  if (messages[index].role === 'assistant') {
    messages[index].showThinking = !messages[index].showThinking
  }
}

// 新增方法：滚动到底部[5](@ref)
const scrollToBottom = () => {
  if (!autoScroll.value) return

  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

// 新增方法：处理滚动事件[5](@ref)
const handleScroll = () => {
  if (chatContainer.value) {
    const { scrollTop, scrollHeight, clientHeight } = chatContainer.value
    // 如果用户滚动到接近底部，恢复自动滚动
    autoScroll.value = (scrollHeight - scrollTop - clientHeight < 100)
  }
}

const loadModels = async () => {
  loadingModels.value = true
  try {
    const data = await FetchModelEndpoints(1, 100)
    models.value = data.records
    ElMessage.success(`加载了 ${models.value.length} 个模型`)
  } catch (error) {
    ElMessage.error('加载模型列表失败: ' + error.message)
  } finally {
    loadingModels.value = false
  }
}

const handleModelChange = async (modelId) => {
  if (!modelId) {
    currentModel.value = null
    clearMessages()
    return
  }
  try {
    const modelData = await GetModelEndpoints(modelId)
    currentModel.value = modelData
    clearMessages()
    ElMessage.success(`已切换到模型: ${modelData.modelName}`)
    // 移动端选择模型后自动关闭侧边栏
    if (isMobile.value) {
      showSidebar.value = false
    }
  } catch (error) {
    ElMessage.error('加载模型信息失败: ' + error.message)
    selectedModelId.value = null
  }
}

function setExampleQuestion(question) {
  if (!selectedModelId.value) {
    ElMessage.warning('请先选择一个AI模型')
    // 移动端自动打开侧边栏
    if (isMobile.value) {
      showSidebar.value = true
    }
    return
  }
  userInput.value = question
}

// 构建请求体（包含对话历史）[2,5](@ref)
const buildRequestBody = (userMsg) => {
  // 构建消息历史
  const messageHistory = messages
      .filter(m => m.role === 'user' || m.role === 'assistant')
      .map(m => ({
        role: m.role,
        content: m.content
      }))

  // 添加系统提示和当前用户消息
  const requestMessages = [
    { role: 'system', content: '你是一个乐于助人的AI助手。' },
    ...messageHistory,
    { role: 'user', content: userMsg }
  ]

  return {
    model: 'qwen3:8b',
    messages: requestMessages,
    stream: useStream.value
  }
}

async function sendMessage() {
  const inputText = userInput.value.trim()
  if (!inputText || sending.value || !currentModel.value) return

  // 添加用户消息
  messages.push({
    role: 'user',
    type: 'user',
    content: inputText,
    timestamp: Date.now(),
    id: Date.now() // 添加唯一ID
  })
  userInput.value = ''
  sending.value = true

  try {
    const requestBody = buildRequestBody(inputText)

    if (useStream.value) {
      await handleStreamRequest(requestBody)
    } else {
      await handleNonStreamRequest(requestBody)
    }
  } catch (error) {
    handleRequestError(error)
  } finally {
    sending.value = false
    scrollToBottom() // 发送完成后滚动到底部
  }
}

// 流式请求
async function handleStreamRequest(requestBody) {
  controller.value = new AbortController()
  isStreaming.value = true
  resetStreamState()

  try {
    const response = await fetch(endpoint.value, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token.value}`,
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(requestBody),
      signal: controller.value.signal
    })

    if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    if (!response.body) throw new Error('服务器没有返回可读的响应体')

    await processStreamResponse(response)
  } catch (error) {
    if (error.name !== 'AbortError') throw error
  }
}

// 非流式请求
async function handleNonStreamRequest(requestBody) {
  try {
    const response = await fetch(endpoint.value, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token.value}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(requestBody)
    })

    if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    const data = await response.json()
    processNonStreamResponse(data)
  } catch (error) {
    throw error
  }
}

// 流式响应处理
async function processStreamResponse(response) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let receivedChunks = 0

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      receivedChunks++
      streamProgress.value = Math.min(95, Math.floor((receivedChunks / 50) * 100))

      const chunkText = decoder.decode(value, { stream: true })
      rawLog.value += chunkText
      buffer += chunkText

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (!line.trim()) continue
        const processedLine = processStreamLine(line)
        if (processedLine === '[DONE]') { streamProgress.value = 100; break }
      }
    }
  } finally {
    finalizeStreamResponse()
  }
}

function processStreamLine(line) {
  if (line.startsWith('data:')) {
    const dataContent = line.slice(5).trim()
    if (dataContent === '[DONE]' || dataContent === 'DONE') return '[DONE]'
    try {
      const parsedData = JSON.parse(dataContent)
      processStreamChunk(parsedData)
      return 'processed'
    } catch (error) {
      console.warn('JSON解析失败:', error, '原始数据:', dataContent)
      return 'error'
    }
  }
  return 'skip'
}

function processStreamChunk(chunk) {
  const choices = chunk.choices || []
  choices.forEach(choice => {
    const delta = choice.delta || {}
    const hasContent = Object.prototype.hasOwnProperty.call(delta, 'content')
    const contentVal = hasContent ? delta.content : undefined
    const reasoningVal = delta.reasoning_content

    if (thinkingActive.value) {
      if (reasoningVal) reasoningBuffer.value += reasoningVal
      if (hasContent && contentVal != null) tempThinkingContent.value += contentVal
      if (hasContent && contentVal === null && (reasoningVal || reasoningBuffer.value)) {
        thinkingActive.value = false
        if (tempThinkingContent.value) {
          reasoningBuffer.value += tempThinkingContent.value
          tempThinkingContent.value = ''
        }
      }
    } else {
      if (hasContent && contentVal != null) answerBuffer.value += contentVal
      if (reasoningVal) reasoningBuffer.value += reasoningVal
    }
  })
}

function finalizeStreamResponse() {
  if (thinkingActive.value && tempThinkingContent.value) reasoningBuffer.value += tempThinkingContent.value
  thinkingActive.value = false
  reasoningBuffer.value = reasoningBuffer.value.trim()
  answerBuffer.value = answerBuffer.value.trim()

  if (reasoningBuffer.value) {
    messages.push({
      role: 'assistant',
      type: 'reasoning',
      content: answerBuffer.value,
      reasoningContent: reasoningBuffer.value, // 存储思考内容
      showThinking: false, // 默认隐藏
      timestamp: Date.now(),
      id: Date.now()
    })
  } else if (answerBuffer.value) {
    messages.push({
      role: 'assistant',
      type: 'answer',
      content: answerBuffer.value,
      timestamp: Date.now(),
      id: Date.now()
    })
  }

  isStreaming.value = false
  controller.value = null
  streamProgress.value = 0
}

function processNonStreamResponse(data) {
  try {
    const choices = data.choices || []
    let reasoningContent = ''
    let answerContent = ''

    choices.forEach(choice => {
      if (choice.message) {
        reasoningContent += choice.message.reasoning_content || ''
        answerContent += choice.message.content || ''
      }
    })

    if (reasoningContent) {
      messages.push({
        role: 'assistant',
        type: 'reasoning',
        content: answerContent,
        reasoningContent: reasoningContent,
        showThinking: false,
        timestamp: Date.now(),
        id: Date.now()
      })
    } else {
      messages.push({
        role: 'assistant',
        type: 'answer',
        content: answerContent || JSON.stringify(data, null, 2),
        timestamp: Date.now(),
        id: Date.now()
      })
    }
  } catch (error) {
    messages.push({
      role: 'assistant',
      type: 'answer',
      content: `解析错误: ${error.message}\n原始响应: ${JSON.stringify(data, null, 2)}`,
      timestamp: Date.now(),
      id: Date.now()
    })
  }
}

function stopStream() { if (controller.value) { controller.value.abort(); ElMessage.info('生成已停止') } }

function clearMessages() {
  messages.splice(0, messages.length);
  rawLog.value = '';
  resetStreamState();
  ElMessage.success('对话已清空')
}

function resetStreamState() {
  thinkingActive.value = true;
  tempThinkingContent.value = '';
  reasoningBuffer.value = '';
  answerBuffer.value = ''
}

function copyRawData() { navigator.clipboard.writeText(rawLog.value); ElMessage.success('原始数据已复制到剪贴板') }

function exportConversation() {
  const conversation = messages.map(m => `[${formatTime(m.timestamp)}] ${labelOf(m)}: ${m.content}`).join('\n\n')
  const blob = new Blob([conversation], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `conversation-${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

function handleRequestError(error) {
  console.error('请求错误:', error)
  if (error.name === 'AbortError') ElMessage.info('请求已取消')
  else if (error.message.includes('HTTP')) ElMessage.error(`请求失败: ${error.message}`)
  else if (error.message.includes('network')) ElMessage.error('网络连接失败，请检查网络设置')
  else ElMessage.error(`发生错误: ${error.message}`)

  messages.push({
    role: 'system',
    type: 'system',
    content: `错误: ${error.message}`,
    timestamp: Date.now(),
    id: Date.now()
  })
}

// ====== 生命周期 ======
onMounted(() => {
  loadModels()
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile)
})

onUnmounted(() => { if (controller.value) controller.value.abort() })

watch(userInput, (newValue) => { if (newValue.length > 500) ElNotification.warning({ title: '输入过长', message: '输入内容超过500字符，建议精简问题' }) })

// 监听消息变化，自动滚动[5](@ref)
watch(() => messages.length, () => {
  scrollToBottom()
})
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  height: 100vh;
  background-color: #f5f5f7;
  position: relative;
}

/* 移动端侧边栏切换按钮 */
.mobile-sidebar-toggle {
  position: fixed;
  top: 45px;
  left: 16px;
  z-index: 1001;
  background: white;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

/* 侧边栏遮罩层 */
.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1998;
}

.sidebar {
  width: 300px;
  background-color: #ffffff;
  border-right: 1px solid #e0e0e0;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-y: auto;
  transition: transform 0.3s ease;
  z-index: 1999;
  position: relative;
}

/* 移动端侧边栏样式 */
.sidebar.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  transform: translateX(-100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar.mobile-sidebar.sidebar-open {
  transform: translateX(0);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e0e0e0;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
}

.close-icon {
  cursor: pointer;
  color: #666;
  size: 18px;
}

.sidebar h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #1a1a1a;
  font-weight: 600;
}

.model-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.model-selector .el-select {
  flex: 1;
}

.refresh-btn {
  flex-shrink: 0;
}

.model-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.model-name {
  font-weight: 600;
}

.model-type {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color-light);
  padding: 2px 6px;
  border-radius: 4px;
}

/* 简化的模型信息 */
.model-info-simple {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #10a37f;
}

.model-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

-time {
  font-size: 12px;
  color: #999;
}

.model-description {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.settings-section {
  margin-top: auto;
}

.setting-item {
  margin-bottom: 16px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f0f7ff;
  border-radius: 6px;
  font-size: 12px;
  color: #1890ff;
  margin-top: auto;
}

.main-chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: filter 0.3s ease;
}

.main-chat-area.mobile-full {
  filter: blur(2px);
  pointer-events: none;
}

/* 当前模型指示器（移动端） */
.current-model-indicator {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e0e0e0;
  cursor: pointer;
}

.indicator-content {
  display:flex;
  align-items: center;
  gap: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-message {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.welcome-section {
  text-align: center;
  max-width: 500px;
}

.welcome-section h2 {
  margin-bottom: 8px;
  color: #1a1a1a;
}

.welcome-section p {
  color: #666;
  margin-bottom: 24px;
}

.suggestions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: all 0.2s;
}

.suggestion:hover {
  background: #f9f9f9;
  border-color: #d0d0d0;
}

.suggestion .el-icon {
  color: #666;
}

.message-item {
  display: flex;
  gap: 12px;
  max-width: 100%;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.user .message-content-wrapper {
  align-items: flex-end;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar {
  background-color: #10a37f;
  color: white;
}

.ai-avatar {
  background-color: #6e6e80;
  color: white;
}

.message-content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.message-sender {
  font-weight: 600;
  font-size: 14px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-content {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.user .message-content {
  background-color: #10a37f;
  color: white;
  border-top-right-radius: 2px;
}

.ai .message-content {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-top-left-radius: 2px;
}

/* 新增思考过程样式 */
.thinking-section {
  margin-top: 12px;
  border-top: 1px dashed #e0e0e0;
  padding-top: 8px;
}

.thinking-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  user-select: none;
}

.thinking-toggle:hover {
  color: #1890ff;
}

.thinking-content {
  margin-top: 8px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.5;
  color: #666;
  white-space: pre-wrap;
}

.thinking-fade-enter-active,
.thinking-fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.thinking-fade-enter-from,
.thinking-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.reasoning {
  background-color: #f9f9f9 !important;
  border: 1px dashed #d0d0d0 !important;
  font-size: 14px;
  color: #666;
  padding: 12px 16px;
  border-radius: 8px;
  margin-top: 8px;
  position: relative;
}

.reasoning-label {
  font-weight: 600;
  margin-bottom: 4px;
  color: #888;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.reasoning-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  color: #666;
  font-size: 12px;
  margin-bottom: 8px;
}

.reasoning-toggle:hover {
  color: #1890ff;
}

.reasoning-content {
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.5;
  color: #666;
  white-space: pre-wrap;
  margin-top: 8px;
  border-left: 3px solid #10a37f;
}

.thinking-fade-enter-active,
.thinking-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.thinking-fade-enter-from,
.thinking-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 流式响应时的特殊样式 */
.reasoning.streaming {
  border-color: #10a37f;
  background-color: #f0f7ff;
}

.reasoning .streaming-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #10a37f;
  margin-top: 8px;
}

.reasoning .indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #10a37f;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .reasoning {
    padding: 10px 12px;
    font-size: 13px;
  }

  .reasoning-content {
    padding: 10px;
    font-size: 13px;
  }
}

/* 暗色主题支持 */
@media (prefers-color-scheme: dark) {
  .reasoning {
    background-color: #2d2d2d !important;
    border-color: #555 !important;
    color: #ccc;
  }

  .reasoning-content {
    background-color: #3d3d3d;
    color: #ddd;
    border-left-color: #10a37f;
  }

  .reasoning-label {
    color: #aaa;
  }

  .reasoning.streaming {
    background-color: #2d3d4d;
    border-color: #10a37f;
  }
}
</style>