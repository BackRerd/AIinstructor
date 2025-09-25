<template>
  <el-container style="height: 100vh; background-color: #f5f7fa;">
    <!-- 顶部导航栏 -->
    <el-header height="70px" class="header">
      <div class="header-container">
        <div class="header-left">
          <div class="logo">
            <el-icon class="logo-icon"><ChatDotRound /></el-icon>
            <h3 class="logo-text">AI 对话助手</h3>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="36" :src="userAvatar" />
              <span class="username">{{ userName || '未登录' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="login" v-if="!userName">登录</el-dropdown-item>
                <el-dropdown-item @click="logout" v-else>退出登录</el-dropdown-item>
                <el-dropdown-item @click="showSettings">设置</el-dropdown-item>
                <el-dropdown-item @click="showHelp">帮助中心</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <div class="mobile-menu-btn" @click="drawerVisible = true">
            <el-icon><Menu /></el-icon>
          </div>
        </div>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 左侧边栏 - 桌面版 -->
      <el-aside width="260px" class="aside" v-show="!isMobile">
        <div class="model-selector">
          <h4 class="section-title">
            <el-icon class="section-icon"><Setting /></el-icon>
            模型选择
          </h4>
          <el-select
              v-model="selectedModel"
              placeholder="请选择模型"
              class="model-select"
              popper-class="model-select-popper"
          >

            <el-option-group
                v-for="group in modelGroups"
                :key="group.label"
                :label="group.label"
            >
              <el-option
                  v-for="model in group.options"
                  :key="model.value"
                  :label="model.label"
                  :value="model.value"
                  class="model-option"
              >
                <div class="model-info">
                  <span class="model-name">{{ model.label }}</span>
                  <span class="model-desc">{{ model.description }}</span>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
          <!-- 分页组件 - 桌面版 -->
          <div class="model-pagination" v-if="modelPagination.pages > 1">
            <el-pagination
                :current-page="modelPagination.current"
                :page-size="modelPagination.size"
                :total="modelPagination.total"
                :page-sizes="[5, 10, 20, 50]"
                layout="prev, pager, next, jumper"
                small
                @current-change="handleCurrentChange"
            /></div>
        </div>

        <div class="history-section">
          <h4 class="section-title">
            <el-icon class="section-icon"><ChatDotRound /></el-icon>
            历史对话
            <div class="section-actions">
              <el-button type="primary" size="small" @click="createNewChat" :disabled="!selectedModel">
                <el-icon><Plus /></el-icon>
                新对话
              </el-button>
              <el-button type="text" @click="refreshHistory" :loading="isLoadingHistory">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </h4>
          <div class="history-list">
            <div v-if="isLoadingHistory" class="loading-history">
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>加载中...</span>
            </div>
            <div
                v-else
                v-for="(item, index) in historyList"
                :key="index"
                class="history-item"
                @click="loadHistory(item.id)"
            >
              <div class="history-content">
                <div class="history-title">{{ item.title }}</div>
                <div class="history-time">{{ item.time }}</div>
                <div class="history-meta">
                  <span class="message-count">{{ item.messageCount || 0 }}条消息</span>
                </div>
              </div>
              <el-button 
                type="text" 
                @click.stop="deleteHistory(item.id)" 
                class="delete-btn"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div v-if="!isLoadingHistory && historyList.length === 0" class="empty-history">
              <el-icon class="empty-icon"><CircleClose /></el-icon>
              <p>暂无历史对话</p>
            </div>
          </div>
          <!-- 历史记录分页 -->
          <div class="history-pagination" v-if="historyPagination.pages > 1">
            <div class="pagination-info">
              <span>共 {{ historyPagination.total }} 条记录，第 {{ historyPagination.current }} / {{ historyPagination.pages }} 页</span>
            </div>
            <el-pagination
                :current-page="historyPagination.current"
                :page-size="historyPagination.size"
                :total="historyPagination.total"
                :page-sizes="[5, 10, 20]"
                layout="prev, pager, next, sizes"
                small
                @current-change="handleHistoryPaginationChange"
                @size-change="handleHistorySizeChange"
            />
          </div>
        </div>
      </el-aside>

      <!-- 主聊天区域 -->
      <el-main class="chat-main">
        <!-- 状态栏 -->
        <div class="status-bar">
          <el-tag :type="statusType" size="small" effect="dark" round>
            <el-icon style="margin-right: 4px;">
              <Connection /> <!-- 连接图标 -->
            </el-icon>
            <span class="status-dot" :class="statusType"></span>
            {{ status }}
          </el-tag>
          <span class="model-info" v-if="selectedModel">
            当前模型: {{ getModelName(selectedModel) }}
          </span>
          <div class="tool-buttons">
            <el-tooltip content="清空对话" placement="top">
              <el-button
                  type="info"
                  size="small"
                  circle
                  @click="clearMessages"
              >
                <el-icon><DeleteFilled /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="复制对话" placement="top">
              <el-button
                  type="info"
                  size="small"
                  circle
                  @click="copyConversation"
              >
                <el-icon><DocumentCopy /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <!-- 聊天窗口 -->
        <div class="chat-window" ref="chatWindow">
          <div v-if="messages.length === 0" class="welcome-screen">
            <div class="welcome-content">
              <el-icon class="welcome-icon"><ChatLineRound /></el-icon>
              <h3>欢迎使用 AI 对话助手</h3>
              <p>请选择模型并开始对话，或从历史记录中加载之前的对话</p>
              <div class="quick-questions">
                <el-tag
                    v-for="(question, index) in quickQuestions"
                    :key="index"
                    class="quick-question"
                    @click="inputMessage = question; sendMessage()"
                >
                  {{ question }}
                </el-tag>
              </div>
            </div>
          </div>

          <div v-else>
            <div
                v-for="(msg, index) in messages"
                :key="index"
                :class="['chat-message', msg.role]"
            >
              <div class="message-container">
                <div class="message-avatar">
                  <el-avatar
                      :size="36"
                      :src="msg.role === 'user' ? userAvatar : aiAvatar"
                  />
                </div>
                <div class="message-content">
                  <div class="message-header">
                    <span class="message-role">{{ msg.role === 'user' ? userName || '我' : 'AI助手' }}</span>
                    <span class="message-time">{{ msg.timestamp ? formatChatTime(msg.timestamp) : formatTime(msg.time) }}</span>
                  </div>
                  <div class="message-text">
                    <markdown-renderer
                        v-if="msg.content"
                        :content="msg.content"
                        class="markdown-content"
                    />
                  </div>
                  <div class="message-actions" v-if="msg.role === 'ai'">
                    <el-tooltip content="复制回答" placement="top">
                      <el-button
                          type="text"
                          size="small"
                          @click="copyText(msg.content)"
                      >
                        <el-icon><DocumentCopy /></el-icon>
                      </el-button>
                    </el-tooltip>
                    <el-tooltip content="重新生成" placement="top">
                      <el-button
                          type="text"
                          size="small"
                          @click="regenerateResponse(index)"
                      >
                        <el-icon><Refresh /></el-icon>
                      </el-button>
                    </el-tooltip>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="isSearching" class="loading-indicator">
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>正在搜寻...</span>
            </div>
            <div v-if="isLoading" class="loading-indicator">
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>AI 正在思考中...</span>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <div class="input-tools">
            <el-tooltip content="上传文件" placement="top">
              <el-button type="text" @click="uploadFile">
                <el-icon><Upload /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="语音输入" placement="top">
              <el-button type="text" @click="startVoiceInput">
                <el-icon><Microphone /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
          <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="isMobile ? 2 : 3"
              placeholder="输入消息，Shift+Enter换行，Enter发送"
              resize="none"
              @keyup.enter.native="handleKeyUp"
              class="message-input"
          />
          <div class="input-footer">
            <el-tooltip content="快捷指令" placement="top">
              <el-button type="text" @click="showShortcuts">
                ⌘
              </el-button>
            </el-tooltip>
            <el-button
                type="primary"
                @click="sendMessage"
                :disabled="!inputMessage.trim() || isLoading"
                class="send-button"
            >
              发送
              <template #icon>
                <el-icon><Upload /></el-icon>
              </template>
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>

    <!-- 底部信息 -->
    <el-footer height="50px" class="footer">
      <div class="footer-content">
        <span>© 2025 AI 对话助手 v1.0.0</span>
        <div class="footer-links">
          <el-link type="info" @click="showAbout">关于我们</el-link>
          <el-link type="info" @click="showPrivacy">隐私政策</el-link>
          <el-link type="info" @click="showTerms">服务条款</el-link>
        </div>
      </div>
    </el-footer>

    <!-- 移动端抽屉菜单 - 完全重构 -->
    <el-drawer
        v-model="drawerVisible"
        title="AI 对话助手"
        direction="ltr"
        :size="isMobile ? '85%' : '300px'"
        class="mobile-drawer"
    >
      <!--      <div class="drawer-header">-->
      <!--        <h3>AI 对话助手</h3>-->
      <!--        <el-button type="text" @click="drawerVisible = false">-->
      <!--          <el-icon><Close /></el-icon>-->
      <!--        </el-button>-->
      <!--      </div>-->

      <div class="drawer-content">
        <!-- 用户信息 -->
        <div class="user-card">
          <el-avatar :size="48" :src="userAvatar" />
          <div class="user-info">
            <div class="username">{{ userName || '未登录用户' }}</div>
            <div class="user-actions">
              <el-button v-if="!userName" type="primary" size="small" @click="login">登录</el-button>
              <el-button v-else type="danger" size="small" @click="logout">退出</el-button>
            </div>
          </div>
        </div>

        <!-- 模型选择 -->
        <div class="drawer-section">
          <div class="section-header">
            <el-icon class="section-icon"><Setting /></el-icon>
            <h4>模型选择</h4>
          </div>
          <el-select
              v-model="selectedModel"
              placeholder="请选择模型"
              class="model-select"
          >
            <el-option-group
                v-for="group in modelGroups"
                :key="group.label"
                :label="group.label"
            >
              <el-option
                  v-for="model in group.options"
                  :key="model.value"
                  :label="model.label"
                  :value="model.value"
              >
                <div class="model-info">
                  <span class="model-name">{{ model.label }}</span>
                  <span class="model-desc">{{ model.description }}</span>
                </div>
              </el-option>
            </el-option-group>
          </el-select>
          <div class="model-pagination" v-if="modelPagination.pages > 1">
            <el-pagination
                :current-page="modelPagination.current"
                :page-size="modelPagination.size"
                :total="modelPagination.total"
                layout="prev, pager, next"
                small
                @current-change="handleCurrentChange"
            />
          </div>
        </div>

        <!-- 历史对话 -->
        <div class="drawer-section">
          <div class="section-header">
            <el-icon class="section-icon"><ChatDotRound /></el-icon>
            <h4>历史对话</h4>
            <div class="section-actions">
              <el-button type="primary" size="small" @click="createNewChat" :disabled="!selectedModel">
                <el-icon><Plus /></el-icon>
                新对话
              </el-button>
              <el-button type="text" @click="refreshHistory" :loading="isLoadingHistory">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="history-list">
            <div v-if="isLoadingHistory" class="loading-history">
              <el-icon class="is-loading">
                <Loading />
              </el-icon>
              <span>加载中...</span>
            </div>
            <div
                v-else
                v-for="(item, index) in historyList"
                :key="index"
                class="history-item"
                @click="loadHistory(item.id); drawerVisible = false"
            >
              <div class="history-icon">
                <el-icon><ChatLineRound /></el-icon>
              </div>
              <div class="history-content">
                <div class="history-title">{{ item.title }}</div>
                <div class="history-time">{{ item.time }}</div>
                <div class="history-meta">
                  <span class="message-count">{{ item.messageCount || 0 }}条消息</span>
                </div>
              </div>
              <el-button type="text" @click.stop="deleteHistory(item.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <div v-if="!isLoadingHistory && historyList.length === 0" class="empty-history">
              <el-icon class="empty-icon"><CircleClose /></el-icon>
              <p>暂无历史对话</p>
            </div>
          </div>
          <!-- 移动端历史记录分页 -->
          <div class="history-pagination" v-if="historyPagination.pages > 1">
            <div class="pagination-info">
              <span>共 {{ historyPagination.total }} 条记录，第 {{ historyPagination.current }} / {{ historyPagination.pages }} 页</span>
            </div>
            <el-pagination
                :current-page="historyPagination.current"
                :page-size="historyPagination.size"
                :total="historyPagination.total"
                :page-sizes="[5, 10, 20]"
                layout="prev, pager, next, sizes"
                small
                @current-change="handleHistoryPaginationChange"
                @size-change="handleHistorySizeChange"
            />
          </div>
        </div>

        <!-- 快捷功能 -->
        <div class="drawer-section">
          <div class="section-header">
            <el-icon class="section-icon"><Tools /></el-icon>
            <h4>快捷功能</h4>
          </div>
          <div class="quick-actions">
            <el-button @click="showSettings" style="margin-left: 12px">
              <el-icon><Setting /></el-icon>
              <span>设置</span>
            </el-button>
            <el-button @click="showHelp">
              <el-icon><QuestionFilled /></el-icon>
              <span>帮助</span>
            </el-button>
            <el-button @click="showShortcuts">
              <el-icon><Operation /></el-icon>
              <span>快捷指令</span>
            </el-button>
            <el-button @click="clearMessages">
              <el-icon><DeleteFilled /></el-icon>
              <span>清空对话</span>
            </el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Loading,
  Menu,
  Refresh,
  Delete,
  Plus,
  Setting,
  ChatDotRound,
  Upload,
  Microphone,
  Tools,
  QuestionFilled,
  ChatLineRound,
  DocumentCopy,
  DeleteFilled,
  CircleClose, Connection
} from '@element-plus/icons-vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import {
  FetchModelEndpoints,
  FetchChatSessions,
  DeleteChatSession,
  CreateChatSession,
  GetChatSession,
  UpdateSessionActivity,
  IncrementMessageCount,
  InsertRecords,
  GetChatRecordsBySession
} from "@/api/user.js";

