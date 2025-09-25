<template>
  <div class="markdown-renderer" v-html="renderedContent"></div>
</template>

<script setup>
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

// 创建 markdown-it 实例
const md = new MarkdownIt({
  html: true,         // 允许 HTML 标签
  linkify: true,      // 自动转换链接
  typographer: true,  // 启用一些语言替换
  breaks: true       // 转换换行符为 <br>
})

const renderedContent = computed(() => {
  // 使用 markdown-it 解析 Markdown
  const rawHtml = md.render(props.content || '')
  // 使用 DOMPurify 净化 HTML，防止 XSS 攻击
  return DOMPurify.sanitize(rawHtml)
})
</script>

<style scoped>
.markdown-renderer {
  line-height: 1.6;
}

.markdown-renderer :deep(pre) {
  background-color: #f6f8fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
}

.markdown-renderer :deep(code) {
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 85%;
}

.markdown-renderer :deep(blockquote) {
  border-left: 4px solid #dfe2e5;
  color: #6a737d;
  padding: 0 1em;
  margin: 0 0 16px 0;
}

.markdown-renderer :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin-bottom: 16px;
}

.markdown-renderer :deep(th),
.markdown-renderer :deep(td) {
  padding: 6px 13px;
  border: 1px solid #dfe2e5;
}

.markdown-renderer :deep(tr:nth-child(2n)) {
  background-color: #f6f8fa;
}
</style>