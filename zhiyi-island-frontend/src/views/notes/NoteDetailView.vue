<!--
  笔记详情页面
  展示笔记的完整内容，包括 AI 摘要
-->
<script setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { useNoteStore } from '@/stores/note'
  import { useToastStore } from '@/stores/toast'

  const route = useRoute()
  const router = useRouter()
  const noteStore = useNoteStore()
  const toast = useToastStore()

  /** 当前笔记 */
  const note = computed(() => noteStore.currentNote)
  /** 加载状态 */
  const loading = computed(() => noteStore.loading)
  /** AI摘要生成中状态 */
  const generatingSummary = computed(() => noteStore.generatingSummary)

  /** 删除确认弹窗显示状态 */
  const showDeleteConfirm = ref(false)
  /** 图片预览弹窗显示状态 */
  const showImagePreview = ref(false)
  /** 预览图片URL */
  const previewImageUrl = ref('')
  /** AI摘要选项弹窗显示状态 */
  const showSummaryOptions = ref(false)
  /** AI摘要选项配置 */
  const summaryOptions = ref({
    style: 'keypoints',
    length: 'medium'
  })

  /**
   * 获取分类样式配置
   * @param {string} category - 分类名称
   * @returns {Object} 样式配置对象
   */
  const getCategoryStyle = (category) => {
    const styles = {
      '学习笔记': {
        icon: 'ri:book-open-line',
        gradient: 'linear-gradient(135deg, #4F7CFF 0%, #3B5FD9 100%)',
        color: '#4F7CFF',
        bg: '#EEF2FF'
      },
      '工作记录': {
        icon: 'ri:briefcase-line',
        gradient: 'linear-gradient(135deg, #6366F1 0%, #4F46E5 100%)',
        color: '#6366F1',
        bg: '#EEF2FF'
      },
      '生活随笔': {
        icon: 'ri:heart-line',
        gradient: 'linear-gradient(135deg, #F472B6 0%, #EC4899 100%)',
        color: '#EC4899',
        bg: '#FDF2F8'
      },
      '技术文档': {
        icon: 'ri:code-s-slash-line',
        gradient: 'linear-gradient(135deg, #22C55E 0%, #16A34A 100%)',
        color: '#22C55E',
        bg: '#F0FDF4'
      },
      '读书笔记': {
        icon: 'ri:book-2-line',
        gradient: 'linear-gradient(135deg, #FB923C 0%, #F97316 100%)',
        color: '#F97316',
        bg: '#FFF7ED'
      },
      '项目总结': {
        icon: 'ri:folder-chart-line',
        gradient: 'linear-gradient(135deg, #38BDF8 0%, #0EA5E9 100%)',
        color: '#0EA5E9',
        bg: '#F0F9FF'
      },
      '会议记录': {
        icon: 'ri:team-line',
        gradient: 'linear-gradient(135deg, #A78BFA 0%, #8B5CF6 100%)',
        color: '#8B5CF6',
        bg: '#F5F3FF'
      },
      '其他': {
        icon: 'ri:file-text-line',
        gradient: 'linear-gradient(135deg, #94A3B8 0%, #64748B 100%)',
        color: '#64748B',
        bg: '#F8FAFC'
      },
    }
    return styles[category] || {
      icon: 'ri:file-text-line',
      gradient: 'linear-gradient(135deg, #94A3B8 0%, #64748B 100%)',
      color: '#64748B',
      bg: '#F8FAFC'
    }
  }

  const getCategoryColor = (category) => {
    return getCategoryStyle(category)
  }

  /**
   * 格式化日期显示
   * @param {string} dateStr - 日期字符串
   * @returns {string} 格式化后的日期
   */
  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  /**
   * 返回上一页
   */
  const goBack = () => {
    router.back()
  }

  /**
   * 编辑当前笔记
   */
  const editNote = () => {
    router.push({ path: '/home/add-note', query: { id: note.value.id } })
  }

  /**
   * 显示删除确认弹窗
   */
  const confirmDelete = () => {
    showDeleteConfirm.value = true
  }

  /**
   * 删除当前笔记
   */
  const deleteNote = async () => {
    if (!note.value) return
    const result = await noteStore.deleteNote(note.value.id)
    if (result) {
      toast.success('删除成功')
      router.push('/notes')
    }
    showDeleteConfirm.value = false
  }

  /**
   * 打开AI摘要选项弹窗
   */
  const openSummaryOptions = () => {
    showSummaryOptions.value = true
  }

  /**
   * 生成AI摘要
   */
  const generateSummary = async () => {
    if (!note.value) return
    showSummaryOptions.value = false
    try {
      await noteStore.generateSummary(note.value.id, summaryOptions.value)
      toast.success('摘要生成成功')
    } catch (error) {
      toast.error('摘要生成失败')
    }
  }

  /**
   * 打开图片预览
   * @param {string} url - 图片URL
   */
  const openImagePreview = (url) => {
    previewImageUrl.value = url
    showImagePreview.value = true
  }

  /**
   * 关闭图片预览
   */
  const closeImagePreview = () => {
    showImagePreview.value = false
    previewImageUrl.value = ''
  }

  /**
   * 格式化AI摘要内容
   * @param {Object} aiSummary - AI摘要对象
   * @returns {string} 格式化后的摘要内容
   */
  const formatSummary = (aiSummary) => {
    if (!aiSummary) return ''
    const content = aiSummary.summaryContent || aiSummary.summary || ''
    return content.replace(/\n/g, '<br>')
  }

  /**
   * 获取摘要风格的中文名称
   * @param {string} style - 风格代码
   * @returns {string} 中文名称
   */
  const getStyleLabel = (style) => {
    const labels = {
      'keypoints': '要点提炼',
      'paragraph': '段落总结',
      'outline': '大纲梳理'
    }
    return labels[style] || style
  }

  /**
   * 获取摘要长度的中文名称
   * @param {string} length - 长度代码
   * @returns {string} 中文名称
   */
  const getLengthLabel = (length) => {
    const labels = {
      'short': '简短',
      'medium': '适中',
      'long': '详细'
    }
    return labels[length] || length
  }

  onMounted(async () => {
    const id = route.params.id
    if (id) {
      await noteStore.fetchNote(id)
      if (noteStore.checkGeneratingStatus(parseInt(id))) {
        noteStore.setGeneratingStatus(true)
      }
    }
  })

  onUnmounted(() => {
    noteStore.clearCurrentNote()
  })
