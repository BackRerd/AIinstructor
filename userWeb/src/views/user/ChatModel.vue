<template>
  <div class="sse-container">
    <h3>实时流数据展示</h3>

    <!-- 按钮操作 -->
    <el-button
        type="primary"
        @click="connectSSE"
        :disabled="isConnected"
        :icon="Connection"
    >
      连接SSE
    </el-button>

    <el-button
        type="danger"
        @click="closeSSE"
        :disabled="!isConnected"
        :icon="SwitchButton"
    >
      断开连接
    </el-button>

    <el-button
        @click="clearMessages"
        :icon="Delete"
    >
      清空消息
    </el-button>

    <!-- 状态显示 -->
    <el-alert
        :title="status"
        :type="statusType"
        :closable="false"
        class="status-alert"
    />

    <!-- 消息展示区域 -->
    <el-card class="message-box">
      <template #header>
        <div class="card-header">
          <span>实时消息流</span>
        </div>
      </template>

      <div class="message-content" ref="messageContainer">
        <p class="message-item" v-if="messageText">{{ messageText }}</p>
        <p v-else class="empty-text">暂无数据，等待接收...</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted, nextTick } from 'vue'
import { ElButton, ElAlert, ElCard, ElMessage } from 'element-plus'
import { Connection, SwitchButton, Delete } from '@element-plus/icons-vue'

// 响应式数据
const messageText = ref('')
const isConnected = ref(false)
const eventSource = ref(null)
const messageContainer = ref(null) // 用于滚动

// 计算连接状态
const status = computed(() => (isConnected.value ? '已连接' : '未连接'))
const statusType = computed(() => (isConnected.value ? 'success' : 'info'))

// SSE 连接
const connectSSE = () => {
  if (eventSource.value) return

  try {
    eventSource.value = new EventSource('http://localhost:8080/chat/stream?msg=你好')
    let isContent = false
    let isAir = true

    eventSource.value.onopen = () => {
      isConnected.value = true
      messageText.value += 'SSE连接已建立。'
      ElMessage.success('SSE连接成功')
    }

    eventSource.value.onmessage = (e) => {
      if (e.data === '[DONE]') {
        messageText.value += ' 流式传输完成。'
        closeSSE()
        scrollToBottom()
      } else {
        const data = JSON.parse(e.data).choices[0].delta
        let text = null
        if (data.reasoning_content){
          isAir = true
        }
        if (isAir){
          text = data.reasoning_content ? "回答数据:" + data.content : "思考数据:" + data.content
          isAir = false
        }else {
          text = data.content
        }


        messageText.value += text
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
    ElMessage.info('SSE连接已关闭')
  }
}

// 清空消息
const clearMessages = () => {
  messageText.value = ''
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

// 组件卸载自动关闭 SSE
onUnmounted(() => {
  closeSSE()
})
</script>

<style scoped>
.sse-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.status-alert {
  margin: 15px 0;
}

.message-box {
  margin-top: 20px;
}

.message-content {
  max-height: 400px;
  overflow-y: auto;
  white-space: pre-wrap; /* 保留空格和换行符 */
}

.message-item {
  padding: 8px 0;
  margin: 0;
}

.empty-text {
  color: #999;
  text-align: center;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
