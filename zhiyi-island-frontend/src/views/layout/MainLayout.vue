<script setup>
  import { ref, computed, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { useAuthStore } from '@/stores/auth'
  import { useNotificationStore } from '@/stores/notification'

  const route = useRoute()
  const router = useRouter()
  const authStore = useAuthStore()
  const notificationStore = useNotificationStore()

  const pageConfigs = {
    '/home': '',
    '/notes': '记录想法，沉淀知识',
    '/knowledge': '构建你的个人知识体系',
    '/search': '用 AI 快速找到你需要的信息',
    '/profile': '管理你的账户和偏好设置',
    '/notification': '通知中心',
    '/drafts': '草稿箱',
  }

  const pageSubtitle = computed(() => {
    return pageConfigs[route.path] || ''
  })

  const menuItems = [
    { path: '/home', title: '首页', icon: 'ri:home-4-line' },
    { path: '/notes', title: '笔记', icon: 'ri:book-3-line' },
    { path: '/knowledge', title: '知识库', icon: 'ri:brain-line' },
    { path: '/search', title: '记忆搜索', icon: 'ri:search-line' },
    { path: '/profile', title: '个人中心', icon: 'ri:user-3-line' },
  ]

  const isCollapse = ref(false)

  const navigateTo = (path) => {
    router.push(path)
  }

  const isActive = (path) => {
    return route.path === path
  }

  const handleLogout = async () => {
    await authStore.logout()
    router.push('/login')
  }

  const goToNotification = () => {
    router.push('/notification')
  }

  const goToDrafts = () => {
    router.push('/drafts')
  }

  onMounted(() => {
    notificationStore.fetchUnreadCount()
  })
</script>

<template>
  <div class="app-container">
    <aside class="sidebar" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <Icon icon="ri:ai-generate" class="logo-icon" />
        <span class="logo-text">知忆岛</span>
      </div>

      <nav class="menu" @click="isCollapse = !isCollapse">
        <div v-for="item in menuItems" :key="item.path" class="menu-item" :class="{ active: isActive(item.path) }"
          @click.stop="navigateTo(item.path)">
          <Icon :icon="item.icon" class="menu-icon" />
          <span class="menu-title">{{ item.title }}</span>
        </div>
      </nav>

      <div class="logout-section" @click.stop="handleLogout">
        <Icon icon="ri:logout-box-r-line" class="logout-icon" />
        <span class="logout-text">退出登录</span>
      </div>
    </aside>

    <main class="main-content">
      <header class="header">
        <p class="page-subtitle">{{ pageSubtitle }}</p>
        <div class="header-actions">
          <div class="action-btn" @click="goToDrafts">
            <Icon icon="ri:draft-line" class="header-icon" />
          </div>
          <div class="action-btn" @click="goToNotification">
            <Icon icon="ri:notification-3-line" class="header-icon" />
            <span v-if="notificationStore.unreadCount > 0" class="action-badge">{{ notificationStore.unreadCount
              }}</span>
          </div>
        </div>
      </header>

      <div class="content">
        <router-view v-slot="{ Component }">
          <keep-alive :include="['search']">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </div>
    </main>
  </div>
</template>

<style scoped>
  .app-container {
    display: flex;
    height: 100vh;
    background-color: var(--bg-color, #f0f2f5);
  }

  .sidebar {
    width: 160px;
    background: linear-gradient(180deg,
        var(--primary-color, #122E8A) 0%,
        var(--secondary-color, #122E8A) 100%);
    display: flex;
    flex-direction: column;
    transition: width 0.3s ease;
    position: relative;
  }

  .sidebar.collapsed {
    width: 64px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px 16px;
    color: white;
    font-size: 20px;
    font-weight: 600;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  .logo-icon {
    font-size: 28px;
    flex-shrink: 0;
  }

  .menu {
    flex: 1;
    padding: 16px 8px;
    overflow-y: auto;
    cursor: pointer;
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    color: rgba(255, 255, 255, 0.8);
    border-radius: 12px;
    cursor: pointer;
    margin-bottom: 4px;
    transition: all 0.2s ease;
  }

  .menu-item:hover {
    background: rgba(255, 255, 255, 0.15);
    color: white;
  }

  .menu-item.active {
    background: rgba(255, 255, 255, 0.25);
    color: white;
    font-weight: 500;
  }

  .menu-icon {
    font-size: 20px;
    flex-shrink: 0;
  }

  .menu-title {
    white-space: nowrap;
    overflow: hidden;
    opacity: 1;
    transition: opacity 0.2s ease;
  }

  .sidebar.collapsed .menu-title {
    opacity: 0;
    width: 0;
  }

  .logo-text {
    white-space: nowrap;
    overflow: hidden;
    opacity: 1;
    transition: opacity 0.2s ease;
  }

  .sidebar.collapsed .logo-text {
    opacity: 0;
    width: 0;
  }

  .logout-section {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 16px;
    margin: 8px;
    color: rgba(255, 255, 255, 0.7);
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s ease;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    padding-top: 16px;
  }

  .logout-section:hover {
    background: rgba(255, 255, 255, 0.15);
    color: white;
  }

  .logout-icon {
    font-size: 20px;
    flex-shrink: 0;
  }

  .logout-text {
    white-space: nowrap;
    overflow: hidden;
    opacity: 1;
    transition: opacity 0.2s ease;
  }

  .sidebar.collapsed .logout-text {
    opacity: 0;
    width: 0;
  }

  .main-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  .header {
    height: 56px;
    background: var(--card-bg, white);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    flex-shrink: 0;
    border-bottom: 1px solid #e5e7eb;
  }

  .page-subtitle {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color, #1f2937);
    margin: 0;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .action-btn {
    position: relative;
    cursor: pointer;
    padding: 8px;
    border-radius: 8px;
    transition: background-color 0.2s;
  }

  .action-btn:hover {
    background: #f3f4f6;
  }

  .header-icon {
    font-size: 22px;
    color: var(--text-secondary, #666);
  }

  .action-badge {
    position: absolute;
    top: 2px;
    right: 2px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background: #ef4444;
    color: white;
    font-size: 10px;
    font-weight: 600;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .content {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
    overflow-x: hidden;
    scrollbar-gutter: stable;
  }
</style>