</script>

<template>
  <div class="note-detail-page" v-if="!loading && note">
    <div class="detail-header">
      <div class="header-row">
        <button class="btn btn-secondary" @click="goBack">
          <Icon icon="ri:arrow-left-line" />
          返回
        </button>
        <div class="header-actions">
          <button class="btn btn-secondary" @click="editNote">
            <Icon icon="ri:edit-line" />
            编辑
          </button>
          <button class="btn btn-danger" @click="confirmDelete">
            <Icon icon="ri:delete-bin-line" />
            删除
          </button>
        </div>
      </div>
      <div class="title-section">
        <div class="title-left">
          <div class="category-badge" :style="{ background: getCategoryColor(note.category).gradient }">
            <Icon :icon="getCategoryStyle(note.category).icon" />
            {{ note.category || '未分类' }}
          </div>
          <h1 class="note-title">{{ note.title }}</h1>
          <div class="meta-info">
            <span class="meta-item">
              <Icon icon="ri:calendar-line" />
              {{ formatDate(note.createTime) }}
            </span>
            <span class="meta-item" v-if="note.updateTime !== note.createTime">
              <Icon icon="ri:edit-line" />
              更新于 {{ formatDate(note.updateTime) }}
            </span>
          </div>
        </div>
        <button class="btn btn-primary" @click="openSummaryOptions" :disabled="generatingSummary"
          v-if="!note.aiSummary">
          <Icon :icon="generatingSummary ? 'ri:loader-4-line' : 'ri:magic-line'" :class="{ spin: generatingSummary }" />
          {{ generatingSummary ? '生成中...' : 'AI摘要' }}
        </button>
        <button class="btn btn-secondary" @click="openSummaryOptions" :disabled="generatingSummary" v-else>
          <Icon :icon="generatingSummary ? 'ri:loader-4-line' : 'ri:refresh-line'"
            :class="{ spin: generatingSummary }" />
          {{ generatingSummary ? '生成中...' : '重新生成' }}
        </button>
      </div>
    </div>

    <div class="detail-body">
      <div class="main-content-area">
        <div class="note-content-section">
          <div class="content-header">
            <Icon icon="ri:file-text-line" />
            笔记内容
          </div>
          <div class="content-body" v-html="note.content"></div>
        </div>

        <div class="images-section" v-if="note.images && note.images.length">
          <div class="images-header">
            <Icon icon="ri:image-line" />
            笔记图片
          </div>
          <div class="images-list">
            <div v-for="(imageUrl, index) in note.images" :key="index" class="image-item">
              <img :src="imageUrl" :alt="`图片${index + 1}`" @click="openImagePreview(imageUrl)" />
            </div>
          </div>
        </div>
      </div>

      <div class="summary-column" v-if="note.aiSummary">
        <div class="ai-summary-section" :key="note.aiSummary.id || note.aiSummary.createTime">
          <div class="summary-header">
            <Icon icon="ri:robot-line" />
            AI 摘要
          </div>
          <div class="summary-content" v-html="formatSummary(note.aiSummary)"></div>
          <div class="summary-meta" v-if="note.aiSummary.summaryStyle || note.aiSummary.summaryLength">
            <span class="meta-tag" v-if="note.aiSummary.summaryStyle">{{ getStyleLabel(note.aiSummary.summaryStyle)
              }}</span>
            <span class="meta-tag" v-if="note.aiSummary.summaryLength">{{ getLengthLabel(note.aiSummary.summaryLength)
              }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="delete-modal" v-if="showDeleteConfirm" @click.self="showDeleteConfirm = false">
      <div class="modal-content">
        <div class="modal-icon">
          <Icon icon="ri:alert-line" />
        </div>
        <h3>确认删除</h3>
        <p>确定要删除这篇笔记吗？此操作无法撤销。</p>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="showDeleteConfirm = false">取消</button>
          <button class="btn btn-danger" @click="deleteNote">确认删除</button>
        </div>
      </div>
    </div>

    <div class="image-preview-modal" v-if="showImagePreview" @click="closeImagePreview">
      <div class="preview-container">
        <img :src="previewImageUrl" alt="图片预览" />
        <button class="close-preview-btn" @click="closeImagePreview">
          <Icon icon="ri:close-line" />
        </button>
      </div>
    </div>

    <div class="summary-options-modal" v-if="showSummaryOptions" @click.self="showSummaryOptions = false">
      <div class="summary-modal-content">
        <div class="summary-modal-header">
          <Icon icon="ri:magic-line" class="modal-icon-ai" />
          <h3>AI摘要选项</h3>
        </div>

        <div class="summary-options-body">
          <div class="option-group">
            <label>摘要风格</label>
            <div class="option-cards">
              <div class="option-card" :class="{ active: summaryOptions.style === 'keypoints' }"
                @click="summaryOptions.style = 'keypoints'">
                <Icon icon="ri:list-check-2" />
                <span>要点列表</span>
                <p>以要点形式呈现核心内容</p>
              </div>
              <div class="option-card" :class="{ active: summaryOptions.style === 'paragraph' }"
                @click="summaryOptions.style = 'paragraph'">
                <Icon icon="ri:text" />
                <span>段落形式</span>
                <p>以连贯段落描述主要内容</p>
              </div>
              <div class="option-card" :class="{ active: summaryOptions.style === 'outline' }"
                @click="summaryOptions.style = 'outline'">
                <Icon icon="ri:node-tree" />
                <span>大纲形式</span>
                <p>以层级结构展示内容框架</p>
              </div>
            </div>
          </div>

          <div class="option-group">
            <label>摘要长度</label>
            <div class="length-options">
              <button class="length-btn" :class="{ active: summaryOptions.length === 'short' }"
                @click="summaryOptions.length = 'short'">
                简短
              </button>
              <button class="length-btn" :class="{ active: summaryOptions.length === 'medium' }"
                @click="summaryOptions.length = 'medium'">
                适中
              </button>
              <button class="length-btn" :class="{ active: summaryOptions.length === 'detailed' }"
                @click="summaryOptions.length = 'detailed'">
                详细
              </button>
            </div>
          </div>
        </div>

        <div class="summary-modal-actions">
          <button class="btn btn-secondary" @click="showSummaryOptions = false">取消</button>
          <button class="btn btn-primary" @click="generateSummary" :disabled="generatingSummary">
            <Icon :icon="generatingSummary ? 'ri:loader-4-line' : 'ri:sparkling-line'"
              :class="{ spin: generatingSummary }" />
            {{ generatingSummary ? '生成中...' : '开始生成' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <div class="loading-state" v-else-if="loading">
    <Icon icon="ri:loader-4-line" class="loading-icon" />
    <p>加载中...</p>
  </div>

  <div class="empty-state" v-else>
    <Icon icon="ri:file-unread-line" class="empty-icon" />
    <p>笔记不存在或已被删除</p>
    <button class="btn btn-primary" @click="router.push('/notes')">返回笔记列表</button>
  </div>
</template>

<style scoped>
  .note-detail-page {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .detail-header {
    padding: 24px 0;
  }

  .header-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }

  .title-section {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 24px;
  }

  .title-left {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .category-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    border-radius: 20px;
    color: white;
    font-size: 13px;
    font-weight: 500;
    width: fit-content;
    box-shadow:
      0 4px 6px rgba(0, 0, 0, 0.1),
      0 2px 4px rgba(0, 0, 0, 0.06),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
    transition: all 0.2s ease;
  }

  .category-badge:hover {
    transform: translateY(-4px);
    box-shadow:
      0 8px 12px rgba(0, 0, 0, 0.15),
      0 4px 6px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.3);
  }

  .note-title {
    font-size: 28px;
    font-weight: 700;
    color: #1f2937;
    line-height: 1.3;
    margin: 0;
  }

  .meta-info {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #6b7280;
  }

  .header-actions {
    display: flex;
    gap: 12px;
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

  .detail-body {
    display: flex;
    gap: 24px;
    align-items: flex-start;
  }

  .summary-column {
    width: 320px;
    flex-shrink: 0;
  }

  .main-content-area {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .ai-summary-section {
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    border-radius: 12px;
    padding: 20px;
    position: sticky;
    top: 20px;
    animation: fadeIn 0.3s ease;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-10px);
    }

    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .summary-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--primary-color);
    margin-bottom: 12px;
  }

  .summary-content {
    font-size: 14px;
    line-height: 1.8;
    color: #0D1F5C;
  }

  .summary-meta {
    display: flex;
    gap: 8px;
    margin-top: 16px;
    flex-wrap: wrap;
  }

  .meta-tag {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    background: rgba(79, 124, 255, 0.15);
    color: var(--primary-color);
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
  }

  .note-content-section {
    background: white;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  }

  .content-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }

  .content-body {
    font-size: 15px;
    line-height: 1.9;
    color: #374151;
    white-space: pre-wrap;
    word-wrap: break-word;
    word-break: break-word;
  }

  .content-body :deep(p) {
    margin-bottom: 16px;
  }

  .content-body :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
    margin: 16px 0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .content-body :deep(h1),
  .content-body :deep(h2),
  .content-body :deep(h3),
  .content-body :deep(h4),
  .content-body :deep(h5),
  .content-body :deep(h6) {
    margin: 24px 0 16px 0;
    font-weight: 600;
    color: #1f2937;
  }

  .content-body :deep(ul),
  .content-body :deep(ol) {
    margin: 16px 0;
    padding-left: 24px;
  }

  .content-body :deep(li) {
    margin-bottom: 8px;
  }

  .content-body :deep(blockquote) {
    margin: 16px 0;
    padding: 12px 16px;
    border-left: 4px solid var(--primary-color);
    background: #f9fafb;
    color: #4b5563;
  }

  .content-body :deep(code) {
    padding: 2px 6px;
    background: #f3f4f6;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 14px;
  }

  .content-body :deep(pre) {
    margin: 16px 0;
    padding: 16px;
    background: #1f2937;
    border-radius: 8px;
    overflow-x: auto;
  }

  .content-body :deep(pre code) {
    padding: 0;
    background: transparent;
    color: #e5e7eb;
  }

  .content-body :deep(a) {
    color: var(--primary-color);
    text-decoration: none;
  }

  .content-body :deep(a:hover) {
    text-decoration: underline;
  }

  .content-body :deep(table) {
    width: 100%;
    margin: 16px 0;
    border-collapse: collapse;
  }

  .content-body :deep(th),
  .content-body :deep(td) {
    padding: 12px;
    border: 1px solid #e5e7eb;
    text-align: left;
  }

  .content-body :deep(th) {
    background: #f9fafb;
    font-weight: 600;
  }

  .images-section {
    background: white;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  }

  .images-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 16px;
  }

  .images-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }

  .image-item {
    position: relative;
    border-radius: 10px;
    overflow: hidden;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s, box-shadow 0.2s;
    background: #f3f4f6;
  }

  .image-item:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  }

  .image-item img {
    width: 100%;
    height: auto;
    display: block;
  }

  .delete-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }

  .modal-content {
    background: white;
    border-radius: 16px;
    padding: 32px;
    max-width: 400px;
    text-align: center;
  }

  .modal-icon {
    width: 64px;
    height: 64px;
    background: #fef2f2;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 32px;
    color: #ef4444;
  }

  .modal-content h3 {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }

  .modal-content p {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: 24px;
  }

  .modal-actions {
    display: flex;
    justify-content: space-between;
    gap: 12px;
  }

  .image-preview-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.9);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1001;
    cursor: pointer;
  }

  .preview-container {
    position: relative;
    max-width: 90vw;
    max-height: 90vh;
  }

  .preview-container img {
    max-width: 100%;
    max-height: 90vh;
    border-radius: 8px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  }

  .close-preview-btn {
    position: absolute;
    top: -40px;
    right: 0;
    width: 36px;
    height: 36px;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    border-radius: 50%;
    color: white;
    font-size: 24px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
  }

  .close-preview-btn:hover {
    background: rgba(255, 255, 255, 0.3);
  }

  .loading-state,
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
    text-align: center;
  }

  .loading-icon,
  .empty-icon {
    font-size: 48px;
    color: #9ca3af;
    margin-bottom: 16px;
  }

  .loading-icon {
    animation: spin 1s linear infinite;
  }

  .loading-state p,
  .empty-state p {
    font-size: 16px;
    color: #6b7280;
    margin-bottom: 16px;
  }

  @media (max-width: 900px) {
    .detail-body {
      flex-direction: column;
    }

    .summary-column {
      width: 100%;
      order: -1;
    }

    .ai-summary-section {
      position: static;
    }

    .images-list {
      grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
      gap: 12px;
    }
  }

  @media (max-width: 768px) {
    .header-row {
      flex-wrap: wrap;
      gap: 12px;
    }

    .header-actions {
      width: 100%;
      justify-content: flex-end;
    }

    .title-section {
      flex-direction: column;
      gap: 16px;
    }

    .note-title {
      font-size: 22px;
    }

    .note-content-section {
      padding: 20px;
    }
  }

  .summary-options-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1002;
  }

  .summary-modal-content {
    background: white;
    border-radius: 20px;
    padding: 32px;
    max-width: 560px;
    width: 90%;
    max-height: 90vh;
    overflow-y: auto;
  }

  .summary-modal-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 24px;
  }

  .modal-icon-ai {
    font-size: 28px;
    color: var(--primary-color);
  }

  .summary-modal-header h3 {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
  }

  .summary-options-body {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .option-group label {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12px;
  }

  .option-cards {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .option-card {
    padding: 16px 12px;
    border: 2px solid #e5e7eb;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
  }

  .option-card:hover {
    border-color: var(--primary-color);
    background: #f0f4ff;
  }

  .option-card.active {
    border-color: var(--primary-color);
    background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
  }

  .option-card .iconify {
    font-size: 24px;
    color: var(--primary-color);
    margin-bottom: 8px;
  }

  .option-card span {
    display: block;
    font-size: 14px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .option-card p {
    font-size: 12px;
    color: #6b7280;
    margin: 0;
    line-height: 1.4;
  }

  .length-options {
    display: flex;
    gap: 12px;
  }

  .length-btn {
    flex: 1;
    padding: 12px 16px;
    border: 2px solid #e5e7eb;
    border-radius: 10px;
    background: white;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    cursor: pointer;
    transition: all 0.2s;
  }

  .length-btn:hover {
    border-color: var(--primary-color);
    background: #f0f4ff;
  }

  .length-btn.active {
    border-color: var(--primary-color);
    background: var(--primary-color);
    color: white;
  }

  .summary-modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #e5e7eb;
  }

  @media (max-width: 560px) {
    .option-cards {
      grid-template-columns: 1fr;
    }
  }
</style>