// 导入登录相关函数
import { login as authLogin, logout as authLogout, getCurrentUser, isLoggedIn } from '@/api/auth.js'
import router from '@/router'

// 响应式判断
const isMobile = ref(false)
const drawerVisible = ref(false)

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth < 768
}


// 用户信息
const userName = ref('')
const userId = ref('')
const userAvatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png')
const aiAvatar = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')
// 模型选择和分页
const selectedModel = ref('')
const modelGroups = ref([])
const modelPagination = ref({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})
const isLoadingModels = ref(false)

// 监听模型切换，自动刷新历史记录
watch(selectedModel, (newModel, oldModel) => {
  if (newModel !== oldModel && newModel) {
    // 清空当前消息
    messages.value = []
    currentSessionId.value = null
    // 刷新历史记录
    loadChatHistory(1, historyPagination.value.size)
  }
})

// 加载模型列表
const loadModelList = async (page = 1, size = 10) => {
  try {
    isLoadingModels.value = true
    const response = await FetchModelEndpoints(page, size)

    if (response && response.records && response.records.length > 0) {
      // 更新分页信息
      modelPagination.value = {
        current: response.current,
        size: response.size,
        total: response.total,
        pages: response.pages
      }

      // 按模型类型分组
      const groupedModels = {}

      response.records.forEach(model => {
        if (!groupedModels[model.modelType]) {
          groupedModels[model.modelType] = []
        }

        groupedModels[model.modelType].push({
          label: model.modelName,
          value: model.modelId.toString(),
          description: model.modelDescription || '暂无描述',
          rawData: model // 保留原始数据以便后续使用
        })
      })

      // 转换为 modelGroups 格式
      modelGroups.value = Object.keys(groupedModels).map(type => ({
        label: type.toUpperCase() + ' 模型',
        options: groupedModels[type]
      }))

      // 默认选择第一个模型
      if (modelGroups.value.length > 0 && modelGroups.value[0].options.length > 0) {
        selectedModel.value = modelGroups.value[0].options[0].value
      }
    } else {
      // 如果没有数据，使用默认模型
      modelGroups.value = [
        {
          label: '默认模型',
          options: [
            {
              label: '默认AI模型',
              value: 'default',
              description: '系统默认AI模型'
            }
          ]
        }
      ]
      selectedModel.value = 'default'
      modelPagination.value = {
        current: 1,
        size: 10,
        total: 1,
        pages: 1
      }
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
    ElMessage.error('加载模型列表失败，使用默认模型')

    modelGroups.value = [
      {
        label: '默认模型',
        options: [
          {
            label: '默认AI模型',
            value: 'default',
            description: '系统默认AI模型'
          }
        ]
      }
    ]
    selectedModel.value = 'default'
  } finally {
    isLoadingModels.value = false
  }
}

// 处理分页变化
const handlePaginationChange = (page, size) => {
  loadModelList(page, size)
}

// 历史对话
const historyList = ref([])
const historyPagination = ref({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})
const isLoadingHistory = ref(false)

// 聊天数据
const inputMessage = ref('')
let userMsg = ref('')
const messages = ref([])
const isLoading = ref(false)
const isSearching = ref(false)
const currentSessionId = ref(null) // 当前会话ID

// 快速问题示例
const quickQuestions = ref([
  '如何学习Vue3？',
  'Python数据分析的最佳实践是什么？',
  '解释一下机器学习中的过拟合',
  '如何提高我的编程技能？'
])

// SSE 流式处理
const isConnected = ref(false)
const eventSource = ref(null)
const chatWindow = ref(null)

// 状态显示
const status = computed(() => {
  if (isLoading.value) return 'AI思考中'
  return isConnected.value ? '连接中' : '准备就绪'
})
const statusType = computed(() => {
  if (isLoading.value) return 'warning'
  return isConnected.value ? 'success' : 'info'
})

// 获取模型显示名称
const getModelName = (value) => {
  for (const group of modelGroups.value) {
    const model = group.options.find(m => m.value === value)
    if (model) return model.label
  }
  return '未知模型'
}

// 获取当前选中的模型完整信息
const getCurrentModel = () => {
  for (const group of modelGroups.value) {
    const model = group.options.find(m => m.value === selectedModel.value)
    if (model) return model
  }
  return null
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString()
}

// 解析时间字符串的辅助函数
const parseTimeString = (timeStr) => {
  if (!timeStr) return null
  
  console.log('正在解析时间:', timeStr)
  
  try {
    // 直接尝试解析ISO 8601格式
    const date = new Date(timeStr)
    if (!isNaN(date.getTime())) {
      console.log('解析成功:', date.toISOString())
      return date
    }
    
    console.warn('直接解析失败，尝试清理格式')
    
    // 如果直接解析失败，尝试清理格式
    // 移除毫秒部分 (.000)
    let cleaned = timeStr.replace(/\.\d{3}/, '')
    console.log('移除毫秒后:', cleaned)
    
    const cleanedDate = new Date(cleaned)
    if (!isNaN(cleanedDate.getTime())) {
      console.log('清理后解析成功:', cleanedDate.toISOString())
      return cleanedDate
    }
    
    console.warn('所有解析方法都失败了，返回null')
    
  } catch (error) {
    console.warn('时间解析异常:', error, '原始时间:', timeStr)
  }
  
  return null
}

// 格式化聊天记录时间
const formatChatTime = (timeStr) => {
  if (!timeStr) return '刚刚'
  
  try {
    const date = parseTimeString(timeStr)
    
    if (!date) {
      console.warn('无法解析时间格式:', timeStr)
      return '刚刚'
    }
    
    const now = new Date()
    const diff = now - date
    
    // 小于1小时显示分钟
    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000))
      return minutes < 1 ? '刚刚' : `${minutes}分钟前`
    }
    
    // 小于24小时显示小时
    if (diff < 24 * 60 * 60 * 1000) {
      const hours = Math.floor(diff / (60 * 60 * 1000))
      return `${hours}小时前`
    }
    
    // 小于7天显示天数
    if (diff < 7 * 24 * 60 * 60 * 1000) {
      const days = Math.floor(diff / (24 * 60 * 60 * 1000))
      return `${days}天前`
    }
    
    // 超过7天显示具体日期
    return date.toLocaleDateString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.warn('时间格式化失败:', error, '原始时间:', timeStr)
    return '刚刚'
  }
}

