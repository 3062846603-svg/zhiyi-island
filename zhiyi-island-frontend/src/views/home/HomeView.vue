<!--
  首页视图组件
  展示用户统计数据、快捷操作入口和最近笔记
-->
<script setup>
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { useAuthStore } from '@/stores/auth'
  import { useNoteStore } from '@/stores/note'
  import { useKnowledgeStore } from '@/stores/knowledge'
  import { knowledgeApi } from '@/api/knowledge'

  const router = useRouter()
  const authStore = useAuthStore()
  const noteStore = useNoteStore()
  const knowledgeStore = useKnowledgeStore()

  const userName = computed(
    () => authStore.userInfo?.nickname || authStore.userInfo?.username || '用户',
  )

  const stats = ref([
    { label: '笔记总数', value: 0, icon: 'ri:book-3-line', color: '#122E8A' },
    { label: '知识条目', value: 0, icon: 'ri:brain-line', color: '#7c3aed' },
    { label: '今日笔记', value: 0, icon: 'ri:edit-line', color: '#10b981' },
  ])

  const recentNotes = ref([])

  const quickActions = [
    { label: '添加笔记', icon: 'ri:add-circle-line', color: '#122E8A', route: '/home/add-note' },
    { label: '导入文档', icon: 'ri:file-upload-line', color: '#7c3aed', route: '/home/import' },
    {
      label: '知识图谱',
      icon: 'ri:bubble-chart-line',
      color: '#10b981',
      route: '/home/knowledge-graph',
    },
    {
      label: '导出数据',
      icon: 'ri:file-download-line',
      color: '#f59e0b',
      route: '/profile/export',
    },
  ]

  /** 格式化数字显示 */
  const formatNumber = (num) => {
    if (num >= 1000) {
      return (num / 1000).toFixed(1) + 'k'
    }
    return num.toString()
  }

  /** 导航到指定路由 */
  const goToRoute = (route) => {
    router.push(route)
  }

  /** 跳转到笔记详情 */
  const goToNoteDetail = (id) => {
    router.push(`/notes/${id}`)
  }

  /** 跳转到笔记列表 */
  const goToNotes = () => {
    router.push('/notes')
  }

  /** 加载统计数据 */
  const loadStats = async () => {
    try {
      const noteCount = await noteStore.getNoteCount()
      stats.value[0].value = noteCount || 0
    } catch (e) {
      console.error('获取笔记数量失败:', e)
    }

    try {
      const knowledgeRes = await knowledgeApi.itemCount()
      if (knowledgeRes.code === 200) {
        stats.value[1].value = knowledgeRes.data || 0
      }
    } catch (e) {
      console.error('获取知识条目数量失败:', e)
    }

    try {
      await noteStore.fetchNotes()
      const today = new Date().toISOString().substring(0, 10)
      const todayCount = noteStore.notes.filter(
        (note) => note.createTime?.substring(0, 10) === today,
      ).length
      stats.value[2].value = todayCount
      loadRecentNotes()
    } catch (e) {
      console.error('获取今日笔记数量失败:', e)
    }
  }

  /** 加载最近笔记 */
  const loadRecentNotes = () => {
    recentNotes.value = noteStore.notes.slice(0, 3).map((note) => ({
      id: note.id,
      title: note.title,
      date: note.createTime?.substring(0, 10) || '',
    }))
  }

  onMounted(() => {
    loadStats()
  })
</script>

