<!--
  个人资料设置页面
  用于修改用户昵称、头像等信息
-->
<script setup>
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { ElTooltip } from 'element-plus'
  import { useAuthStore } from '@/stores/auth'
  import { userApi } from '@/api/user'
  import { useToastStore } from '@/stores/toast'
  import { ValidationRules } from '@/utils/validation'

  const router = useRouter()
  const authStore = useAuthStore()
  const toast = useToastStore()

  /** 用户表单数据 */
  const userForm = ref({
    name: '',
    email: '',
    phone: '',
  })

  const avatarUrl = ref(null)
  const saving = ref(false)
  const loading = ref(false)

  /** 失焦状态 */
  const nicknameBlurred = ref(false)
  const phoneBlurred = ref(false)

  /** 验证 */
  const nicknameInvalid = computed(() => {
    return nicknameBlurred.value && userForm.value.name && userForm.value.name.length > ValidationRules.nickname.maxLength
  })

  const phoneRegex = /^1[3-9]\d{9}$/
  const phoneInvalid = computed(() => {
    return phoneBlurred.value && userForm.value.phone && !phoneRegex.test(userForm.value.phone)
  })

  const goBack = () => {
    router.back()
  }

  const loadUserInfo = async () => {
    loading.value = true
    try {
      if (authStore.userInfo) {
        userForm.value.name = authStore.userInfo.nickname || authStore.userInfo.username || ''
        userForm.value.email = authStore.userInfo.email || ''
        userForm.value.phone = authStore.userInfo.phone || ''
        avatarUrl.value = authStore.userInfo.avatar || null
      }
      const userInfo = await authStore.fetchUserInfo()
      if (userInfo) {
        userForm.value.name = userInfo.nickname || userInfo.username || ''
        userForm.value.email = userInfo.email || ''
        userForm.value.phone = userInfo.phone || ''
        avatarUrl.value = userInfo.avatar || null
      }
    } catch (e) {
      console.error('获取用户信息失败:', e)
    } finally {
      loading.value = false
    }
  }

  const handleAvatarChange = async () => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.onchange = async (e) => {
      const file = e.target.files[0]
      if (!file) return

      if (file.size > 5 * 1024 * 1024) {
        toast.warning('图片大小不能超过5MB')
        return
      }

      try {
        const result = await userApi.uploadAvatar(file)
        if (result.code === 200) {
          avatarUrl.value = result.data
          await authStore.fetchUserInfo()
          toast.success('头像上传成功')
        } else {
          toast.error(result.message || '上传失败')
        }
      } catch (e) {
        toast.error('上传失败: ' + (e.message || '未知错误'))
      }
    }
    input.click()
  }

  const saveProfile = async () => {
    if (userForm.value.name && userForm.value.name.length > ValidationRules.nickname.maxLength) {
      toast.warning(ValidationRules.nickname.messages.maxLength)
      return
    }

    if (userForm.value.phone && !phoneRegex.test(userForm.value.phone)) {
      toast.warning(ValidationRules.phone.messages.pattern)
      return
    }

    saving.value = true
    try {
      const result = await userApi.updateProfile({
        nickname: userForm.value.name,
        avatar: avatarUrl.value,
        phone: userForm.value.phone,
      })

      if (result.code === 200) {
        const newUserInfo = await authStore.fetchUserInfo()
        if (newUserInfo) {
          userForm.value.name = newUserInfo.nickname || ''
          userForm.value.phone = newUserInfo.phone || ''
          avatarUrl.value = newUserInfo.avatar || null
        }
        toast.success('保存成功')
      } else {
        toast.error(result.message || '保存失败')
      }
    } catch (e) {
      toast.error('保存失败: ' + (e.message || '未知错误'))
    } finally {
      saving.value = false
    }
  }

  onMounted(() => {
    loadUserInfo()
  })
</script>