// 登录/登出
const login = () => {
  // 跳转到登录页面
  router.push('/login')
}

const logout = () => {
  authLogout()
  userName.value = ''
  userId.value = ''
  ElMessage.info('您已退出登录')
  drawerVisible.value = false
}

// 检查用户登录状态
const checkLoginStatus = () => {
  if (isLoggedIn()) {
    const userInfo = getCurrentUser()
    if (userInfo) {
      userName.value = userInfo.username
      userId.value = userInfo.uid
    }
  }
}
// SSE 流式处理

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return

  // 如果没有当前会话，先创建一个新会话
  if (!currentSessionId.value) {
    await createNewChatForMessage()
  }

  messages.value.push({ role: 'user', content: inputMessage.value })
  const msg = inputMessage.value
  userMsg.value = msg
  inputMessage.value = ''

  scrollToBottom()

  // 确保会话ID存在后再建立 SSE 连接
  if (currentSessionId.value) {
    connectSSE(msg)
  } else {
    ElMessage.error('会话创建失败，无法发送消息')
  }
}

// 为消息创建新会话
const createNewChatForMessage = async () => {
  try {
    // 检查用户是否已登录
    if (!isLoggedIn()) {
      ElMessage.warning('请先登录后再创建会话')
      router.push('/login')
      throw new Error('用户未登录')
    }

    // 生成唯一的会话ID - 使用纯数字ID
    const sessionId = Date.now().toString()
    
    // 创建会话数据
    const sessionData = {
      sessionId: sessionId,
      userId: userId.value,
      sessionTitle: '新对话',
      modelId: parseInt(selectedModel.value)
    }

    // 调用API创建会话
    const response = await CreateChatSession(sessionData)
    
    if (response && response.success) {
      currentSessionId.value = sessionId
      // 刷新历史记录
      await loadChatHistory(historyPagination.value.current, historyPagination.value.size)
    } else {
      throw new Error('创建会话失败')
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error('创建会话失败，请重试')
    throw error
  }
}

// 更新会话活跃状态和消息计数
const updateSessionAfterMessage = async () => {
  if (!currentSessionId.value) return

  try {
    // 获取最后一条用户消息作为最后消息
    const lastUserMessage = messages.value.filter(msg => msg.role === 'user').pop()
    const lastMessage = lastUserMessage ? lastUserMessage.content : ''

    // 更新会话活跃状态
    await UpdateSessionActivity(currentSessionId.value, lastMessage)
    
    // 增加消息计数
    await IncrementMessageCount(currentSessionId.value)
    
    // 刷新历史记录
    await loadChatHistory(historyPagination.value.current, historyPagination.value.size)
  } catch (error) {
    console.error('更新会话状态失败:', error)
  }
}
function safeInsert(chatData) {
  const originalContent = chatData.response_content;
  const hasEmoji = /[^\u0000-\uFFFF]/.test(originalContent);

  if (hasEmoji) {
    // 记录警告，便于后续分析
    console.warn('过滤掉emoji内容:', originalContent);
    chatData.response_content = removeEmoji(originalContent);
  }

  return InsertRecords(chatData);
}

const connectSSE = (msg) => {
  {
    if (eventSource.value) closeSSE();

    // 验证必要参数
    if (!currentSessionId.value) {
      ElMessage.error('会话ID不存在，无法建立连接')
      return
    }
    
    if (!selectedModel.value) {
      ElMessage.error('模型ID不存在，无法建立连接')
      return
    }

    try {
      const url = `http://localhost:8080/chat/stream?msg=${encodeURIComponent(msg)}&modelId=${selectedModel.value}&sessionId=${parseInt(currentSessionId.value)}`
      console.log('建立SSE连接，URL:', url)
      eventSource.value = new EventSource(url)
      isConnected.value = true

      // 连接建立即显示“正在搜寻...”，以便在搜寻开始前就提示
      isSearching.value = true

      let isAir = false
      let asy = ''
      eventSource.value.onmessage = (e) => {
        if (e.data === '[DONE]') {
          closeSSE()
          isSearching.value = false
          scrollToBottom()
          // 更新会话活跃状态和消息计数
          updateSessionAfterMessage()
          try {
            const responseAsy = InsertRecords(
                15,
                currentSessionId.value,
                selectedModel.value,
                "assistant",
                userMsg.value,
                asy
            )
            const responseContent = InsertRecords(
                15,
                currentSessionId.value,
                selectedModel.value,
                "system",
                userMsg.value,
                messages.value[messages.value.length - 1].content
            )
            console.log('responseAsy', responseAsy)
            console.log('responseContent', responseContent)
          }catch (e){
            console.error(e)
          }


        } else {
          const data = JSON.parse(e.data).choices[0].delta
          let text = ''
          console.log(data.content)
          if (data.reasoning_content && !isAir){
            isAir = true
            asy = data.reasoning_content
          }
          if (isAir) {
            text = data.content === 'null' || data.content === null ? '' : data.content
            if (text) {
              // 第一段正常内容到来时移除“正在搜寻...”
              isSearching.value = false
            }
          }
          // if (data.reasoning_content) {
          //   isAir = true
          // }
          // if (isAir) {
          //   text = data.reasoning_content ? "回答数据:" + data.content : "思考数据:" + data.content
          //   isAir = false
          // } else {
          //   text = data.content
          // }

          // 将 AI 消息追加到 messages
          if (messages.value.length === 0 || messages.value[messages.value.length - 1].role !== 'ai') {
            messages.value.push({role: 'ai', content: text})
          } else {
            messages.value[messages.value.length - 1].content += text
          }

          scrollToBottom()
        }
      }
      eventSource.value.onerror = (error) => {
        isConnected.value = false
        isSearching.value = false
        ElMessage.error('SSE连接出错')
        console.error('SSE错误:', error)
      }
    } catch (error) {
      ElMessage.error('创建SSE连接失败')
      console.error('创建EventSource失败:', error)
    }
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
// 处理键盘事件
const handleKeyUp = (e) => {
  if (e.shiftKey && e.key === 'Enter') {
    // Shift+Enter 换行
    inputMessage.value += '\n'
  } else if (e.key === 'Enter') {
    // Enter 发送消息
    if (!e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
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

// 清空对话
const clearMessages = () => {
  ElMessageBox.confirm('确定要清空当前对话吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value = []
    ElMessage.success('对话已清空')
    drawerVisible.value = false
  }).catch(() => {})
}

// 复制文本
const copyText = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

// 复制整个对话
const copyConversation = () => {
  const conversation = messages.value.map(msg => {
    return `${msg.role === 'user' ? '我' : 'AI'}: ${msg.content}`
  }).join('\n\n')

  copyText(conversation)
}

// 重新生成回答
const regenerateResponse = (index) => {
  ElMessageBox.confirm('确定要重新生成此回答吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    messages.value.splice(index, 1)
    const lastUserMessage = messages.value[messages.value.length - 1]
    if (lastUserMessage && lastUserMessage.role === 'user') {
      inputMessage.value = lastUserMessage.content
      sendMessage()
    }
  }).catch(() => {})
}

// 加载聊天历史数据
const loadChatHistory = async (page = 1, size = 10) => {
  try {
    isLoadingHistory.value = true
    // 根据当前选中的模型ID查询历史记录
    const modelId = selectedModel.value ? parseInt(selectedModel.value) : null
    const response = await FetchChatSessions(page, size, null, modelId)
    
    if (response && response.records) {
      // 更新分页信息
      historyPagination.value = {
        current: response.current,
        size: response.size,
        total: response.total,
        pages: response.pages
      }
      
      // 转换数据格式以适配现有UI
      historyList.value = response.records.map(session => ({
        id: session.sessionId,
        title: session.sessionTitle || '未命名对话',
        time: formatHistoryTime(session.lastActiveAt || session.createdAt),
        sessionId: session.sessionId,
        userId: session.userId,
        modelId: session.modelId,
        lastMessage: session.lastMessage,
        messageCount: session.messageCount,
        lastActiveAt: session.lastActiveAt,
        createdAt: session.createdAt,
        updatedAt: session.updatedAt
      }))
    } else {
      historyList.value = []
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    ElMessage.error('加载聊天历史失败')
    historyList.value = []
  } finally {
    isLoadingHistory.value = false
  }
}

// 格式化历史时间
const formatHistoryTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  // 小于1小时显示分钟
  if (diff < 60 * 60 * 1000) {
    const minutes = Math.floor(diff / (60 * 1000))
    return minutes < 1 ? '刚刚' : `${minutes}分钟前`
  }
  
  // 小于24小时显示小时
  if (diff < 24 * 60 * 60 * 1000) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    return `${hours}小时前`
  }
  
  // 小于7天显示天数
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000))
    return `${days}天前`
  }
  
  // 超过7天显示具体日期
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 测试时间解析
const testTimeParsing = () => {
  const testTime = "2025-09-25T03:05:47.000+00:00"
  console.log('=== 时间解析测试 ===')
  console.log('测试时间:', testTime)
  console.log('直接new Date():', new Date(testTime))
  console.log('parseTimeString结果:', parseTimeString(testTime))
  console.log('formatChatTime结果:', formatChatTime(testTime))
  console.log('==================')
}

// 加载历史对话
const loadHistory = async (id) => {
  const history = historyList.value.find(item => item.id === id)
  if (history) {
    try {
      // 设置当前会话ID
      currentSessionId.value = history.sessionId
      
      // 显示加载状态
      ElMessage.info('正在加载历史对话...')
      
      // 测试时间解析
      testTimeParsing()
      
      // 调用API获取完整的聊天记录
      console.log('正在加载聊天记录，sessionId:', history.sessionId)
      const response = await GetChatRecordsBySession(history.sessionId)
      console.log('聊天记录API响应:', response)
      
      if (response && response.success && response.data && response.data.length > 0) {
        // 将聊天记录转换为消息格式
        messages.value = []
        
        // 按时间戳排序聊天记录
        const sortedRecords = response.data.sort((a, b) => {
          const timeA = new Date(a.messageTimestamp || a.createdAt).getTime()
          const timeB = new Date(b.messageTimestamp || b.createdAt).getTime()
          return timeA - timeB
        })
        
        sortedRecords.forEach(record => {
          // 调试时间数据
          console.log('处理聊天记录:', {
            messageTimestamp: record.messageTimestamp,
            createdAt: record.createdAt,
            messageType: record.messageType,
            messageContent: record.messageContent?.substring(0, 50) + '...',
            responseContent: record.responseContent?.substring(0, 50) + '...'
          })
          
          // 测试时间解析
          const testTime = record.messageTimestamp || record.createdAt
          if (testTime) {
            console.log('=== 时间解析测试 ===')
            console.log('原始时间:', testTime)
            console.log('直接new Date():', new Date(testTime))
            console.log('parseTimeString结果:', parseTimeString(testTime))
            console.log('formatChatTime结果:', formatChatTime(testTime))
            console.log('==================')
          }
          
          // 根据messageType判断消息类型
          if (record.messageType === 'user' || record.messageType === 'system') {
            // 添加用户消息
            if (record.messageContent && record.messageContent.trim()) {
              const timeStr = record.messageTimestamp || record.createdAt || new Date().toISOString()
              messages.value.push({
                role: 'user',
                content: record.messageContent.trim(),
                time: formatChatTime(timeStr),
                timestamp: timeStr
              })
            }
            
            // 添加AI回复
            if (record.responseContent && record.responseContent.trim()) {
              const timeStr = record.messageTimestamp || record.createdAt || new Date().toISOString()
              messages.value.push({
                role: 'ai',
                content: record.responseContent.trim(),
                time: formatChatTime(timeStr),
                timestamp: timeStr
              })
            }
          }
        })
        
        // 计算消息统计
        const userMessages = messages.value.filter(msg => msg.role === 'user').length
        const aiMessages = messages.value.filter(msg => msg.role === 'ai').length
        
        ElMessage.success(`已加载历史对话: ${history.title} (用户消息: ${userMessages}条, AI回复: ${aiMessages}条)`)
        scrollToBottom()
        
        // 关闭移动端抽屉
        if (isMobile.value) {
          drawerVisible.value = false
        }
      } else {
        // 如果没有聊天记录，显示默认消息
        const defaultTime = history.lastActiveAt || history.createdAt || new Date().toISOString()
        messages.value = [
          {
            role: 'user',
            content: history.title,
            time: formatChatTime(defaultTime),
            timestamp: defaultTime
          },
          {
            role: 'ai',
            content: history.lastMessage || `这是关于"${history.title}"的历史对话内容。`,
            time: formatChatTime(defaultTime),
            timestamp: defaultTime
          }
        ]
        ElMessage.success(`已加载历史对话: ${history.title}`)
        scrollToBottom()
      }
    } catch (error) {
      console.error('加载历史对话失败:', error)
      ElMessage.error('加载历史对话失败: ' + error.message)
      
      // 如果API调用失败，显示默认消息
      const fallbackTime = history.lastActiveAt || history.createdAt || new Date().toISOString()
      messages.value = [
        {
          role: 'user',
          content: history.title,
          time: formatChatTime(fallbackTime),
          timestamp: fallbackTime
        },
        {
          role: 'ai',
          content: history.lastMessage || `这是关于"${history.title}"的历史对话内容。`,
          time: formatChatTime(fallbackTime),
          timestamp: fallbackTime
        }
      ]
      
      // 关闭移动端抽屉
      if (isMobile.value) {
        drawerVisible.value = false
      }
    }
  }
}

// 刷新历史记录
const refreshHistory = () => {
  loadChatHistory(historyPagination.value.current, historyPagination.value.size)
  ElMessage.success('历史记录已刷新')
}

// 创建新对话
const createNewChat = async () => {
  if (!selectedModel.value) {
    ElMessage.warning('请先选择模型')
    return
  }

  try {
    // 生成唯一的会话ID - 使用纯数字ID
    const sessionId = Date.now().toString()
    
    // 创建会话数据
    const sessionData = {
      sessionId: sessionId,
      userId: userName.value || 'anonymous',
      sessionTitle: '新对话',
      modelId: parseInt(selectedModel.value)
    }

    // 调用API创建会话
    const response = await CreateChatSession(sessionData)
    
    if (response && response.success) {
      // 清空当前消息
      messages.value = []
      currentSessionId.value = sessionId
      
      // 刷新历史记录
      await loadChatHistory(historyPagination.value.current, historyPagination.value.size)
      
      ElMessage.success('新对话已创建')
      drawerVisible.value = false
    } else {
      ElMessage.error('创建新对话失败')
    }
  } catch (error) {
    console.error('创建新对话失败:', error)
    ElMessage.error('创建新对话失败，请重试')
  }
}

// 处理历史记录分页变化
const handleHistoryPaginationChange = (page) => {
  historyPagination.value.current = page
  loadChatHistory(page, historyPagination.value.size)
}

// 处理历史记录页面大小变化
const handleHistorySizeChange = (size) => {
  historyPagination.value.size = size
  historyPagination.value.current = 1 // 重置到第一页
  loadChatHistory(1, size)
}

// 删除历史记录
const deleteHistory = async (id) => {
  console.log('删除历史记录，ID:', id, '类型:', typeof id)
  
  ElMessageBox.confirm('确定要删除此历史记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      console.log('开始调用删除API，ID:', id)
      const result = await DeleteChatSession(id)
      console.log('删除API返回结果:', result)
      
      if (result && result.success) {
        ElMessage.success('历史记录已删除')
        // 重新加载当前页数据
        await loadChatHistory(historyPagination.value.current, historyPagination.value.size)
      } else {
        ElMessage.error('删除失败：' + (result?.message || '未知错误'))
      }
    } catch (error) {
      console.error('删除历史记录失败:', error)
      ElMessage.error('删除失败，请重试: ' + error.message)
    }
  }).catch((error) => {
      console.log('用户取消删除或确认框出错:', error)
  })
}

