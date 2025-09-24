<template>
  <el-container style="height: 100vh;">
    <!-- 顶部栏 -->
    <el-header height="60px" class="header">
      <div class="header-left">
        <h3>AI 对话平台</h3>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="login">登录</el-button>
      </div>
    </el-header>

    <el-container>
      <!-- 左侧模型选择 -->
      <el-aside width="200px" class="aside">
        <h4>选择模型</h4>
        <el-select v-model="selectedModel" placeholder="请选择模型" style="width: 100%;">
          <el-option
              v-for="model in models"
              :key="model.value"
              :label="model.label"
              :value="model.value"
          />
        </el-select>
      </el-aside>

      <!-- 聊天区域 -->
      <el-main class="chat-main">
        <!-- 状态显示 -->
        <el-alert :title="status" :type="statusType" :closable="false" class="status-alert" />

        <!-- 聊天消息显示 -->
        <div class="chat-window" ref="chatWindow">
          <div v-for="(msg, index) in messages" :key="index" :class="['chat-message', msg.role]">
            <span class="chat-role">{{ msg.role === 'user' ? '我' : 'AI' }}:</span>
            <span class="chat-content">{{ msg.content }}</span>
          </div>
          <p v-if="messages.length === 0" class="empty-text">暂无消息</p>
        </div>

        <!-- 输入框 -->
        <div class="chat-input">
          <el-input
              v-model="inputMessage"
              placeholder="请输入消息"
              @keyup.enter.native="sendMessage"
              clearable
          />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </el-main>
    </el-container>

    <!-- 底部栏 -->
    <el-footer height="40px" class="footer">
      © 2025 AI 对话平台
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'

// 模型选择
const selectedModel = ref('')
const models = ref([
  { label: 'GPT-4', value: 'gpt-4' },
  { label: 'GPT-3.5', value: 'gpt-3.5' },
  { label: '自定义模型', value: 'custom' },
])

// 聊天数据
const inputMessage = ref('')
const messages = ref([])

// SSE 流式处理
const isConnected = ref(false)
const eventSource = ref(null)
const chatWindow = ref(null)

// 状态显示
const status = computed(() => (isConnected.value ? 'SSE连接中' : '空闲'))
const statusType = computed(() => (isConnected.value ? 'success' : 'info'))

// 登录
const login = () => {
  ElMessage.info('点击了登录按钮')
}

// 发送消息（用户发送后触发 SSE）
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  messages.value.push({ role: 'user', content: inputMessage.value })
  const msg = inputMessage.value
  inputMessage.value = ''

  scrollToBottom()

  // 建立 SSE 连接
  connectSSE(msg)
}

// SSE 连接
const connectSSE = (msg) => {
  if (eventSource.value) closeSSE()

  try {
    eventSource.value = new EventSource(`http://localhost:8080/chat/stream?msg=${encodeURIComponent(msg)}&model=${selectedModel.value || 'default'}`)
    isConnected.value = true

    let isAir = true

    eventSource.value.onmessage = (e) => {
      if (e.data === '[DONE]') {
        closeSSE()
        scrollToBottom()
      } else {
        const data = JSON.parse(e.data).choices[0].delta
        let text = ''
        if (data.reasoning_content) {
          isAir = true
        }
        if (isAir) {
          text = data.reasoning_content ? "回答数据:" + data.content : "思考数据:" + data.content
          isAir = false
        } else {
          text = data.content
        }

        // 将 AI 消息追加到 messages
        if (messages.value.length === 0 || messages.value[messages.value.length - 1].role !== 'ai') {
          messages.value.push({ role: 'ai', content: text })
        } else {
          messages.value[messages.value.length - 1].content += text
        }

        scrollToBottom()
      }
    }

    eventSource.value.onerror = (error) => {
      isConnected.value = false
      ElMessage.error('SSE连接出错')
      console.error('SSE错误:', error)
    }
  } catch (error) {
    ElMessage.error('创建SSE连接失败')
    console.error('创建EventSource失败:', error)
  }
}

// 关闭 SSE
const closeSSE = () => {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
    isConnected.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatWindow.value) {
      chatWindow.value.scrollTop = chatWindow.value.scrollHeight
    }
  })
}

// 组件卸载自动关闭 SSE
onUnmounted(() => {
  closeSSE()
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  background-color: #409eff;
  color: white;
}
.header h3 {
  margin: 0;
}
.aside {
  padding: 20px;
  background-color: #f5f7fa;
}
.chat-main {
  display: flex;
  flex-direction: column;
  padding: 10px;
  background-color: #fff;
}
.status-alert {
  margin-bottom: 10px;
}
.chat-window {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #f9f9f9;
  max-height: 400px;
}
.chat-message {
  margin-bottom: 10px;
}
.chat-message.user .chat-role {
  font-weight: bold;
  color: #409eff;
}
.chat-message.ai .chat-role {
  font-weight: bold;
  color: #67c23a;
}
.chat-input {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}
.empty-text {
  color: #999;
  text-align: center;
  padding: 20px;
}
.footer {
  text-align: center;
  line-height: 40px;
  background-color: #f5f7fa;
}
</style>