<template>
  <div class="home-page">
    <div class="welcome-section">
      <h1 class="welcome-title">欢迎回来，{{ userName }}</h1>
      <p class="welcome-subtitle">今天想要学习什么呢？</p>
    </div>

    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="stat-card">
        <div class="stat-icon" :style="{ background: stat.color }">
          <Icon :icon="stat.icon" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ formatNumber(stat.value) }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="section recent-notes">
        <div class="section-header">
          <h3>最近笔记</h3>
          <span class="more" @click="goToNotes">查看全部 →</span>
        </div>
        <div class="notes-list">
          <div v-for="note in recentNotes" :key="note.id" class="note-item" @click="goToNoteDetail(note.id)">
            <div class="note-title">{{ note.title }}</div>
            <div class="note-meta">
              <span class="note-date">{{ note.date }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="section quick-actions">
        <div class="section-header">
          <h3>快捷操作</h3>
        </div>
        <div class="actions-grid">
          <div v-for="action in quickActions" :key="action.label" class="action-item" @click="goToRoute(action.route)">
            <div class="action-icon" :style="{ background: action.color }">
              <Icon :icon="action.icon" />
            </div>
            <span class="action-label">{{ action.label }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .home-page {
    width: 100%;
  }

  .welcome-section {
    margin-bottom: 32px;
  }

  .welcome-title {
    font-size: 28px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 8px;
  }

  .welcome-subtitle {
    font-size: 16px;
    color: #6b7280;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 32px;
  }

  .stat-card {
    background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
    border-radius: 16px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.02),
      0 4px 8px rgba(0, 0, 0, 0.04),
      0 8px 16px rgba(0, 0, 0, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
  }

  .stat-card:hover {
    transform: translateY(-4px);
    box-shadow:
      0 4px 8px rgba(0, 0, 0, 0.04),
      0 8px 16px rgba(0, 0, 0, 0.06),
      0 16px 32px rgba(0, 0, 0, 0.06),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
  }

  .stat-card:hover .stat-icon {
    transform: scale(1.1);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    transition: transform 0.3s ease;
    box-shadow:
      0 4px 12px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }

  .stat-value {
    font-size: var(--font-size-stat-value);
    font-weight: 600;
    color: #1f2937;
  }

  .stat-label {
    font-size: var(--font-size-body);
    color: #6b7280;
  }

  .content-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
    align-items: stretch;
  }

  .section {
    background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
    border-radius: 16px;
    padding: 24px;
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.02),
      0 4px 8px rgba(0, 0, 0, 0.04),
      0 8px 16px rgba(0, 0, 0, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;
    transition: all 0.3s ease;
  }

  .section:hover {
    box-shadow:
      0 4px 8px rgba(0, 0, 0, 0.04),
      0 8px 16px rgba(0, 0, 0, 0.06),
      0 16px 32px rgba(0, 0, 0, 0.06),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
  }

  .section .recent-notes {
    height: 345px;
  }

  .section .quick-actions {
    height: 345px;
  }

  .recent-notes .notes-list {
    flex: 1;
    overflow-y: auto;
  }

  .quick-actions .actions-grid {
    flex: 1;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .section-header h3 {
    font-size: var(--font-size-section-title);
    font-weight: 600;
    color: #1f2937;
  }

  .more {
    font-size: var(--font-size-body);
    color: #122E8A;
    cursor: pointer;
  }

  .notes-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .note-item {
    padding: 16px;
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.03);
  }

  .note-item:hover {
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
    box-shadow:
      inset 0 1px 2px rgba(0, 0, 0, 0.06),
      0 2px 8px rgba(0, 0, 0, 0.04);
  }

  .note-title {
    font-size: var(--font-size-card-title);
    font-weight: 500;
    color: #1f2937;
    margin-bottom: 8px;
  }

  .note-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .note-date {
    font-size: var(--font-size-small);
    color: #9ca3af;
  }

  .actions-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 20px;
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.03);
  }

  .action-item:hover {
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
    transform: translateY(-2px);
    box-shadow:
      inset 0 1px 2px rgba(0, 0, 0, 0.06),
      0 4px 12px rgba(0, 0, 0, 0.06);
  }

  .action-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 22px;
    box-shadow:
      0 4px 12px rgba(0, 0, 0, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }

  .action-label {
    font-size: var(--font-size-body);
    font-weight: 500;
    color: #374151;
  }

  @media (max-width: 1024px) {
    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
    }

    .content-grid {
      grid-template-columns: 1fr;
    }
  }
</style>