// 刷新模型列表
const refreshModels = () => {
  loadModelList(modelPagination.value.current, modelPagination.value.size)
  ElMessage.success('模型列表已刷新')
}

// 改变每页显示数量
const handleSizeChange = (size) => {
  modelPagination.value.size = size
  loadModelList(1, size)
}

// 改变当前页码
const handleCurrentChange = (page) => {
  loadModelList(page, modelPagination.value.size)
}

// 其他功能方法
const showSettings = () => {
  ElMessage.info('打开设置面板')
  drawerVisible.value = false
}

const showHelp = () => {
  ElMessage.info('打开帮助中心')
  drawerVisible.value = false
}

const showAbout = () => {
  ElMessage.info('显示关于信息')
}

const showPrivacy = () => {
  ElMessage.info('显示隐私政策')
}

const showTerms = () => {
  ElMessage.info('显示服务条款')
}

const uploadFile = () => {
  ElMessage.info('上传文件功能(敬请期待)')
}

const startVoiceInput = () => {
  ElMessage.info('开始语音输入(敬请期待)')
}

const showShortcuts = () => {
  ElMessage.info('显示快捷指令')
  drawerVisible.value = false
}

onMounted(() => {
  checkScreenSize()
  loadModelList()
  loadChatHistory()
  // 检查用户登录状态
  checkLoginStatus()
})

