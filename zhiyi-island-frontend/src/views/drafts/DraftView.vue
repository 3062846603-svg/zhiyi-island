<script setup>
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { ElMessageBox } from 'element-plus'
  import { noteApi } from '@/api/note'

  const router = useRouter()
  const drafts = ref([])
  const loading = ref(true)

  const loadDrafts = async () => {
    loading.value = true
    try {
      const result = await noteApi.getDrafts()
      if (result.code === 200) {
        drafts.value = result.data || []
      }
    } catch (e) {
      console.error('获取草稿失败:', e)
    } finally {
      loading.value = false
    }
  }

  const editDraft = (id) => {
    router.push(`/home/add-note?id=${id}`)
  }

  const publishDraft = async (id) => {
    try {
      const result = await noteApi.publishDraft(id)
      if (result.code === 200) {
        loadDrafts()
      }
    } catch (e) {
      console.error('发布草稿失败:', e)
    }
  }

  const deleteDraft = async (id) => {
    try {
      await ElMessageBox.confirm('确定要删除这篇草稿吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
    } catch {
      return
    }

    try {
      const result = await noteApi.delete(id)
      if (result.code === 200) {
        loadDrafts()
      }
    } catch (e) {
      console.error('删除草稿失败:', e)
    }
  }

  const formatDate = (dateStr) => {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))

    if (days === 0) {
      return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (days === 1) {
      return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    } else if (days < 7) {
      return `${days}天前`
    } else {
      return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    }
  }

  onMounted(() => {
    loadDrafts()
  })
</script>

<template>
  <div class="drafts-page">
    <div v-if="loading" class="loading-state">
      <Icon icon="ri:loader-4-line" class="spin" />
      <span>加载中...</span>
    </div>

    <div v-else-if="drafts.length === 0" class="empty-state">
      <div class="empty-icon">
        <Icon icon="ri:draft-line" />
      </div>
      <h3>暂无草稿</h3>
      <p>你还没有保存任何草稿</p>
      <button class="btn btn-primary" @click="router.push('/home/add-note')">
        <Icon icon="ri:add-line" />
        写笔记
      </button>
    </div>

    <div v-else class="drafts-grid">
      <div v-for="draft in drafts" :key="draft.id" class="draft-card">
        <div class="card-header">
          <span class="draft-badge">
            <Icon icon="ri:draft-line" />
            草稿
          </span>
          <span class="draft-time">{{ formatDate(draft.updateTime) }}</span>
        </div>

        <div class="card-body" @click="editDraft(draft.id)">
          <h3 class="draft-title">{{ draft.title || '无标题' }}</h3>
          <p class="draft-content">{{ (draft.summary || draft.content?.replace(/<[^>]+>/g, '').replace(/\n/g, ' ') ||
              '无内容').substring(0, 120) }}</p>
        </div>

        <div class="card-footer">
          <span v-if="draft.category" class="category-tag">
            <Icon icon="ri:folder-line" />
            {{ draft.category }}
          </span>
          <span v-else class="category-tag empty"></span>
          <div class="card-actions">
            <button class="btn btn-sm btn-primary" @click="publishDraft(draft.id)">
              <Icon icon="ri:send-plane-line" />
              发布
            </button>
            <button class="btn btn-sm btn-icon btn-danger" @click="deleteDraft(draft.id)">
              <Icon icon="ri:delete-bin-line" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .drafts-page {
    padding: 24px;
    min-height: calc(100vh - 120px);
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 80px;
    color: var(--text-secondary, #6b7280);
  }

  .spin {
    font-size: 32px;
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

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 24px;
    text-align: center;
  }

  .empty-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;
  }

  .empty-icon .iconify {
    font-size: 36px;
    color: var(--primary-color, #122E8A);
  }

  .empty-state h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin: 0 0 8px 0;
  }

  .empty-state p {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    margin: 0 0 24px 0;
  }

  .drafts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .draft-card {
    background: var(--card-bg, white);
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
    transition: all 0.2s;
    display: flex;
    flex-direction: column;
  }

  .draft-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #f3f4f6;
  }

  .draft-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 4px 10px;
    background: linear-gradient(135deg, #E8EDFA 0%, #D4DFF5 100%);
    color: var(--primary-color, #122E8A);
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
  }

  .draft-time {
    font-size: 12px;
    color: var(--text-secondary, #9ca3af);
  }

  .card-body {
    flex: 1;
    padding: 20px;
    cursor: pointer;
  }

  .draft-title {
    font-size: 17px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin: 0 0 10px 0;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .draft-content {
    font-size: 14px;
    color: var(--text-secondary, #6b7280);
    margin: 0;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-top: 1px solid #f3f4f6;
    background: #fafafa;
  }

  .category-tag {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: #f3f4f6;
    border-radius: 6px;
    font-size: 12px;
    color: var(--text-secondary, #6b7280);
  }

  .category-tag.empty {
    background: transparent;
  }

  .card-actions {
    display: flex;
    gap: 8px;
  }

  @media (max-width: 768px) {
    .drafts-page {
      padding: 16px;
    }

    .drafts-grid {
      grid-template-columns: 1fr;
    }
  }
</style>