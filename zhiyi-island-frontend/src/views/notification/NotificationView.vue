<script setup>
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { useNotificationStore } from '@/stores/notification'
  import { useToastStore } from '@/stores/toast'

  const router = useRouter()
  const notificationStore = useNotificationStore()
  const toast = useToastStore()

  const activeTab = ref('all')

  const tabs = [
    { key: 'all', label: '全部通知' },
    { key: '系统通知', label: '系统通知' },
    { key: '笔记通知', label: '笔记相关' },
    { key: 'AI通知', label: 'AI 通知' },
  ]

  const filteredNotifications = computed(() => {
    if (activeTab.value === 'all') {
      return notificationStore.notifications
    } else {
      return notificationStore.notifications.filter((n) => n.type === activeTab.value)
    }
  })

  const unreadCount = computed(() => {
    return notificationStore.unreadCount
  })

  const getTypeIcon = (type) => {
    const icons = {
      '系统通知': 'ri:settings-3-line',
      '笔记通知': 'ri:book-3-line',
      'AI通知': 'ri:robot-line',
      '知识库通知': 'ri:brain-line',
    }
    return icons[type] || 'ri:notification-3-line'
  }

  const getTypeColor = (type) => {
    const colors = {
      '系统通知': '#6366f1',
      '笔记通知': '#10b981',
      'AI通知': '#f59e0b',
      '知识库通知': '#3b82f6',
    }
    return colors[type] || '#6b7280'
  }

  const getTypeName = (type) => {
    const names = {
      '系统通知': '系统',
      '笔记通知': '笔记',
      'AI通知': 'AI',
      '知识库通知': '知识库',
    }
    return names[type] || '通知'
  }

  const formatTime = (timeStr) => {
    if (!timeStr) return ''
    const date = new Date(timeStr)
    const now = new Date()
    const diff = now - date

    if (diff < 60000) return '刚刚'
    if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
    if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
    if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
    return timeStr.substring(0, 10)
  }

  const loadNotifications = async () => {
    await notificationStore.fetchNotifications(100)
  }

  const markAsRead = async (id) => {
    try {
      await notificationStore.markAsRead(id)
    } catch (e) {
      console.error('标记已读失败:', e)
    }
  }

  const markAllAsRead = async () => {
    try {
      await notificationStore.markAllAsRead()
      toast.success('已全部标记为已读')
    } catch (e) {
      console.error('全部标记已读失败:', e)
      toast.error('操作失败')
    }
  }

  const deleteNotification = async (id) => {
    try {
      await notificationStore.deleteNotification(id)
      toast.success('已删除')
    } catch (e) {
      console.error('删除通知失败:', e)
      toast.error('删除失败')
    }
  }

  const goBack = () => {
    router.back()
  }

  const getFormattedTime = (item) => {
    return formatTime(item.createTime)
  }

  onMounted(() => {
    loadNotifications()
  })
</script>

<template>
  <div class="notification-page">
    <div class="page-header">
      <button class="btn btn-secondary" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <h2>通知中心</h2>
      <button v-if="unreadCount > 0" class="btn btn-primary" @click="markAllAsRead">
        <Icon icon="ri:check-double-line" />
        全部已读
      </button>
    </div>

    <div class="tabs-container">
      <div class="tabs">
        <button v-for="tab in tabs" :key="tab.key" class="btn btn-text" :class="{ 'active-tab': activeTab === tab.key }"
          @click="activeTab = tab.key">
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="content-area">
      <div v-if="notificationStore.loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="filteredNotifications.length === 0" class="empty-state">
        <Icon icon="ri:notification-off-line" class="empty-icon" />
        <p>暂无通知</p>
      </div>

      <div v-else class="notification-list">
        <div v-for="item in filteredNotifications" :key="item.id" class="notification-card"
          :class="{ unread: !item.read }">
          <div class="notification-icon" :style="{ background: getTypeColor(item.type) }">
            <Icon :icon="getTypeIcon(item.type)" />
          </div>
          <div class="notification-content">
            <div class="notification-header">
              <span class="notification-type" :style="{ color: getTypeColor(item.type) }">
                {{ getTypeName(item.type) }}
              </span>
              <div class="notification-actions">
                <button v-if="!item.read" class="btn btn-sm btn-icon btn-success" @click="markAsRead(item.id)">
                  <Icon icon="ri:check-line" />
                </button>
                <button class="btn btn-sm btn-icon btn-danger" @click="deleteNotification(item.id)">
                  <Icon icon="ri:delete-bin-line" />
                </button>
              </div>
            </div>
            <div class="notification-body">
              <div class="notification-main">
                <h3 class="notification-title">{{ item.title }}</h3>
                <p class="notification-text">{{ item.content }}</p>
              </div>
              <span class="notification-time">{{ getFormattedTime(item) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .notification-page {
    height: calc(100vh - 108px);
    display: flex;
    flex-direction: column;
  }

  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 20px;
    flex-shrink: 0;
  }

  .page-header h2 {
    flex: 1;
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
    text-align: center;
  }

  .tabs-container {
    background: white;
    border-radius: 12px;
    padding: 8px;
    margin-bottom: 20px;
    flex-shrink: 0;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
  }

  .tabs {
    display: flex;
    gap: 4px;
  }

  .active-tab {
    background: linear-gradient(145deg, var(--primary-color) 0%, var(--secondary-color) 100%) !important;
    color: white !important;
    box-shadow:
      0 4px 12px rgba(18, 46, 138, 0.25),
      0 8px 20px rgba(18, 46, 138, 0.12),
      inset 0 1px 0 rgba(255, 255, 255, 0.2) !important;
  }


  .content-area {
    flex: 1;
    overflow-y: auto;
  }

  .loading-state,
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    background: white;
    border-radius: 16px;
    border: 1px solid #e5e7eb;
  }

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f3f4f6;
    border-top-color: var(--primary-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .loading-state p,
  .empty-state p {
    color: #6b7280;
    font-size: 14px;
  }

  .empty-icon {
    font-size: 48px;
    color: #d1d5db;
    margin-bottom: 16px;
  }

  .notification-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .notification-card {
    background: white;
    border-radius: 12px;
    padding: 14px 16px;
    display: flex;
    align-items: center;
    gap: 14px;
    position: relative;
    transition: all 0.2s;
    border: 1px solid #e5e7eb;
  }

  .notification-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }

  .notification-card.unread {
    background: linear-gradient(135deg, #f0f7ff 0%, #e8f4ff 100%);
    border-color: var(--primary-color);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
  }

  .notification-icon {
    width: 38px;
    height: 38px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
    flex-shrink: 0;
  }

  .notification-content {
    flex: 1;
    min-width: 0;
  }

  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }

  .notification-type {
    font-size: 12px;
    font-weight: 500;
  }

  .notification-actions {
    display: flex;
    gap: 8px;
    opacity: 0;
    transition: opacity 0.2s;
  }

  .notification-card:hover .notification-actions {
    opacity: 1;
  }

  .notification-body {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }

  .notification-main {
    flex: 1;
    min-width: 0;
  }

  .notification-title {
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 4px 0;
  }

  .notification-text {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.5;
    margin: 0;
  }

  .notification-time {
    font-size: 12px;
    color: #9ca3af;
    white-space: nowrap;
    flex-shrink: 0;
    align-self: flex-start;
    padding-top: 23px;
  }

  @media (max-width: 768px) {
    .tabs {
      overflow-x: auto;
      padding-bottom: 4px;
    }

    .tab {
      white-space: nowrap;
    }

    .notification-actions {
      opacity: 1;
    }
  }
</style>