onUnmounted(() => {
})
</script>

<style lang="scss" scoped>
.header {
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  z-index: 10;

  .header-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .header-left {
    display: flex;
    align-items: center;
  }

  .logo {
    display: flex;
    align-items: center;

    .logo-icon {
      width: 28px;
      height: 28px;
      margin-right: 10px;
      color: #409eff;
    }

    .logo-text {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #333;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
  }

  .mobile-menu-btn {
    display: none;
    margin-left: 15px;
    cursor: pointer;
    font-size: 20px;
    color: #606266;
  }

  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;

    .username {
      margin-left: 10px;
      font-size: 14px;
    }
  }
}

.main-container {
  width: 1400px;
  max-width: 1400px;
  margin: 0 auto;
  height: calc(100vh - 120px);
  display: flex;
}

.aside {
  background-color: #ffffff;
  border-right: 1px solid #e6e6e6;
  padding: 20px;
  display: flex;
  flex-direction: column;
  width: 260px;
  min-width: 260px; // 确保有足够空间显示删除按钮
  flex-shrink: 0; // 防止侧边栏被压缩

  .section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    color: #666;
    margin: 15px 0 10px;

    .section-icon {
      width: 16px;
      height: 16px;
      margin-right: 8px;
      color: #888;
    }

    .section-actions {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .model-select {
    width: 100%;

    :deep(.el-input__inner) {
      background-color: #f7f9fc;
      border-color: #e4e7ed;
    }
  }

  .history-section {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }

  .history-list {
    flex: 1;
    overflow-y: auto;
    margin-top: 10px;

    .history-item {
      display: flex;
      align-items: center;
      padding: 10px;
      border-radius: 6px;
      margin-bottom: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background-color: #f5f7fa;
        
        .delete-btn {
          opacity: 1;
        }
      }

      .history-content {
        flex: 1;
        min-width: 0; // 防止内容溢出

        .history-title {
          font-size: 13px;
          color: #333;
          margin-bottom: 4px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }

        .history-time {
          font-size: 11px;
          color: #999;
          margin-bottom: 2px;
        }

        .history-meta {
          font-size: 10px;
          color: #999;

          .message-count {
            background-color: #f0f0f0;
            padding: 2px 6px;
            border-radius: 10px;
            font-size: 10px;
          }
        }
      }

      .delete-btn {
        opacity: 0;
        transition: all 0.2s;
        padding: 4px;
        margin-left: 8px;
        flex-shrink: 0;
        min-width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #999;
        
        &:hover {
          color: #f56c6c;
          background-color: rgba(245, 108, 108, 0.1);
          border-radius: 4px;
        }
      }
    }

    .loading-history {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      color: #666;

      .el-icon {
        margin-right: 8px;
        animation: rotating 2s linear infinite;
      }
    }

    .history-pagination {
      margin-top: 15px;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 10px;

      .pagination-info {
        font-size: 12px;
        color: #666;
        text-align: center;
      }
    }

    .empty-history {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #999;

      .empty-icon {
        width: 50px;
        height: 50px;
        margin-bottom: 10px;
        color: #ddd;
      }

      p {
        margin: 0;
        font-size: 13px;
      }
    }
  }
}

