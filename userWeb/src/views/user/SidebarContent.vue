<template>
  <div class="sidebar-content">
    <div class="model-selector">
      <h4 class="section-title">
        <svg-icon icon-class="model" class="section-icon" />
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
    </div>

    <div class="history-section">
      <h4 class="section-title">
        <svg-icon icon-class="history" class="section-icon" />
        历史对话
      </h4>
      <div class="history-list">
        <div
            v-for="(item, index) in historyList"
            :key="index"
            class="history-item"
            @click="loadHistory(item.id)"
        >
          <div class="history-title">{{ item.title }}</div>
          <div class="history-time">{{ item.time }}</div>
        </div>
        <div v-if="historyList.length === 0" class="empty-history">
          <svg-icon icon-class="empty" class="empty-icon" />
          <p>暂无历史对话</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import SvgIcon from '@/components/SvgIcon.vue'

const props = defineProps({
  selectedModel: {
    type: String,
    default: ''
  },
  modelGroups: {
    type: Array,
    default: () => []
  },
  historyList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:selectedModel', 'loadHistory'])

const selectedModel = ref(props.selectedModel)

watch(selectedModel, (val) => {
  emit('update:selectedModel', val)
})

const loadHistory = (id) => {
  const history = props.historyList.find(item => item.id === id)
  if (history) {
    emit('loadHistory', id)
    ElMessage.success(`已加载历史对话: ${history.title}`)
  }
}
</script>

<style lang="scss" scoped>
.sidebar-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 15px;

  .section-title {
    display: flex;
    align-items: center;
    font-size: 14px;
    color: var(--text-color-secondary);
    margin: 15px 0 10px;

    .section-icon {
      width: 16px;
      height: 16px;
      margin-right: 8px;
      color: var(--text-color-secondary);
    }
  }

  .model-select {
    width: 100%;

    :deep(.el-input__inner) {
      background-color: var(--input-bg);
      border-color: var(--border-color);
      color: var(--text-color);
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
      padding: 10px;
      border-radius: 6px;
      margin-bottom: 8px;
      cursor: pointer;
      transition: all 0.2s;
      background-color: var(--input-bg);

      &:hover {
        background-color: var(--hover-color);
      }

      .history-title {
        font-size: 13px;
        color: var(--text-color);
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .history-time {
        font-size: 11px;
        color: var(--text-color-secondary);
      }
    }

    .empty-history {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: var(--text-color-secondary);

      .empty-icon {
        width: 50px;
        height: 50px;
        margin-bottom: 10px;
        color: var(--border-color);
      }

      p {
        margin: 0;
        font-size: 13px;
      }
    }
  }
}
</style>