<template>
  <div class="profile-settings-page">
    <div class="page-header">
      <button class="btn btn-secondary" @click="goBack">
        <Icon icon="ri:arrow-left-line" />
        返回
      </button>
      <h2>个人资料</h2>
      <button class="btn btn-primary" @click="saveProfile" :disabled="saving">
        <Icon icon="ri:save-line" />
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </div>

    <div class="settings-content" v-if="!loading">
      <div class="profile-card">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <div class="avatar">
              <img v-if="avatarUrl" :src="avatarUrl" alt="avatar" />
              <Icon v-else icon="ri:user-3-line" class="avatar-icon" />
            </div>
            <button class="btn btn-sm btn-icon btn-primary" @click="handleAvatarChange">
              <Icon icon="ri:camera-line" />
            </button>
          </div>
          <div class="avatar-info">
            <h3 class="user-name">{{ userForm.name || '用户' }}</h3>
            <p class="user-email">{{ userForm.email || '未设置邮箱' }}</p>
          </div>
        </div>
      </div>

      <div class="form-section">
        <h4 class="section-title">
          <Icon icon="ri:user-line" />
          基本信息
        </h4>
        <div class="form-group">
          <label class="form-label">用户名</label>
          <div class="readonly-field">
            <span class="readonly-value">{{ authStore.userInfo?.username || '未设置' }}</span>
            <span class="readonly-hint">登录凭证，不可修改</span>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">昵称</label>
          <ElTooltip :visible="nicknameInvalid" :content="ValidationRules.nickname.messages.maxLength" effect="light"
            placement="top">
            <template #default>
              <input v-model="userForm.name" type="text" class="form-input" :class="{ 'input-error': nicknameInvalid }"
                placeholder="输入昵称（最多20个字符）" @blur="nicknameBlurred = true" />
            </template>
          </ElTooltip>
        </div>
        <div class="form-group">
          <label class="form-label">邮箱</label>
          <div class="readonly-field">
            <span class="readonly-value">{{ userForm.email || '未设置' }}</span>
            <router-link to="/profile/email" class="change-link">更换</router-link>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">手机号</label>
          <ElTooltip :visible="phoneInvalid" :content="ValidationRules.phone.messages.pattern" effect="light"
            placement="top">
            <template #default>
              <input v-model="userForm.phone" type="tel" class="form-input" :class="{ 'input-error': phoneInvalid }"
                placeholder="输入手机号（可选）" @blur="phoneBlurred = true" />
            </template>
          </ElTooltip>
        </div>
      </div>
    </div>

    <div class="loading-container" v-else>
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>
  </div>
</template>

<style scoped>
  .profile-settings-page {
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

  .settings-content {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .profile-card {
    background: white;
    border-radius: 16px;
    padding: 32px;
  }

  .avatar-section {
    display: flex;
    align-items: center;
    gap: 24px;
  }

  .avatar-wrapper {
    position: relative;
    flex-shrink: 0;
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
  }

  .avatar-wrapper .btn {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
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

  .avatar-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .user-name {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
  }

  .user-email {
    font-size: 14px;
    color: #6b7280;
  }

  .form-section {
    background: white;
    border-radius: 16px;
    padding: 24px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .form-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .form-label {
    font-size: 13px;
    font-weight: 500;
    color: #6b7280;
  }

  .readonly-field {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    background: #f3f4f6;
    border-radius: 8px;
  }

  .readonly-value {
    font-size: 14px;
    color: #6b7280;
  }

  .readonly-hint {
    font-size: 12px;
    color: #9ca3af;
  }

  .change-link {
    font-size: 13px;
    color: var(--primary-color);
    text-decoration: none;
  }

  .change-link:hover {
    text-decoration: underline;
  }

  .form-input {
    padding: 10px 14px;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    font-size: 14px;
    color: #1f2937;
    transition: all 0.2s;
    background: #fafafa;
  }

  .form-input:focus {
    outline: none;
    border-color: var(--primary-color);
    background: white;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }

  .form-input.input-error {
    border-color: #ef4444;
  }

  .form-input.input-error:focus {
    border-color: #ef4444;
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
  }

  @media (max-width: 768px) {
    .avatar-section {
      flex-direction: column;
      text-align: center;
    }
  }

  .loading-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
    background: white;
    border-radius: 16px;
  }

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f3f4f6;
    border-top-color: var(--primary-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  @keyframes spin {
    to {
      transform: rotate(360deg);
    }
  }

  .loading-container p {
    color: #6b7280;
    font-size: 14px;
  }
</style>