.chat-main {
  display: flex;
  flex-direction: column;
  padding: 0;
  background-color: #f9fafc;
  height: 100%;

  .status-bar {
    display: flex;
    align-items: center;
    padding: 10px 20px;
    background-color: #ffffff;
    border-bottom: 1px solid #e6e6e6;

    .el-tag {
      margin-right: 15px;
    }

    .status-dot {
      display: inline-block;
      width: 8px;
      height: 8px;
      border-radius: 50%;
      margin-right: 6px;

      &.success {
        background-color: #67c23a;
      }

      &.info {
        background-color: #909399;
      }

      &.warning {
        background-color: #e6a23c;
      }
    }

    .model-info {
      font-size: 13px;
      color: #666;
    }

    .tool-buttons {
      margin-left: auto;

      .el-button {
        margin-left: 8px;
      }
    }
  }

  .chat-window {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background-color: #ffffff;

    .welcome-screen {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100%;

      .welcome-content {
        text-align: center;
        max-width: 500px;

        .welcome-icon {
          width: 80px;
          height: 80px;
          margin-bottom: 20px;
          color: #409eff;
        }

        h3 {
          margin: 0 0 10px;
          color: #333;
        }

        p {
          margin: 0 0 20px;
          color: #666;
          font-size: 14px;
        }

        .quick-questions {
          display: flex;
          flex-wrap: wrap;
          justify-content: center;
          gap: 10px;

          .quick-question {
            cursor: pointer;
            transition: all 0.2s;

            &:hover {
              background-color: #ecf5ff;
              color: #409eff;
            }
          }
        }
      }
    }

    .chat-message {
      margin-bottom: 20px;

      .message-container {
        display: flex;
        max-width: 800px;
        margin: 0 auto;

        .message-avatar {
          margin-right: 15px;
        }

        .message-content {
          flex: 1;

          .message-header {
            display: flex;
            align-items: center;
            margin-bottom: 5px;

            .message-role {
              font-weight: 600;
              font-size: 15px;
            }

            .message-time {
              margin-left: 10px;
              font-size: 12px;
              color: #999;
            }
          }

          .message-text {
            line-height: 1.6;
            font-size: 15px;

            .markdown-content {
              :deep(pre) {
                background-color: #f6f8fa;
                padding: 12px;
                border-radius: 6px;
                overflow-x: auto;
              }

              :deep(code) {
                font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
                font-size: 85%;
              }
            }
          }

          .message-actions {
            margin-top: 8px;
            opacity: 0;
            transition: opacity 0.2s;
          }
        }
      }

      &.user {
        .message-container {
          flex-direction: row-reverse;

          .message-avatar {
            margin-right: 0;
            margin-left: 15px;
          }

          .message-content {
            text-align: right;

            .message-header {
              justify-content: flex-end;
            }
          }
        }
      }

      &:hover {
        .message-actions {
          opacity: 1;
        }
      }
    }

    .loading-indicator {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 15px;
      color: #666;

      .el-icon {
        margin-right: 8px;
        animation: rotating 2s linear infinite;
      }
    }
  }

  .chat-input-area {
    padding: 15px 20px;
    background-color: #ffffff;
    border-top: 1px solid #e6e6e6;

    .input-tools {
      display: flex;
      margin-bottom: 8px;

      .el-button {
        padding: 5px;
        margin-right: 5px;
      }
    }

    .message-input {
      :deep(.el-textarea__inner) {
        background-color: #f7f9fc;
        border-color: #e4e7ed;
        box-shadow: none;
        font-size: 14px;
        line-height: 1.6;

        &:focus {
          border-color: #409eff;
        }
      }
    }

    .input-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;

      .send-button {
        padding: 8px 20px;
      }
    }
  }
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  border-top: 1px solid #e6e6e6;

  .footer-content {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #666;

    .footer-links {
      margin-left: 20px;

      .el-link {
        margin-left: 15px;
        font-size: 13px;
      }
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .main-container {
    width: 100%;
    max-width: 100%;
  }
  
  .aside {
    width: 240px;
    min-width: 240px;
  }
}

@media (max-width: 768px) {
  .header {
    height: 60px;

    .logo-text {
      font-size: 16px;
    }

    .mobile-menu-btn {
      display: block;
    }

    .username {
      display: none;
    }
  }

  .aside {
    display: none;
  }

  .chat-main {
    .status-bar {
      padding: 8px 15px;

      .model-info {
        font-size: 12px;
      }
    }

    .chat-window {
      padding: 15px;

      .chat-message {
        .message-container {
          max-width: 100%;

          .message-avatar {
            margin-right: 10px;
          }

          .message-header {
            .message-role {
              font-size: 14px;
            }

            .message-time {
              font-size: 11px;
            }
          }

          .message-text {
            font-size: 14px;
          }
        }
      }
    }

    .chat-input-area {
      padding: 10px 15px;
    }
  }

  .footer {
    height: 40px;

    .footer-content {
      flex-direction: column;
      text-align: center;

      .footer-links {
        margin-left: 0;
        margin-top: 5px;

        .el-link {
          margin: 0 8px;
          font-size: 12px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .header {
    height: 50px;

    .logo-text {
      font-size: 14px;
    }
  }

  .chat-main {
    .status-bar {
      flex-wrap: wrap;

      .el-tag {
        margin-bottom: 5px;
      }

      .model-info {
        margin-bottom: 5px;
        width: 100%;
      }
    }

    .welcome-screen {
      .welcome-content {
        .quick-questions {
          .quick-question {
            font-size: 12px;
            padding: 4px 8px;
          }
        }
      }
    }

    .chat-input-area {
      .input-footer {
        .send-button {
          padding: 6px 15px;
          font-size: 13px;
        }
      }
    }
  }

  .footer {
    .footer-content {
      font-size: 12px;

      .footer-links {
        .el-link {
          font-size: 11px;
        }
      }
    }
  }
}

/* 移动端抽屉菜单 - 完全重构 */
.mobile-drawer {
  .el-drawer__header {
    display: none;
  }

  .drawer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
    background-color: #409eff;
    color: white;

    h3 {
      margin: 0;
      font-size: 18px;
    }

    .el-button {
      color: white;
    }
  }

  .drawer-content {
    padding: 20px;
    height: calc(100% - 60px);
    overflow-y: auto;

    .user-card {
      display: flex;
      align-items: center;
      padding: 15px;
      background-color: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 20px;

      .user-info {
        margin-left: 15px;
        flex: 1;

        .username {
          font-weight: 600;
          font-size: 16px;
          margin-bottom: 5px;
        }

        .user-actions {
          display: flex;
          gap: 10px;
        }
      }
    }

    .drawer-section {
      margin-bottom: 25px;

      .section-header {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 1px solid #eee;

        .section-icon {
          width: 20px;
          height: 20px;
          margin-right: 10px;
          color: #409eff;
        }

        h4 {
          margin: 0;
          font-size: 16px;
          flex: 1;
        }

        .section-actions {
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }

      .model-select {
        width: 100%;
      }

      .history-list {
        .history-item {
          display: flex;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          cursor: pointer;
          transition: background-color 0.2s;

          &:hover {
            background-color: #f9f9f9;
          }

          .history-icon {
            width: 36px;
            height: 36px;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #ecf5ff;
            border-radius: 50%;
            margin-right: 12px;

            .el-icon {
              width: 18px;
              height: 18px;
              color: #409eff;
            }
          }

          .history-content {
            flex: 1;

            .history-title {
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 4px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }

            .history-time {
              font-size: 12px;
              color: #999;
              margin-bottom: 2px;
            }

            .history-meta {
              font-size: 11px;
              color: #999;

              .message-count {
                background-color: #f0f0f0;
                padding: 2px 6px;
                border-radius: 10px;
                font-size: 10px;
              }
            }
          }

          .el-button {
            padding: 5px;
          }
        }

        .loading-history {
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 20px;
          color: #666;

          .el-icon {
            margin-right: 8px;
            animation: rotating 2s linear infinite;
          }
        }

        .empty-history {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding: 30px 0;
          color: #999;

          .empty-icon {
            width: 60px;
            height: 60px;
            margin-bottom: 15px;
            color: #ddd;
          }

          p {
            margin: 0;
            font-size: 14px;
          }
        }

        .history-pagination {
          margin-top: 15px;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 10px;

          .pagination-info {
            font-size: 12px;
            color: #666;
            text-align: center;
          }
        }
      }

      .quick-actions {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 5px;

        .el-button {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 80px;
          padding: 10px;
          background-color: #f5f7fa;
          border-radius: 8px;

          .el-icon {
            width: 24px;
            height: 24px;
            margin-bottom: 8px;
          }

          span {
            font-size: 13px;
          }
        }
      }
    }
  }
}
</style>

<style lang="scss">
.model-select-popper {
  .el-select-dropdown__item {
    height: auto;
    padding: 8px 20px;

    &.selected {
      background-color: #f5f7fa;
    }
  }

  .model-info {
    display: flex;
    flex-direction: column;

    .model-name {
      font-size: 14px;
      margin-bottom: 2px;
    }

    .model-desc {
      font-size: 12px;
      color: #999;
    }
  }

  .el-select-group__title {
    padding-left: 20px;
    font-size: 13px;
    color: #666;
  }
}

.mobile-drawer {
  .el-drawer {
    box-shadow: -5px 0 15px rgba(0, 0, 0, 0.1);
  }

  .el-drawer__body {
    padding: 0;
  }
}
</style>