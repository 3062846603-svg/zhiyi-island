<script setup>
  import { computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { useAuthStore } from '@/stores/auth'

  const router = useRouter()
  const authStore = useAuthStore()

  const userName = computed(
    () => authStore.userInfo?.nickname || authStore.userInfo?.username || '用户',
  )
  const userEmail = computed(() => authStore.userInfo?.email || '')
  const userAvatar = computed(() => authStore.userInfo?.avatar || null)
  const joinDate = computed(() => authStore.userInfo?.createTime?.substring(0, 10) || '')

  const settingsSections = [
    {
      title: '账户设置',
      items: [
        { label: '个人资料', icon: 'ri:user-settings-line', route: '/profile/settings' },
        { label: '修改密码', icon: 'ri:lock-password-line', route: '/profile/password' },
        { label: '换绑邮箱', icon: 'ri:mail-line', route: '/profile/email' },
      ],
    },
    {
      title: '数据管理',
      items: [
        { label: '导入数据', icon: 'ri:file-upload-line', route: '/home/import' },
        { label: '导出数据', icon: 'ri:file-download-line', route: '/profile/export' },
      ],
    },
    {
      title: '关于',
      items: [{ label: '关于我们', icon: 'ri:team-line', route: '/legal/about' }],
    },
  ]

  const goToSettings = (route) => {
    if (route) {
      router.push(route)
    }
  }

  const goToProfileSettings = () => {
    router.push('/profile/settings')
  }

  const handleLogout = async () => {
    await authStore.logout()
    router.push('/login')
  }

  onMounted(async () => {
    if (!authStore.userInfo) {
      await authStore.fetchUserInfo()
    }
  })
</script>

<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="user-card">
        <div class="avatar">
          <img v-if="userAvatar" :src="userAvatar" alt="avatar" />
          <Icon v-else icon="ri:user-3-line" class="avatar-icon" />
        </div>
        <div class="user-info">
          <h2>{{ userName }}</h2>
          <p class="email">{{ userEmail || '未设置邮箱' }}</p>
          <p class="join-date" v-if="joinDate">
            <Icon icon="ri:calendar-line" />
            加入于 {{ joinDate }}
          </p>
        </div>
        <button class="btn btn-secondary" @click="goToProfileSettings">
          <Icon icon="ri:edit-line" />
          编辑资料
        </button>
      </div>
    </div>

    <div class="settings-grid">
      <div v-for="section in settingsSections" :key="section.title" class="setting-group">
        <h3 class="group-title">{{ section.title }}</h3>
        <div class="setting-list">
          <div v-for="item in section.items" :key="item.label" class="setting-item" @click="goToSettings(item.route)">
            <div class="setting-left">
              <Icon :icon="item.icon" class="setting-icon" />
              <span>{{ item.label }}</span>
            </div>
            <Icon icon="ri:arrow-right-s-line" class="arrow-icon" />
          </div>
        </div>
      </div>

      <div class="setting-group logout-group">
        <div class="setting-list">
          <div class="setting-item logout-item" @click="handleLogout">
            <div class="setting-left">
              <Icon icon="ri:logout-box-line" class="setting-icon logout-icon" />
              <span>退出登录</span>
            </div>
            <Icon icon="ri:arrow-right-s-line" class="arrow-icon" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .profile-page {
    width: 100%;
    max-width: 800px;
    margin: 0 auto;
  }

  .profile-header {
    background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
    border-radius: 20px;
    padding: 32px;
    margin-bottom: 24px;
    box-shadow:
      0 4px 6px -1px rgba(0, 0, 0, 0.05),
      0 10px 20px -5px rgba(0, 0, 0, 0.08),
      0 0 0 1px rgba(0, 0, 0, 0.03),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  .user-card {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    flex-shrink: 0;
    box-shadow:
      0 4px 12px rgba(102, 126, 234, 0.35),
      0 2px 4px rgba(0, 0, 0, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
  }

  .avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-icon {
    font-size: 36px;
    color: white;
  }

  .user-info {
    flex: 1;
    min-width: 0;
  }

  .user-info h2 {
    font-size: 24px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .email {
    color: #6b7280;
    font-size: 14px;
    margin-bottom: 4px;
  }

  .join-date {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #9ca3af;
    font-size: 13px;
  }

  .settings-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .setting-group {
    background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
    border-radius: 16px;
    padding: 20px 24px;
    box-shadow:
      0 2px 4px rgba(0, 0, 0, 0.02),
      0 4px 8px rgba(0, 0, 0, 0.04),
      0 8px 16px rgba(0, 0, 0, 0.04),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
  }

  .setting-group:hover {
    box-shadow:
      0 4px 8px rgba(0, 0, 0, 0.04),
      0 8px 16px rgba(0, 0, 0, 0.06),
      0 16px 32px rgba(0, 0, 0, 0.06),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
    transform: translateY(-2px);
  }

  .group-title {
    font-size: 13px;
    font-weight: 600;
    color: #9ca3af;
    margin-bottom: 12px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  .setting-list {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    margin: 0 -16px;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .setting-item:hover {
    background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
    box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.05);
  }

  .logout-item:hover {
    background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
    box-shadow: inset 0 1px 2px rgba(239, 68, 68, 0.1);
  }

  .setting-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .setting-icon {
    font-size: 18px;
    color: #6b7280;
  }

  .logout-icon {
    color: #ef4444;
  }

  .logout-item span {
    color: #ef4444;
  }

  .setting-item span {
    font-size: 15px;
    color: #374151;
  }

  .arrow-icon {
    color: #d1d5db;
    font-size: 18px;
  }

  .logout-group {
    margin-top: 8px;
  }

  @media (max-width: 640px) {
    .user-card {
      flex-direction: column;
      text-align: center;
    }

    .user-info {
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .join-date {
      justify-content: center;
    }
  }
</style>