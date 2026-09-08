<script setup>
  import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { ElMessageBox } from 'element-plus'
  import { useKnowledgeStore } from '@/stores/knowledge'
  import { useToastStore } from '@/stores/toast'

  const route = useRoute()
  const router = useRouter()
  const knowledgeStore = useKnowledgeStore()
  const toast = useToastStore()

  const knowledgeId = computed(() => route.params.id)

  const knowledge = ref(null)
  const knowledgeItems = ref([])
  const loading = ref(true)
  const selectedItem = ref(null)
  const searchQuery = ref('')
  const viewMode = ref('list')

  const filteredItems = computed(() => {
    let result = knowledgeItems.value
    if (searchQuery.value.trim()) {
      const query = searchQuery.value.toLowerCase()
      result = result.filter(
        (item) =>
          item.title?.toLowerCase().includes(query) ||
          item.content?.toLowerCase().includes(query),
      )
    }
    return result
  })

  const goBack = () => {
    router.back()
  }

  const selectItem = (item) => {
    selectedItem.value = item
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN')
  }

  const getKnowledgeCategoryStyle = (category) => {
    const styles = {
      '技术': { icon: 'ri:code-s-slash-line', color: '#22C55E', bg: '#F0FDF4' },
      '学习': { icon: 'ri:book-open-line', color: '#4F7CFF', bg: '#EEF2FF' },
      '工作': { icon: 'ri:briefcase-line', color: '#6366F1', bg: '#EEF2FF' },
      '生活': { icon: 'ri:heart-line', color: '#EC4899', bg: '#FDF2F8' },
      '健康': { icon: 'ri:heart-pulse-line', color: '#EF4444', bg: '#FEF2F2' },
      '财经': { icon: 'ri:money-cny-circle-line', color: '#F59E0B', bg: '#FFFBEB' },
      '文化': { icon: 'ri:quill-pen-line', color: '#8B5CF6', bg: '#F5F3FF' },
      '其他': { icon: 'ri:folder-line', color: '#94A3B8', bg: '#F8FAFC' },
    }
    return styles[category] || styles['其他']
  }

  const loadKnowledgeDetail = async () => {
    loading.value = true
    try {
      const data = await knowledgeStore.fetchKnowledge(knowledgeId.value)
      if (data) {
        knowledge.value = data
        knowledgeItems.value = data.items || []
      }
    } finally {
      loading.value = false
    }
  }

  const deleteKnowledge = async () => {
    if (!knowledge.value) return
    try {
      await ElMessageBox.confirm(`确定要删除知识库"${knowledge.value.title}"吗？删除后不可恢复。`, '确认删除', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      })
      await knowledgeStore.deleteKnowledge(knowledgeId.value)
      toast.success('知识库已删除')
      router.push('/knowledge')
    } catch (e) {
      if (e !== 'cancel') {
        toast.error('删除失败: ' + (e.message || '未知错误'))
      }
    }
  }

  const deleteItem = async (item, event) => {
    if (event) event.stopPropagation()
    try {
      await ElMessageBox.confirm(`确定要删除知识点"${item.title}"吗？`, '确认删除', {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      })
      await knowledgeStore.deleteItem(item.id)
      toast.success('知识点已删除')
      selectedItem.value = null
      loadKnowledgeDetail()
    } catch (e) {
      if (e !== 'cancel') {
        toast.error('删除失败: ' + (e.message || '未知错误'))
      }
    }
  }

  watch(knowledgeId, () => {
    if (knowledgeId.value) {
      loadKnowledgeDetail()
    }
  })

  onMounted(() => {
    if (knowledgeId.value) {
      loadKnowledgeDetail()
    }
  })
</script>

