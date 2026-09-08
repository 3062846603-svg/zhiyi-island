<!--
  AI摘要页面
  使用AI生成文本内容的摘要
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useAiStore } from '@/stores/ai'
import { useToastStore } from '@/stores/toast'

const router = useRouter()
const aiStore = useAiStore()
const toast = useToastStore()

/** 输入的文本内容 */
const inputContent = ref('')
/** 生成的摘要结果 */
const summaryResult = ref('')
/** 当前摘要ID */
const currentSummaryId = ref(null)
/** 是否正在生成 */
const isGenerating = ref(false)

/** 摘要选项配置 */
const summaryOptions = ref({
  length: 'medium',
  style: 'keypoints',
  language: 'chinese',
})

const generateSummary = async () => {
  if (!inputContent.value.trim()) return

  if (isGenerating.value) return

  isGenerating.value = true

  try {
    const result = await aiStore.generateSummary({
      content: inputContent.value,
      style: summaryOptions.value.style,
      length: summaryOptions.value.length,
    })

    if (result) {
      summaryResult.value =
        result.summaryContent || result.summary || result.content || result.text || ''
      currentSummaryId.value = result.id || result.summaryId
      toast.success('摘要生成成功')
    } else {
      toast.error('摘要生成失败，请稍后重试')
    }
  } catch (e) {
    toast.error('摘要生成失败')
  } finally {
    isGenerating.value = false
  }
}

const goBack = () => {
  router.push('/home')
}

const copyResult = () => {
  navigator.clipboard.writeText(summaryResult.value)
  toast.success('已复制到剪贴板')
}

const saveToNotes = async () => {
  if (!currentSummaryId.value) {
    toast.warning('请先生成摘要')
    return
  }

  try {
    const success = await aiStore.saveToNote(currentSummaryId.value)
    if (success) {
      toast.success('已保存到笔记')
    } else {
      toast.error('保存失败')
    }
  } catch (e) {
    toast.error('保存失败: ' + (e.message || '未知错误'))
  }
}

onMounted(() => {
  aiStore.fetchSummaryHistory(5)
})
</script>

<template>
  <div class="ai-summary-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <div class="header-actions">
        <button class="action-btn" @click="copyResult" :disabled="!summaryResult">
          <Icon icon="ri:file-copy-line" />
          复制
        </button>
        <button class="action-btn primary" @click="saveToNotes" :disabled="!summaryResult">
          <Icon icon="ri:save-line" />
          保存到笔记
        </button>
      </div>
    </div>

    <div class="content-grid">
      <div class="input-section">
        <div class="section-header">
          <h3><Icon icon="ri:file-text-line" /> 输入内容</h3>
          <span class="char-count">{{ inputContent.length }} 字</span>
        </div>
        <textarea
          v-model="inputContent"
          class="content-input"
          placeholder="粘贴或输入需要摘要的文章、笔记内容..."
        ></textarea>

        <div class="options-section">
          <h4>摘要选项</h4>
          <div class="options-grid">
            <div class="option-group">
              <label>摘要长度</label>
              <div class="option-buttons">
                <button
                  class="opt-btn"
                  :class="{ active: summaryOptions.length === 'short' }"
                  @click="summaryOptions.length = 'short'"
                >
                  简短
                </button>
                <button
                  class="opt-btn"
                  :class="{ active: summaryOptions.length === 'medium' }"
                  @click="summaryOptions.length = 'medium'"
                >
                  适中
                </button>
                <button
                  class="opt-btn"
                  :class="{ active: summaryOptions.length === 'detailed' }"
                  @click="summaryOptions.length = 'detailed'"
                >
                  详细
                </button>
              </div>
            </div>
            <div class="option-group">
              <label>输出风格</label>
              <div class="option-buttons">
                <button
                  class="opt-btn"
                  :class="{ active: summaryOptions.style === 'keypoints' }"
                  @click="summaryOptions.style = 'keypoints'"
                >
                  要点列表
                </button>
                <button
                  class="opt-btn"
                  :class="{ active: summaryOptions.style === 'paragraph' }"
                  @click="summaryOptions.style = 'paragraph'"
                >
                  段落形式
                </button>
              </div>
            </div>
          </div>
        </div>

        <button
          class="generate-btn"
          :class="{ loading: isGenerating }"
          :disabled="!inputContent.trim() || isGenerating"
          @click="generateSummary"
        >
          <Icon v-if="!isGenerating" icon="ri:ai-generate" />
          <Icon v-else icon="ri:loader-4-line" class="spin" />
          {{ isGenerating ? '生成中...' : '生成摘要' }}
        </button>
      </div>

      <div class="result-section">
        <div class="section-header">
          <h3><Icon icon="ri:magic-line" /> 摘要结果</h3>
        </div>
        <div class="result-content" v-if="summaryResult">
          <div
            class="result-text"
            v-html="
              summaryResult
                .replace(/\n/g, '<br>')
                .replace(/## (.*)/g, '<h4>$1</h4>')
                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
            "
          ></div>
        </div>
        <div class="empty-result" v-else>
          <Icon icon="ri:ai-generate" class="empty-icon" />
          <p>输入内容后点击"生成摘要"</p>
        </div>

        <div class="history-section">
          <h4>历史摘要</h4>
          <div class="history-list">
            <div v-for="item in aiStore.summaryHistory" :key="item.id" class="history-item">
              <div class="history-icon">
                <Icon icon="ri:file-list-3-line" />
              </div>
              <div class="history-info">
                <span class="history-title">{{ item.title }}</span>
                <span class="history-meta"
                  >{{ item.createTime?.substring(0, 10) }} · {{ item.wordCount }} 字</span
                >
              </div>
            </div>
            <div v-if="aiStore.summaryHistory.length === 0" class="empty-history">暂无历史摘要</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-summary-page {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f9fafb;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
  color: white;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.input-section,
.result-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.char-count {
  font-size: 13px;
  color: #9ca3af;
}

.content-input {
  width: 100%;
  height: 180px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
  transition: border-color 0.2s;
}

.content-input:focus {
  border-color: var(--primary-color);
}

.options-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f3f4f6;
}

.options-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
}

.options-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-group label {
  display: block;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 8px;
}

.option-buttons {
  display: flex;
  gap: 8px;
}

.opt-btn {
  padding: 8px 16px;
  background: #f3f4f6;
  border: none;
  border-radius: 8px;
  color: #6b7280;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.opt-btn:hover {
  background: #e5e7eb;
}

.opt-btn.active {
  background: var(--primary-color);
  color: white;
}

.generate-btn {
  width: 100%;
  margin-top: 20px;
  padding: 14px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
}

.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.generate-btn.loading {
  background: #9ca3af;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.result-content {
  min-height: 150px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 24px;
}

.result-text {
  font-size: 14px;
  color: #374151;
  line-height: 1.8;
}

.result-text :deep(h4) {
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin: 16px 0 8px 0;
}

.result-text :deep(h4:first-child) {
  margin-top: 0;
}

.empty-result {
  min-height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.history-section {
  border-top: 1px solid #f3f4f6;
  padding-top: 20px;
}

.history-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-item:hover {
  background: #f3f4f6;
}

.history-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
}

.history-info {
  display: flex;
  flex-direction: column;
}

.history-title {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.history-meta {
  font-size: 12px;
  color: #9ca3af;
}

.empty-history {
  text-align: center;
  padding: 20px;
  color: #9ca3af;
  font-size: 14px;
}
</style>
