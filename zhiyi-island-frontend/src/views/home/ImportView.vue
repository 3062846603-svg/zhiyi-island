<script setup>
  import { ref, computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { importApi } from '@/api/import'
  import { useToastStore } from '@/stores/toast'

  const router = useRouter()
  const toast = useToastStore()

  const uploadedFiles = ref([])
  const isDragging = ref(false)
  const isImporting = ref(false)
  const aiSummary = ref(false)

  const pendingFiles = computed(() => uploadedFiles.value.filter((f) => f.status === 'pending'))
  const completedFiles = computed(() => uploadedFiles.value.filter((f) => f.status === 'success'))

  const SUPPORTED_EXTENSIONS = /\.(pdf|docx|txt|md|epub)$/i

  const formatFileSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  }

  const getFileIcon = (name) => {
    const ext = name.split('.').pop().toLowerCase()
    const iconMap = {
      pdf: 'ri:file-pdf-2-line',
      docx: 'ri:file-word-line',
      txt: 'ri:file-text-line',
      md: 'ri:markdown-line',
      epub: 'ri:book-open-line',
    }
    return iconMap[ext] || 'ri:file-line'
  }

  const getFileTypeColor = (name) => {
    const ext = name.split('.').pop().toLowerCase()
    const colorMap = {
      pdf: '#ef4444',
      docx: '#3b82f6',
      txt: '#6b7280',
      md: '#8b5cf6',
      epub: '#10b981',
    }
    return colorMap[ext] || '#6b7280'
  }

  const handleDragOver = () => {
    isDragging.value = true
  }

  const handleDragLeave = () => {
    isDragging.value = false
  }

  const handleDrop = (e) => {
    isDragging.value = false
    addFiles(e.dataTransfer.files)
  }

  const handleFileSelect = (e) => {
    addFiles(e.target.files)
    e.target.value = ''
  }

  const addFiles = (files) => {
    for (const file of files) {
      if (!SUPPORTED_EXTENSIONS.test(file.name)) {
        toast.warning(`不支持的文件格式: ${file.name}`)
        continue
      }
      if (file.size > 10 * 1024 * 1024) {
        toast.warning(`文件大小超过10MB限制: ${file.name}`)
        continue
      }
      if (uploadedFiles.value.some((f) => f.name === file.name)) {
        toast.warning(`文件已存在: ${file.name}`)
        continue
      }
      uploadedFiles.value.push({
        id: Date.now() + Math.random(),
        name: file.name,
        size: formatFileSize(file.size),
        file: file,
        status: 'pending',
        progress: 0,
      })
    }
  }

  const removeFile = (id) => {
    uploadedFiles.value = uploadedFiles.value.filter((f) => f.id !== id)
  }

  const clearCompleted = () => {
    uploadedFiles.value = uploadedFiles.value.filter((f) => f.status !== 'success' && f.status !== 'error')
  }

  const goBack = () => {
    router.back()
  }

  const importFiles = async () => {
    const filesToImport = uploadedFiles.value.filter((f) => f.status === 'pending')
    if (filesToImport.length === 0) {
      toast.warning('请先选择要导入的文件')
      return
    }

    isImporting.value = true

    for (const fileItem of filesToImport) {
      fileItem.status = 'processing'
      fileItem.progress = 0

      try {
        const result = await importApi.importFile(fileItem.file, {
          aiSummary: aiSummary.value,
        })

        if (result.code === 200) {
          fileItem.status = 'success'
          fileItem.progress = 100
          fileItem.recordId = result.data.id
          fileItem.noteCount = result.data.noteCount
          fileItem.knowledgeCount = result.data.knowledgeCount
        } else {
          fileItem.status = 'error'
          fileItem.error = result.message
        }
      } catch (e) {
        fileItem.status = 'error'
        fileItem.error = e.message || '导入失败'
      }
    }

    isImporting.value = false

    const successCount = uploadedFiles.value.filter((f) => f.status === 'success').length
    const errorCount = uploadedFiles.value.filter((f) => f.status === 'error').length

    if (successCount > 0) {
      toast.success(`成功导入 ${successCount} 个文件`)
    }
    if (errorCount > 0) {
      toast.error(`导入失败 ${errorCount} 个文件`)
    }
  }
</script>