<template>
  <div class="knowledge-detail-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <div class="header-actions">
        <div class="search-box">
          <Icon icon="ri:search-line" class="search-icon" />
          <input v-model="searchQuery" type="text" placeholder="搜索知识条目..." />
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <Icon icon="ri:loader-4-line" class="loading-icon" />
      <p>加载中...</p>
    </div>

    <div v-else-if="!knowledge" class="empty-state">
      <Icon icon="ri:database-2-line" class="empty-icon" />
      <p>知识库不存在</p>
    </div>

    <div v-else class="content-layout">
      <div class="sidebar-info" :style="{
          '--category-color': getKnowledgeCategoryStyle(knowledge.category).color,
          '--category-bg': getKnowledgeCategoryStyle(knowledge.category).bg
        }">
        <div class="knowledge-icon" :style="{
            background: getKnowledgeCategoryStyle(knowledge.category).bg,
            borderColor: getKnowledgeCategoryStyle(knowledge.category).color
          }">
          <Icon :icon="getKnowledgeCategoryStyle(knowledge.category).icon"
            :style="{ color: getKnowledgeCategoryStyle(knowledge.category).color }" />
        </div>
        <h2 class="knowledge-title">{{ knowledge.title }}</h2>
        <span class="knowledge-category"
          :style="{ background: getKnowledgeCategoryStyle(knowledge.category).bg, color: getKnowledgeCategoryStyle(knowledge.category).color }">
          {{ knowledge.category || '未分类' }}
        </span>

        <div class="stats-grid">
          <div class="stat-item">
            <span class="stat-value">{{ knowledge.itemCount || 0 }}</span>
            <span class="stat-label">知识点</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ knowledge.noteCount || 0 }}</span>
            <span class="stat-label">来源笔记</span>
          </div>
        </div>

        <p class="description">{{ knowledge.description || '暂无描述' }}</p>

        <div class="meta-info">
          <div class="meta-item">
            <Icon icon="ri:calendar-line" />
            <span>{{ formatDate(knowledge.createTime) }} 创建</span>
          </div>
        </div>

        <button class="delete-btn" @click="deleteKnowledge">
          <Icon icon="ri:delete-bin-line" />
          删除知识库
        </button>
      </div>

      <div class="main-content">
        <div class="content-header">
          <h3 class="section-title">
            <Icon icon="ri:list-check" />
            知识条目 ({{ filteredItems.length }})
          </h3>
          <div class="view-toggle">
            <button class="toggle-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
              <Icon icon="ri:list-unordered" />
            </button>
            <button class="toggle-btn" :class="{ active: viewMode === 'grid' }" @click="viewMode = 'grid'">
              <Icon icon="ri:grid-fill" />
            </button>
          </div>
        </div>

        <div v-if="filteredItems.length > 0" class="items-container" :class="{ 'grid-view': viewMode === 'grid' }">
          <div v-for="(item, index) in filteredItems" :key="item.id" class="item-card"
            :class="{ active: selectedItem?.id === item.id }" :style="{
              '--index': index,
              '--category-color': getKnowledgeCategoryStyle(knowledge.category).color,
              '--category-bg': getKnowledgeCategoryStyle(knowledge.category).bg,
              '--category-shadow': getKnowledgeCategoryStyle(knowledge.category).color + '20'
            }" @click="selectItem(item)">
            <div class="item-number">{{ index + 1 }}</div>
            <button class="item-delete-btn" @click="deleteItem(item, $event)" title="删除">
              <Icon icon="ri:close-line" />
            </button>
            <div class="item-body">
              <h4 class="item-title">{{ item.title }}</h4>
              <p class="item-content">{{ item.content }}</p>
            </div>
            <div class="item-footer">
              <span class="item-date">
                <Icon icon="ri:time-line" />
                {{ formatDate(item.createTime) }}
              </span>
            </div>
          </div>
        </div>

        <div v-else class="empty-items">
          <Icon icon="ri:file-list-3-line" class="empty-items-icon" />
          <p>暂无知识条目</p>
        </div>
      </div>

      <div class="detail-panel" v-if="selectedItem" :style="{
          '--category-color': getKnowledgeCategoryStyle(knowledge.category).color,
          '--category-bg': getKnowledgeCategoryStyle(knowledge.category).bg
        }">
        <div class="panel-scroll">
          <div class="panel-header-badge">
            <Icon icon="ri:lightbulb-flash-line" />
            <span>知识点详情</span>
          </div>

          <h2 class="panel-title">{{ selectedItem.title }}</h2>

          <div class="panel-meta">
            <span class="meta-tag">
              <Icon icon="ri:time-line" />
              {{ formatDate(selectedItem.createTime) }}
            </span>
          </div>

          <div class="panel-divider"></div>

          <div class="panel-content-wrapper">
            <h4 class="content-label">
              <Icon icon="ri:file-text-line" />
              详细内容
            </h4>
            <div class="panel-content">
              <p>{{ selectedItem.content }}</p>
            </div>
          </div>

          <div class="panel-source"
            v-if="selectedItem.source && selectedItem.source !== '笔记内容的第二段' && selectedItem.source !== '笔记内容的第一句话'">
            <h4 class="content-label">
              <Icon icon="ri:link" />
              来源
            </h4>
            <p class="source-text">{{ selectedItem.source }}</p>
          </div>
        </div>
      </div>

      <div class="empty-panel" v-else>
        <Icon icon="ri:cursor-pointer-line" class="empty-panel-icon" />
        <p>点击左侧条目<br />查看详细内容</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .knowledge-detail-page {
    height: calc(100vh - 108px);
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .back-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 18px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    color: #374151;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s ease;
  }

  .back-btn:hover {
    background: #f9fafb;
    border-color: #122E8A;
    color: #122E8A;
    transform: translateX(-2px);
  }

  .header-actions {
    display: flex;
    gap: 16px;
  }

  .search-box {
    display: flex;
    align-items: center;
    background: white;
    border-radius: 10px;
    padding: 0 16px;
    border: 1px solid #e5e7eb;
    transition: all 0.2s;
  }

  .search-box:focus-within {
    border-color: #122E8A;
    box-shadow: 0 0 0 3px rgba(18, 46, 138, 0.08);
  }

  .search-icon {
    color: #9ca3af;
    margin-right: 8px;
  }

  .search-box input {
    border: none;
    outline: none;
    padding: 11px 0;
    font-size: 14px;
    width: 220px;
  }

  .loading-state,
  .empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: white;
    border-radius: 16px;
  }

  .loading-icon {
    font-size: 42px;
    color: #122E8A;
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

  .empty-icon {
    font-size: 56px;
    color: #d1d5db;
    margin-bottom: 16px;
  }

  .content-layout {
    flex: 1;
    display: grid;
    grid-template-columns: 260px 1fr 380px;
    gap: 20px;
    overflow: hidden;
  }

  .sidebar-info {
    background: linear-gradient(180deg, #ffffff 0%, #f8faff 100%);
    border-radius: 16px;
    padding: 28px;
    height: fit-content;
    border: 1px solid #e8ecf4;
  }

  .knowledge-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 20px;
    border: 2px solid;
    transition: all 0.2s ease;
  }

  .knowledge-icon:hover {
    transform: scale(1.05);
  }

  .knowledge-title {
    font-size: 19px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 6px;
    line-height: 1.35;
  }

  .knowledge-category {
    display: inline-block;
    font-size: 12px;
    font-weight: 500;
    color: var(--category-color, #122E8A);
    background: var(--category-bg, linear-gradient(135deg, #ede9fe 0%, #ddd6fe 100%));
    padding: 5px 14px;
    border-radius: 20px;
    margin-bottom: 24px;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 24px;
  }

  .stat-item {
    background: white;
    border-radius: 12px;
    padding: 16px 12px;
    text-align: center;
    border: 1px solid #f0f0f0;
  }

  .stat-value {
    display: block;
    font-size: 26px;
    font-weight: 700;
    color: var(--category-color, #122E8A);
    line-height: 1.2;
  }

  .stat-label {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 4px;
  }

  .description {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.65;
    margin-bottom: 20px;
    padding: 14px;
    background: white;
    border-radius: 10px;
    border: 1px solid #f0f0f0;
  }

  .meta-info {
    border-top: 1px solid #eef0f5;
    padding-top: 16px;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #6b7280;
  }

  .delete-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    width: 100%;
    padding: 10px;
    margin-top: 16px;
    background: #fef2f2;
    border: 1px solid #fecaca;
    border-radius: 10px;
    color: #dc2626;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
  }

  .delete-btn:hover {
    background: #fee2e2;
    border-color: #fca5a5;
  }

  .main-content {
    background: white;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border: 1px solid #e5e7eb;
  }

  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18px 22px;
    border-bottom: 1px solid #f3f4f6;
    background: #fafbfc;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #374151;
    margin: 0;
  }

  .section-title svg,
  .section-title .iconify {
    color: var(--category-color, #122E8A);
  }

  .view-toggle {
    display: flex;
    background: #f3f4f6;
    border-radius: 8px;
    padding: 4px;
  }

  .toggle-btn {
    width: 34px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: none;
    color: #9ca3af;
    cursor: pointer;
    border-radius: 6px;
    transition: all 0.2s;
  }

  .toggle-btn.active {
    background: white;
    color: var(--category-color, #122E8A);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  }

  .items-container {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  .items-container.grid-view {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }

  .item-card {
    background: white;
    border-radius: 12px;
    padding: 0;
    cursor: pointer;
    transition: all 0.25s ease;
    border: 2px solid transparent;
    display: flex;
    flex-direction: column;
    position: relative;
    animation: slideIn 0.35s ease forwards;
    animation-delay: calc(var(--index) * 0.04s);
    opacity: 0;
    transform: translateY(8px);
  }

  @keyframes slideIn {
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .item-card:hover {
    background: var(--category-bg, #fafbff);
    border-color: var(--category-color, #c7d7fe);
    transform: translateX(4px);
  }

  .item-card.active {
    background: linear-gradient(135deg, #ffffff 0%, var(--category-bg, #f0f4ff) 100%);
    border-color: var(--category-color, #122E8A);
    box-shadow: 0 4px 16px var(--category-shadow, rgba(18, 46, 138, 0.12));
  }

  .item-number {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg,
        color-mix(in oklch, var(--category-color, #122E8A) 85%, black) 0%,
        var(--category-color, #2563eb) 100%);
    color: white;
    font-size: 13px;
    font-weight: 600;
    border-radius: 12px 0 0 0;
    flex-shrink: 0;
    margin: 10px 0 0 14px;
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .item-delete-btn {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.9);
    border: none;
    border-radius: 6px;
    color: #9ca3af;
    cursor: pointer;
    opacity: 0;
    transition: all 0.2s;
    z-index: 1;
  }

  .item-card:hover .item-delete-btn {
    opacity: 1;
  }

  .item-delete-btn:hover {
    background: #fef2f2;
    color: #dc2626;
  }

  .item-card.active .item-number {
    background: linear-gradient(135deg,
        color-mix(in oklch, var(--category-color, #0f2558) 70%, black) 0%,
        color-mix(in oklch, var(--category-color, #1a459e) 85%, black) 100%);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.15), 0 2px 6px rgba(0, 0, 0, 0.15);
  }

  .item-body {
    flex: 1;
    padding: 14px 16px 10px;
  }

  .item-title {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .item-content {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .item-footer {
    padding: 10px 16px;
    border-top: 1px solid #f8f9fb;
    display: flex;
    justify-content: flex-end;
  }

  .item-date {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: #b0b8c8;
  }

  .empty-items {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #9ca3af;
  }

  .empty-items-icon {
    font-size: 48px;
    margin-bottom: 12px;
    opacity: 0.4;
  }

  .detail-panel {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
  }

  .panel-scroll {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
  }

  .panel-header-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: linear-gradient(135deg,
        color-mix(in oklch, var(--category-color, #122E8A) 85%, black) 0%,
        var(--category-color, #2563eb) 100%);
    color: white;
    font-size: 13px;
    font-weight: 600;
    border-radius: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px color-mix(in oklch, var(--category-color, #122E8A) 50%, transparent);
  }

  .panel-title {
    font-size: 22px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 16px;
    line-height: 1.35;
  }

  .panel-meta {
    display: flex;
    gap: 12px;
    margin-bottom: 4px;
  }

  .meta-tag {
    display: inline-flex;
    align-items: center;
    gap: 5px;
    font-size: 12px;
    color: #9ca3af;
    background: #f9fafb;
    padding: 5px 12px;
    border-radius: 6px;
  }

  .panel-divider {
    height: 1px;
    background: linear-gradient(90deg, transparent, #e5e7eb, transparent);
    margin: 20px 0;
  }

  .panel-content-wrapper,
  .panel-source {
    margin-bottom: 20px;
  }

  .content-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #6b7280;
    margin-bottom: 10px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .content-label svg,
  .content-label .iconify {
    color: var(--category-color, #122E8A);
  }

  .panel-content {
    font-size: 14px;
    color: #374151;
    line-height: 1.85;
    padding: 18px;
    background: linear-gradient(135deg, var(--category-bg, #f8faff) 0%, #ffffff 100%);
    border-radius: 12px;
    border: 1px solid var(--category-bg, #eef2fa);
    white-space: pre-wrap;
    word-wrap: break-word;
    word-break: break-word;
  }

  .source-text {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.6;
    padding: 12px 14px;
    background: #fefefe;
    border-left: 3px solid var(--category-color, #122E8A);
    border-radius: 0 8px 8px 0;
    font-style: italic;
  }

  .empty-panel {
    background: linear-gradient(180deg, #fafbfd 0%, #ffffff 100%);
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #c0c8d8;
    border: 2px dashed #e2e8f0;
  }

  .empty-panel-icon {
    font-size: 52px;
    margin-bottom: 16px;
    opacity: 0.5;
  }

  .empty-panel p {
    text-align: center;
    line-height: 1.6;
    font-size: 14px;
  }
</style>