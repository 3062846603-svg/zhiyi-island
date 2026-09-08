<!--
  导出数据页面
  支持导出笔记、知识库等数据
-->
<script setup>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { exportApi } from '@/api/export'
  import { useToastStore } from '@/stores/toast'

  const router = useRouter()
  const toast = useToastStore()

  /** 导出选项 */
  const exportOptions = ref({
    notes: true,
    knowledge: true,
    aiSummaries: true,
  })

  const exportFormat = ref('json')
  const isExporting = ref(false)
  const exportHistory = ref([])

  const goBack = () => {
    router.back()
  }

  const handleExport = async () => {
    const hasSelection = Object.values(exportOptions.value).some((v) => v)
    if (!hasSelection) {
      toast.warning('请至少选择一项导出内容')
      return
    }

    isExporting.value = true
    try {
      const fileName = `知忆岛数据导出_${new Date().toISOString().slice(0, 10)}.${exportFormat.value}`

      await exportApi.downloadExport(
        {
          notes: exportOptions.value.notes,
          knowledge: exportOptions.value.knowledge,
          aiSummaries: exportOptions.value.aiSummaries,
          format: exportFormat.value,
        },
        fileName,
      )

      toast.success('数据导出成功！')
      loadExportHistory()
    } catch (e) {
      toast.error('导出失败: ' + (e.message || '未知错误'))
    } finally {
      isExporting.value = false
    }
  }

  const loadExportHistory = async () => {
    try {
      const result = await exportApi.getHistory()
      if (result.code === 200) {
        exportHistory.value = result.data.map((item) => ({
          date: item.createTime?.replace('T', ' ').substring(0, 16) || '',
          type: item.exportType || '全部数据',
          size: formatSize(item.fileSize || 0),
          format: item.format?.toUpperCase() || 'JSON',
        }))
      }
    } catch (e) {
      console.error('获取导出历史失败:', e)
    }
  }

  const formatSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  }

  onMounted(() => {
    loadExportHistory()
  })
</script>

<template>
  <div class="export-page">
    <div class="page-header">
      <button class="btn btn-secondary" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <h2>导出数据</h2>
      <div class="placeholder"></div>
    </div>

    <div class="export-content">
      <div class="export-card">
        <h3 class="card-title">
          <Icon icon="ri:file-list-3-line" />
          选择导出内容
        </h3>
        <div class="option-list">
          <label class="option-item">
            <input type="checkbox" v-model="exportOptions.notes" />
            <div class="option-info">
              <span class="option-label">笔记数据</span>
              <span class="option-desc">包含所有笔记、标注和高亮</span>
            </div>
          </label>
          <label class="option-item">
            <input type="checkbox" v-model="exportOptions.knowledge" />
            <div class="option-info">
              <span class="option-label">知识库</span>
              <span class="option-desc">包含知识图谱和知识条目</span>
            </div>
          </label>
          <label class="option-item">
            <input type="checkbox" v-model="exportOptions.aiSummaries" />
            <div class="option-info">
              <span class="option-label">AI摘要</span>
              <span class="option-desc">包含所有AI生成的摘要</span>
            </div>
          </label>
        </div>
      </div>

      <div class="export-card">
        <h3 class="card-title">
          <Icon icon="ri:file-code-line" />
          导出格式
        </h3>
        <div class="format-options">
          <label class="format-item" :class="{ active: exportFormat === 'json' }">
            <input type="radio" v-model="exportFormat" value="json" />
            <Icon icon="ri:braces-line" class="format-icon" />
            <span class="format-name">JSON</span>
            <span class="format-desc">结构化数据，适合备份和迁移</span>
          </label>
          <label class="format-item" :class="{ active: exportFormat === 'markdown' }">
            <input type="radio" v-model="exportFormat" value="markdown" />
            <Icon icon="ri:markdown-line" class="format-icon" />
            <span class="format-name">Markdown</span>
            <span class="format-desc">纯文本格式，适合阅读和编辑</span>
          </label>
          <label class="format-item" :class="{ active: exportFormat === 'pdf' }">
            <input type="radio" v-model="exportFormat" value="pdf" />
            <Icon icon="ri:file-pdf-line" class="format-icon" />
            <span class="format-name">PDF</span>
            <span class="format-desc">文档格式，适合打印和分享</span>
          </label>
        </div>
      </div>

      <div class="export-card">
        <h3 class="card-title">
          <Icon icon="ri:history-line" />
          导出历史
        </h3>
        <div class="history-list">
          <div v-for="(item, index) in exportHistory" :key="index" class="history-item">
            <div class="history-info">
              <span class="history-type">{{ item.type }}</span>
              <span class="history-date">{{ item.date }}</span>
            </div>
            <div class="history-meta">
              <span class="history-size">{{ item.size }}</span>
              <span class="history-format">{{ item.format }}</span>
            </div>
          </div>
        </div>
      </div>

      <button class="export-btn" :disabled="isExporting" @click="handleExport">
        <Icon :icon="isExporting ? 'ri:loader-4-line' : 'ri:download-line'" :class="{ spinning: isExporting }" />
        {{ isExporting ? '导出中...' : '开始导出' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
  .export-page {
    height: calc(100vh - 108px);
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    flex-shrink: 0;
  }

  .page-header h2 {
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
  }

  .placeholder {
    width: 80px;
  }

  .export-content {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .export-card {
    background: white;
    border-radius: 16px;
    padding: 24px;
  }

  .card-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 16px;
  }

  .option-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .option-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
    background: #f9fafb;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .option-item:hover {
    background: #f3f4f6;
  }

  .option-item input {
    margin-top: 2px;
    width: 18px;
    height: 18px;
    accent-color: var(--primary-color);
  }

  .option-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .option-label {
    font-size: 14px;
    font-weight: 500;
    color: #374151;
  }

  .option-desc {
    font-size: 13px;
    color: #9ca3af;
  }

  .format-options {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .format-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px 16px;
    background: #f9fafb;
    border: 2px solid transparent;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: center;
  }

  .format-item:hover {
    background: #f3f4f6;
  }

  .format-item.active {
    background: #f0f3ff;
    border-color: var(--primary-color);
  }

  .format-item input {
    display: none;
  }

  .format-icon {
    font-size: 28px;
    color: var(--primary-color);
  }

  .format-name {
    font-size: 14px;
    font-weight: 500;
    color: #374151;
  }

  .format-desc {
    font-size: 12px;
    color: #9ca3af;
  }

  .history-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #f9fafb;
    border-radius: 8px;
  }

  .history-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .history-type {
    font-size: 14px;
    font-weight: 500;
    color: #374151;
  }

  .history-date {
    font-size: 12px;
    color: #9ca3af;
  }

  .history-meta {
    display: flex;
    gap: 12px;
  }

  .history-size,
  .history-format {
    font-size: 13px;
    color: #6b7280;
    background: white;
    padding: 4px 8px;
    border-radius: 6px;
  }

  .export-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 16px 24px;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    border: none;
    border-radius: 12px;
    color: white;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    margin-top: auto;
  }

  .export-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  }

  .export-btn:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .spinning {
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
</style>