<template>
  <div class="import-page">
    <div class="page-header">
      <button class="btn-back" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        <span>返回</span>
      </button>
      <h2 class="page-title">导入文件</h2>
    </div>

    <div class="import-layout">
      <div class="import-main">
        <div class="upload-zone" :class="{ dragging: isDragging }" @dragover.prevent="handleDragOver"
          @dragleave="handleDragLeave" @drop.prevent="handleDrop" @click="$refs.fileInput.click()">
          <input type="file" ref="fileInput" multiple accept=".pdf,.docx,.txt,.md,.epub" style="display: none"
            @change="handleFileSelect" />
          <div class="upload-visual">
            <div class="upload-icon-wrap">
              <Icon icon="ri:upload-cloud-2-line" />
            </div>
            <div class="upload-text">
              <h3>拖拽文件到此处，或点击上传</h3>
              <p>AI 将自动解析内容、生成标题和分类，并创建知识库</p>
            </div>
          </div>
          <div class="format-tags">
            <span class="tag tag-pdf">PDF</span>
            <span class="tag tag-docx">DOCX</span>
            <span class="tag tag-txt">TXT</span>
            <span class="tag tag-md">MD</span>
            <span class="tag tag-epub">EPUB</span>
            <span class="tag-limit">单文件最大 10MB</span>
          </div>
        </div>

        <div class="files-section" v-if="uploadedFiles.length > 0">
          <div class="section-bar">
            <h3>文件列表</h3>
            <div class="section-actions">
              <span class="file-count">{{ uploadedFiles.length }} 个文件</span>
              <button v-if="completedFiles.length > 0" class="btn-text" @click="clearCompleted">
                清除已完成
              </button>
            </div>
          </div>

          <div class="files-list">
            <div v-for="file in uploadedFiles" :key="file.id" class="file-card" :class="file.status">
              <div class="file-icon-box" :style="{ background: getFileTypeColor(file.name) }">
                <Icon :icon="getFileIcon(file.name)" />
              </div>
              <div class="file-detail">
                <div class="file-name">{{ file.name }}</div>
                <div class="file-meta-row">
                  <span class="file-size">{{ file.size }}</span>
                  <span class="file-status-badge" :class="file.status">
                    <Icon v-if="file.status === 'success'" icon="ri:check-line" />
                    <Icon v-else-if="file.status === 'processing'" icon="ri:loader-4-line" class="spin" />
                    <Icon v-else-if="file.status === 'error'" icon="ri:close-circle-line" />
                    <Icon v-else icon="ri:time-line" />
                    {{ file.status === 'success' ? '导入成功' : file.status === 'processing' ? '处理中...' :
                    file.status === 'error' ? '导入失败' : '等待导入' }}
                  </span>
                </div>
                <div v-if="file.status === 'success' && file.noteCount" class="file-result">
                  生成 {{ file.noteCount }} 条笔记{{ file.knowledgeCount ? '，已归入知识库' : '' }}
                </div>
                <div v-if="file.status === 'processing'" class="progress-bar">
                  <div class="progress-fill" :style="{ width: file.progress + '%' }"></div>
                </div>
                <div v-if="file.status === 'error' && file.error" class="error-msg">
                  {{ file.error }}
                </div>
              </div>
              <button class="btn-remove" @click="removeFile(file.id)" v-if="file.status !== 'processing'">
                <Icon icon="ri:close-line" />
              </button>
            </div>
          </div>
        </div>

        <div class="action-bar" v-if="uploadedFiles.length > 0">
          <div class="action-info">
            <div class="info-stats">
              <span class="stat">
                <Icon icon="ri:file-list-3-line" />
                {{ uploadedFiles.length }} 个文件
              </span>
              <span v-if="completedFiles.length > 0" class="stat stat-success">
                <Icon icon="ri:checkbox-circle-line" />
                {{ completedFiles.length }} 个完成
              </span>
              <span v-if="pendingFiles.length > 0" class="stat stat-pending">
                <Icon icon="ri:time-line" />
                {{ pendingFiles.length }} 个待处理
              </span>
            </div>
          </div>
          <button class="btn-import" @click="importFiles" :disabled="isImporting || pendingFiles.length === 0">
            <Icon v-if="isImporting" icon="ri:loader-4-line" class="spin" />
            <Icon v-else icon="ri:upload-cloud-line" />
            {{ isImporting ? '导入中...' : '开始导入' }}
          </button>
        </div>
      </div>

      <div class="side-panel">
        <div class="panel-card">
          <div class="panel-title">
            <Icon icon="ri:magic-line" />
            <span>AI 智能解析</span>
          </div>
          <div class="panel-desc">
            导入文件时，AI 将自动完成以下操作：
          </div>
          <ul class="feature-list">
            <li>
              <Icon icon="ri:scissors-cut-line" />
              <div>
                <strong>智能分段</strong>
                <p>根据语义将内容拆分为多条笔记</p>
              </div>
            </li>
            <li>
              <Icon icon="ri:heading" />
              <div>
                <strong>生成标题</strong>
                <p>为每条笔记生成简洁的标题</p>
              </div>
            </li>
            <li>
              <Icon icon="ri:folder-line" />
              <div>
                <strong>自动分类</strong>
                <p>智能选择笔记和知识库分类</p>
              </div>
            </li>
            <li>
              <Icon icon="ri:book-2-line" />
              <div>
                <strong>创建知识库</strong>
                <p>自动创建知识库并关联笔记</p>
              </div>
            </li>
            <li>
              <Icon icon="ri:eraser-line" />
              <div>
                <strong>格式清理</strong>
                <p>去除 Markdown 语法，输出纯文本</p>
              </div>
            </li>
          </ul>
        </div>

        <div class="panel-card">
          <div class="panel-title">
            <Icon icon="ri:settings-4-line" />
            <span>可选功能</span>
          </div>
          <label class="toggle-item">
            <div class="toggle-info">
              <span class="toggle-label">AI 智能摘要</span>
              <span class="toggle-desc">为每条笔记生成内容摘要</span>
            </div>
            <div class="toggle-switch" :class="{ active: aiSummary }" @click="aiSummary = !aiSummary">
              <div class="toggle-knob"></div>
            </div>
          </label>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .import-page {
    width: 100%;
    max-width: 960px;
    margin: 0 auto;
    padding: 0 4px;
  }

  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 28px;
  }

  .btn-back {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    color: #6b7280;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .btn-back:hover {
    color: #374151;
    border-color: #d1d5db;
    background: #f9fafb;
  }

  .page-title {
    font-size: 20px;
    font-weight: 700;
    color: #111827;
  }

  .import-layout {
    display: flex;
    gap: 24px;
    align-items: stretch;
  }

  .import-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  .upload-zone {
    background: white;
    border: 2px dashed #d1d5db;
    border-radius: 16px;
    padding: 40px 32px;
    text-align: center;
    cursor: pointer;
    transition: all 0.25s ease;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .upload-zone:hover {
    border-color: var(--primary-color, #122E8A);
    background: #fafbff;
  }

  .upload-zone.dragging {
    border-color: var(--primary-color, #122E8A);
    background: #f0f3ff;
    transform: scale(1.005);
  }

  .upload-visual {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
  }

  .upload-icon-wrap {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, var(--primary-color, #122E8A), #1e40af);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 28px;
    box-shadow: 0 4px 14px rgba(18, 46, 138, 0.25);
  }

  .upload-text h3 {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 6px;
  }

  .upload-text p {
    font-size: 13px;
    color: #9ca3af;
  }

  .format-tags {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .tag {
    padding: 3px 10px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.3px;
  }

  .tag-pdf {
    background: #fef2f2;
    color: #ef4444;
  }

  .tag-docx {
    background: #eff6ff;
    color: #3b82f6;
  }

  .tag-txt {
    background: #f3f4f6;
    color: #6b7280;
  }

  .tag-md {
    background: #f5f3ff;
    color: #8b5cf6;
  }

  .tag-epub {
    background: #ecfdf5;
    color: #10b981;
  }

  .tag-limit {
    font-size: 11px;
    color: #d1d5db;
    margin-left: 4px;
  }

  .files-section {
    background: white;
    border-radius: 16px;
    padding: 20px 24px;
    margin-top: 20px;
  }

  .section-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14px;
  }

  .section-bar h3 {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
  }

  .section-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .file-count {
    font-size: 13px;
    color: #9ca3af;
  }

  .btn-text {
    font-size: 13px;
    color: var(--primary-color, #122E8A);
    background: none;
    border: none;
    cursor: pointer;
    padding: 0;
  }

  .btn-text:hover {
    text-decoration: underline;
  }

  .files-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .file-card {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 14px 16px;
    background: #f9fafb;
    border-radius: 12px;
    border: 1px solid transparent;
    transition: all 0.2s;
  }

  .file-card.success {
    background: #f0fdf4;
    border-color: #bbf7d0;
  }

  .file-card.error {
    background: #fef2f2;
    border-color: #fecaca;
  }

  .file-card.processing {
    background: #fffbeb;
    border-color: #fde68a;
  }

  .file-icon-box {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 17px;
    flex-shrink: 0;
  }

  .file-detail {
    flex: 1;
    min-width: 0;
  }

  .file-name {
    font-size: 14px;
    font-weight: 500;
    color: #1f2937;
    margin-bottom: 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .file-meta-row {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 12px;
  }

  .file-size {
    color: #9ca3af;
  }

  .file-status-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-weight: 500;
  }

  .file-status-badge.success {
    color: #16a34a;
  }

  .file-status-badge.processing {
    color: #d97706;
  }

  .file-status-badge.pending {
    color: #9ca3af;
  }

  .file-status-badge.error {
    color: #dc2626;
  }

  .file-result {
    font-size: 12px;
    color: #16a34a;
    margin-top: 4px;
    font-weight: 500;
  }

  .progress-bar {
    height: 3px;
    background: #e5e7eb;
    border-radius: 2px;
    margin-top: 8px;
    overflow: hidden;
  }

  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--primary-color, #122E8A), #3b82f6);
    border-radius: 2px;
    transition: width 0.3s;
  }

  .error-msg {
    font-size: 12px;
    color: #dc2626;
    margin-top: 4px;
  }

  .btn-remove {
    width: 30px;
    height: 30px;
    border-radius: 8px;
    background: transparent;
    border: none;
    color: #d1d5db;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    transition: all 0.2s;
    flex-shrink: 0;
  }

  .btn-remove:hover {
    background: #fee2e2;
    color: #ef4444;
  }

  .action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: white;
    border-radius: 16px;
    padding: 16px 24px;
    margin-top: 20px;
  }

  .info-stats {
    display: flex;
    gap: 16px;
  }

  .stat {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 13px;
    color: #6b7280;
  }

  .stat-success {
    color: #16a34a;
  }

  .stat-pending {
    color: #d97706;
  }

  .btn-import {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 11px 28px;
    background: linear-gradient(135deg, var(--primary-color, #122E8A), #1e40af);
    border: none;
    border-radius: 10px;
    color: white;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 2px 8px rgba(18, 46, 138, 0.2);
  }

  .btn-import:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(18, 46, 138, 0.35);
  }

  .btn-import:disabled {
    opacity: 0.5;
    cursor: not-allowed;
    box-shadow: none;
  }

  .side-panel {
    width: 260px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .panel-card {
    background: white;
    border-radius: 16px;
    padding: 20px;
  }

  .panel-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 12px;
  }

  .panel-title .iconify {
    font-size: 18px;
    color: var(--primary-color, #122E8A);
  }

  .panel-desc {
    font-size: 13px;
    color: #6b7280;
    margin-bottom: 14px;
    line-height: 1.5;
  }

  .feature-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .feature-list li {
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }

  .feature-list li .iconify {
    font-size: 16px;
    color: var(--primary-color, #122E8A);
    margin-top: 2px;
    flex-shrink: 0;
  }

  .feature-list li strong {
    font-size: 13px;
    color: #374151;
    display: block;
    line-height: 1.3;
  }

  .feature-list li p {
    font-size: 12px;
    color: #9ca3af;
    margin: 2px 0 0;
    line-height: 1.4;
  }

  .toggle-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    cursor: pointer;
  }

  .toggle-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .toggle-label {
    font-size: 14px;
    color: #374151;
    font-weight: 500;
  }

  .toggle-desc {
    font-size: 12px;
    color: #9ca3af;
  }

  .toggle-switch {
    width: 42px;
    height: 24px;
    background: #d1d5db;
    border-radius: 12px;
    position: relative;
    transition: background 0.25s;
    flex-shrink: 0;
    cursor: pointer;
  }

  .toggle-switch.active {
    background: var(--primary-color, #122E8A);
  }

  .toggle-knob {
    width: 18px;
    height: 18px;
    background: white;
    border-radius: 50%;
    position: absolute;
    top: 3px;
    left: 3px;
    transition: transform 0.25s;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  }

  .toggle-switch.active .toggle-knob {
    transform: translateX(18px);
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

  @media (max-width: 768px) {
    .import-layout {
      flex-direction: column;
    }

    .side-panel {
      width: 100%;
    }

    .upload-zone {
      padding: 32px 20px;
    }
  }
</style>
