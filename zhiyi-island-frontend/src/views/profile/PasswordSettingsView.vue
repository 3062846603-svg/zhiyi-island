<script setup>
  import { ref, computed, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Icon } from '@iconify/vue'
  import { ElTooltip } from 'element-plus'
  import { userApi } from '@/api/user'
  import { useAuthStore } from '@/stores/auth'
  import { useToastStore } from '@/stores/toast'
  import { ValidationRules } from '@/utils/validation'

  const router = useRouter()
  const authStore = useAuthStore()
  const toast = useToastStore()

  const step = ref(1)

  const code = ref('')
  const countdown = ref(0)
  const sendingCode = ref(false)

  const sliderVerified = ref(false)
  const sliderPosition = ref(-2)
  const isDragging = ref(false)
  const sliderTrackRef = ref(null)
  const startX = ref(0)

  const newPassword = ref('')
  const confirmPassword = ref('')
  const showNewPassword = ref(false)
  const showConfirmPassword = ref(false)

  const isLoading = ref(false)

  /** 失焦状态 */
  const newPasswordBlurred = ref(false)
  const confirmPasswordBlurred = ref(false)

  const currentEmail = computed(() => authStore.userInfo?.email || '')

  const maskedEmail = computed(() => {
    const email = currentEmail.value
    if (!email) return ''
    const [localPart, domain] = email.split('@')
    if (!domain) return email
    const maskedLocal = localPart.length > 6
      ? localPart.slice(0, 3) + '****' + localPart.slice(-3)
      : localPart[0] + '****' + localPart.slice(-1)
    return `${maskedLocal}@${domain}`
  })

  const passwordValid = computed(() => {
    return newPassword.value.length >= ValidationRules.password.minLength && newPassword.value.length <= ValidationRules.password.maxLength
  })

  const passwordInvalid = computed(() => {
    return newPasswordBlurred.value && newPassword.value && !passwordValid.value
  })

  const passwordMatch = computed(() => {
    return newPassword.value === confirmPassword.value && confirmPassword.value !== ''
  })

  const passwordMismatch = computed(() => {
    return confirmPasswordBlurred.value && confirmPassword.value && !passwordMatch.value
  })

  const canSendCode = computed(() => {
    return sliderVerified.value && countdown.value === 0 && !sendingCode.value
  })

  const canVerifyEmail = computed(() => {
    return code.value.length === 6 && !isLoading.value
  })

  const canSetNew = computed(() => {
    return passwordValid.value && passwordMatch.value
  })

  const passwordStrength = computed(() => {
    const pwd = newPassword.value
    if (!pwd) return 0
    let score = 0
    if (pwd.length >= 6) score++
    if (pwd.length >= 10) score++
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++
    if (/\d/.test(pwd)) score++
    if (/[!@#$%^&*(),.?":{}|<>]/.test(pwd)) score++
    return Math.min(score, 4)
  })

  const strengthText = computed(() => {
    const texts = ['', '弱', '中', '强', '非常强']
    return texts[passwordStrength.value]
  })

  const hasMinLength = computed(() => newPassword.value.length >= 6)
  const hasGoodLength = computed(() => newPassword.value.length >= 10)
  const hasLetterAndNumber = computed(() => /[a-zA-Z]/.test(newPassword.value) && /\d/.test(newPassword.value))
  const hasUpperAndLower = computed(() => /[a-z]/.test(newPassword.value) && /[A-Z]/.test(newPassword.value))
  const hasSpecialChar = computed(() => /[!@#$%^&*(),.?":{}|<>]/.test(newPassword.value))

  const handleSliderMouseDown = (e) => {
    if (sliderVerified.value) return
    isDragging.value = true
    startX.value = e.clientX - sliderPosition.value
  }

  const handleTouchStart = (e) => {
    if (sliderVerified.value) return
    isDragging.value = true
    startX.value = e.touches[0].clientX - sliderPosition.value
  }

  const handleSliderMouseMove = (e) => {
    if (!isDragging.value || sliderVerified.value) return
    const track = sliderTrackRef.value
    if (!track) return
    const rect = track.getBoundingClientRect()
    const maxPosition = rect.width - 40
    let newPosition = e.clientX - startX.value
    newPosition = Math.max(-2, Math.min(newPosition, maxPosition))
    sliderPosition.value = newPosition
  }

  const handleTouchMove = (e) => {
    if (!isDragging.value || sliderVerified.value) return
    const track = sliderTrackRef.value
    if (!track) return
    const rect = track.getBoundingClientRect()
    const maxPosition = rect.width - 40
    let newPosition = e.touches[0].clientX - startX.value
    newPosition = Math.max(-2, Math.min(newPosition, maxPosition))
    sliderPosition.value = newPosition
  }

  const handleSliderMouseUp = () => {
    if (!isDragging.value) return
    isDragging.value = false
    const track = sliderTrackRef.value
    if (!track) return
    const rect = track.getBoundingClientRect()
    const maxPosition = rect.width - 40
    if (sliderPosition.value >= maxPosition - 10) {
      sliderVerified.value = true
      sliderPosition.value = maxPosition
    } else {
      sliderPosition.value = -2
    }
  }

  const handleTouchEnd = () => {
    handleSliderMouseUp()
  }

  const resetSlider = () => {
    sliderVerified.value = false
    sliderPosition.value = -2
  }

  const sendCode = async () => {
    if (!canSendCode.value) return

    sendingCode.value = true
    try {
      const result = await userApi.sendCode({ email: currentEmail.value })
      if (result.code === 200) {
        toast.success('验证码已发送')
        countdown.value = 60
        const timer = setInterval(() => {
          countdown.value--
          if (countdown.value <= 0) {
            clearInterval(timer)
            resetSlider()
          }
        }, 1000)
      } else {
        toast.error(result.message || '发送失败')
        resetSlider()
      }
    } catch (e) {
      toast.error('发送验证码失败')
      resetSlider()
    } finally {
      sendingCode.value = false
    }
  }

  const verifyEmail = async () => {
    if (!canVerifyEmail.value) {
      toast.warning('请输入6位验证码')
      return
    }

    isLoading.value = true
    try {
      const result = await userApi.verifyEmailCode({
        email: currentEmail.value,
        code: code.value,
      })
      if (result.code === 200) {
        toast.success('验证成功')
        step.value = 2
      } else {
        toast.error(result.message || '验证失败')
      }
    } catch (e) {
      toast.error('验证失败')
    } finally {
      isLoading.value = false
    }
  }

  const updatePassword = async () => {
    if (!canSetNew.value) {
      if (!passwordValid.value) {
        toast.warning('密码长度需在6-20位之间')
      } else if (!passwordMatch.value) {
        toast.warning('两次输入的密码不一致')
      }
      return
    }

    isLoading.value = true
    try {
      const result = await userApi.updatePasswordByCode({
        email: currentEmail.value,
        code: code.value,
        newPassword: newPassword.value,
      })
      if (result.code === 200) {
        toast.success('密码修改成功')
        step.value = 3
      } else {
        toast.error(result.message || '密码修改失败')
      }
    } catch (e) {
      toast.error('密码修改失败')
    } finally {
      isLoading.value = false
    }
  }

  const handleLogout = async () => {
    await authStore.logout()
    router.push('/login')
  }

  const goBack = () => {
    router.back()
  }

  const goBackToStep1 = () => {
    step.value = 1
    code.value = ''
    resetSlider()
  }

  onMounted(() => {
    document.addEventListener('mousemove', handleSliderMouseMove)
    document.addEventListener('mouseup', handleSliderMouseUp)
    document.addEventListener('touchmove', handleTouchMove)
    document.addEventListener('touchend', handleTouchEnd)
  })
</script>

<template>
  <div class="password-settings-page">
    <div v-if="isDragging" class="drag-overlay" @mouseup="handleSliderMouseUp" @touchend="handleTouchEnd"></div>

    <Transition name="form-fade" mode="out-in">
      <div v-if="step === 1" key="step1" class="form-content">
        <div class="page-header">
          <button type="button" class="btn btn-secondary" @click="goBack">
            <Icon icon="ri:arrow-left-line" />
            返回
          </button>
        </div>

        <div class="form-header">
          <h1>修改密码</h1>
          <p>请先验证邮箱身份</p>
        </div>

        <form class="password-form" @submit.prevent="verifyEmail">
          <div class="current-email-box">
            <Icon icon="ri:mail-line" />
            <div class="info">
              <span class="label">当前绑定邮箱</span>
              <span class="value">{{ maskedEmail }}</span>
            </div>
          </div>

          <div class="input-group">
            <label>验证码</label>
            <div class="input-wrapper">
              <Icon icon="ri:shield-check-line" class="input-icon" />
              <input v-model="code" type="text" placeholder="请输入验证码" maxlength="6" />
            </div>
            <div class="verify-row" :class="{ 'is-dragging': isDragging }">
              <div class="slider-verify-wrapper" v-if="!sliderVerified && countdown === 0">
                <div class="slider-track" ref="sliderTrackRef">
                  <div class="slider-fill" :style="{ width: Math.max(0, sliderPosition + 2) + 'px' }"></div>
                  <div class="slider-thumb" :class="{ verified: sliderVerified }"
                    :style="{ left: sliderPosition + 'px' }" @mousedown="handleSliderMouseDown"
                    @touchstart="handleTouchStart">
                    <Icon v-if="sliderVerified" icon="ri:check-line" />
                    <Icon v-else icon="ri:arrow-right-double-line" />
                  </div>
                  <span class="slider-hint" v-if="!sliderVerified && sliderPosition === -2">
                    向右滑动验证
                  </span>
                </div>
              </div>
              <div class="verify-success-inline" v-if="sliderVerified">
                <Icon icon="ri:checkbox-circle-fill" />
                <span>已验证</span>
              </div>
              <button type="button" class="btn btn-sm btn-ghost" :disabled="!canSendCode" @click="sendCode">
                <Icon v-if="sendingCode" icon="ri:loader-4-line" class="spin" />
                <span v-else>{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</span>
              </button>
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-lg" :disabled="!canVerifyEmail || isLoading">
            <Icon v-if="isLoading" icon="ri:loader-4-line" class="spin" />
            <span v-else>下一步</span>
          </button>
        </form>
      </div>

      <div v-else-if="step === 2" key="step2" class="form-content step2-content">
        <div class="page-header">
          <button type="button" class="btn btn-secondary" @click="goBackToStep1">
            <Icon icon="ri:arrow-left-line" />
            返回
          </button>
        </div>

        <div class="form-header">
          <h1>设置新密码</h1>
          <p>请输入新的密码</p>
        </div>

        <div class="step2-layout">
          <form class="password-form" @submit.prevent="updatePassword">
            <div class="input-group">
              <label>新密码</label>
              <ElTooltip :visible="passwordInvalid" :content="ValidationRules.password.messages.minLength"
                effect="light" placement="top">
                <template #default>
                  <div class="input-wrapper" :class="{
                      'input-success': newPasswordBlurred && passwordValid,
                      'input-error': passwordInvalid,
                    }">
                    <Icon icon="ri:lock-password-line" class="input-icon" />
                    <input v-model="newPassword" :type="showNewPassword ? 'text' : 'password'"
                      placeholder="请输入新密码（6-20位）" @blur="newPasswordBlurred = true" />
                    <button type="button" class="toggle-password" @click="showNewPassword = !showNewPassword">
                      <Icon :icon="showNewPassword ? 'ri:eye-off-line' : 'ri:eye-line'" />
                    </button>
                  </div>
                </template>
              </ElTooltip>
            </div>

            <div class="input-group">
              <label>确认新密码</label>
              <ElTooltip :visible="passwordMismatch" content="两次密码输入不一致" effect="light" placement="top">
                <template #default>
                  <div class="input-wrapper" :class="{
                      'input-success': confirmPasswordBlurred && passwordMatch,
                      'input-error': passwordMismatch,
                    }">
                    <Icon icon="ri:lock-2-line" class="input-icon" />
                    <input v-model="confirmPassword" :type="showConfirmPassword ? 'text' : 'password'"
                      placeholder="请再次输入新密码" @blur="confirmPasswordBlurred = true" />
                    <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
                      <Icon :icon="showConfirmPassword ? 'ri:eye-off-line' : 'ri:eye-line'" />
                    </button>
                  </div>
                </template>
              </ElTooltip>
            </div>

            <div class="password-strength" v-if="newPassword">
              <div class="strength-label">密码强度</div>
              <div class="strength-bars">
                <div class="strength-bar" :class="{ active: passwordStrength >= 1 }"></div>
                <div class="strength-bar" :class="{ active: passwordStrength >= 2 }"></div>
                <div class="strength-bar" :class="{ active: passwordStrength >= 3 }"></div>
                <div class="strength-bar" :class="{ active: passwordStrength >= 4 }"></div>
              </div>
              <span class="strength-text">{{ strengthText }}</span>
            </div>

            <button type="submit" class="btn btn-primary btn-lg" :disabled="!canSetNew || isLoading">
              <Icon v-if="isLoading" icon="ri:loader-4-line" class="spin" />
              <span v-else>确认修改</span>
            </button>
          </form>

          <div class="tips-panel">
            <div class="tips-header">
              <Icon icon="ri:lightbulb-line" />
              <span>密码设置建议</span>
            </div>
            <ul class="tips-list">
              <li :class="{ active: hasMinLength }">
                <Icon :icon="hasMinLength ? 'ri:checkbox-circle-fill' : 'ri:checkbox-blank-circle-line'" />
                <span>密码长度至少6位</span>
              </li>
              <li :class="{ active: hasGoodLength }">
                <Icon :icon="hasGoodLength ? 'ri:checkbox-circle-fill' : 'ri:checkbox-blank-circle-line'" />
                <span>密码长度10位以上更安全</span>
              </li>
              <li :class="{ active: hasLetterAndNumber }">
                <Icon :icon="hasLetterAndNumber ? 'ri:checkbox-circle-fill' : 'ri:checkbox-blank-circle-line'" />
                <span>建议包含字母和数字</span>
              </li>
              <li :class="{ active: hasUpperAndLower }">
                <Icon :icon="hasUpperAndLower ? 'ri:checkbox-circle-fill' : 'ri:checkbox-blank-circle-line'" />
                <span>建议包含大小写字母</span>
              </li>
              <li :class="{ active: hasSpecialChar }">
                <Icon :icon="hasSpecialChar ? 'ri:checkbox-circle-fill' : 'ri:checkbox-blank-circle-line'" />
                <span>建议包含特殊字符</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div v-else key="success" class="form-content success-content">
        <div class="success-icon">
          <Icon icon="ri:checkbox-circle-fill" />
        </div>
        <h2>密码修改成功</h2>
        <p class="success-text">您的密码已成功修改，请使用新密码重新登录</p>
        <div class="success-actions">
          <button type="button" class="btn btn-primary" @click="handleLogout">
            <Icon icon="ri:logout-box-line" />
            重新登录
          </button>
          <button type="button" class="btn btn-secondary" @click="goBack">
            返回个人中心
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
  .password-settings-page {
    --spacing-xs: 8px;
    --spacing-sm: 12px;
    --spacing-md: 16px;
    --spacing-lg: 24px;
    --spacing-xl: 32px;
    --spacing-xxl: 40px;
    --transition-fast: 0.2s;

    width: 100%;
    display: flex;
    justify-content: center;
    padding: var(--spacing-xl) var(--spacing-md);
  }

  .drag-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    cursor: grabbing;
  }

  .form-content {
    width: 100%;
    max-width: 400px;
  }

  .step2-content {
    max-width: 720px;
  }

  .page-header {
    margin-bottom: var(--spacing-xl);
  }

  .back-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    color: #374151;
    font-size: 14px;
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .back-btn:hover {
    background: #f9fafb;
    border-color: var(--primary-color, #122E8A);
    color: var(--primary-color, #122E8A);
  }

  .form-header {
    text-align: center;
    margin-bottom: var(--spacing-xxl);
  }

  .form-header h1 {
    font-size: 28px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: var(--spacing-sm);
  }

  .form-header p {
    font-size: 14px;
    color: #6b7280;
  }

  .current-email-box {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
    padding: var(--spacing-lg);
    background: #f9fafb;
    border-radius: 12px;
    margin-bottom: var(--spacing-xxl);
  }

  .current-email-box .iconify {
    font-size: 24px;
    color: var(--primary-color, #122E8A);
  }

  .current-email-box .info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .current-email-box .label {
    font-size: 12px;
    color: #9ca3af;
  }

  .current-email-box .value {
    font-size: 15px;
    font-weight: 500;
    color: #1f2937;
  }

  .password-form {
    display: flex;
    flex-direction: column;
    gap: 0;
  }

  .input-group {
    margin-bottom: var(--spacing-xl);
  }

  .input-group label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    margin-bottom: var(--spacing-sm);
  }

  .input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
  }

  .input-wrapper.input-error input {
    border-color: #ef4444;
  }

  .input-wrapper.input-success input {
    border-color: #22c55e;
  }

  .input-icon {
    position: absolute;
    left: 14px;
    font-size: 18px;
    color: #9ca3af;
    z-index: 1;
  }

  .input-wrapper input {
    width: 100%;
    padding: 12px 14px 12px 44px;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    font-size: 14px;
    color: #374151;
    transition: all var(--transition-fast);
    outline: none;
  }

  .input-wrapper input:focus {
    border-color: var(--primary-color, #122E8A);
    box-shadow: 0 0 0 3px rgba(18, 46, 138, 0.1);
  }

  .input-wrapper input::placeholder {
    color: #9ca3af;
  }

  .toggle-password {
    position: absolute;
    right: 14px;
    background: none;
    border: none;
    color: #9ca3af;
    cursor: pointer;
    font-size: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    z-index: 1;
  }

  .toggle-password:hover {
    color: #6b7280;
  }

  .field-hint {
    display: block;
    font-size: 12px;
    margin-top: 6px;
    line-height: 1.4;
  }

  .field-hint.error {
    color: #ef4444;
  }

  .field-hint.success {
    color: #22c55e;
  }

  .verify-row {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    margin-top: var(--spacing-sm);
    min-height: 40px;
    width: 100%;
  }

  .verify-row.is-dragging .inline-code-btn {
    pointer-events: none;
  }

  .slider-verify-wrapper {
    flex: 1;
    min-width: 0;
  }

  .slider-track {
    height: 40px;
    background: #f3f4f6;
    border-radius: 10px;
    position: relative;
    overflow: hidden;
    border: 1px solid #e5e7eb;
  }

  .slider-fill {
    position: absolute;
    left: 0;
    top: -1px;
    height: 42px;
    background: linear-gradient(135deg,
        var(--primary-color, #122E8A) 0%,
        var(--secondary-color, #122E8A) 100%);
    opacity: 0.3;
    transition: none;
  }

  .slider-thumb {
    position: absolute;
    top: -1px;
    left: -2px;
    width: 44px;
    height: 42px;
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: grab;
    font-size: 18px;
    color: #9ca3af;
    transition: border-color 0.2s, color 0.2s, background 0.2s;
    user-select: none;
    box-shadow: 0 2px 4px rgba(18, 46, 138, 0.1);
    touch-action: none;
  }

  .slider-thumb:hover {
    color: var(--primary-color, #122E8A);
  }

  .slider-thumb.verified {
    background: linear-gradient(135deg,
        var(--primary-color, #122E8A) 0%,
        var(--secondary-color, #122E8A) 100%);
    border-color: transparent;
    color: white;
  }

  .slider-hint {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    font-size: 13px;
    color: #9ca3af;
    pointer-events: none;
    white-space: nowrap;
  }

  .verify-success-inline {
    flex: 1;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    height: 40px;
    padding: 0 14px;
    background: #ecfdf5;
    border-radius: 8px;
    font-size: 13px;
    color: #10b981;
  }

  .verify-success-inline .iconify {
    font-size: 16px;
  }

  .inline-code-btn {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    padding: 0;
    width: 90px;
    height: 40px;
    background: transparent;
    border: 1px solid var(--primary-color, #122E8A);
    border-radius: 6px;
    color: var(--primary-color, #122E8A);
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all var(--transition-fast);
  }

  .inline-code-btn:hover:not(:disabled) {
    background: var(--primary-color, #122E8A);
    color: #ffffff;
  }

  .inline-code-btn:disabled {
    border-color: #d1d5db;
    color: #9ca3af;
    cursor: not-allowed;
  }

  .step2-layout {
    display: flex;
    gap: var(--spacing-xl);
    align-items: flex-start;
  }

  .step2-layout .password-form {
    flex: 1;
    min-width: 0;
  }

  .tips-panel {
    width: 240px;
    flex-shrink: 0;
    background: #f9fafb;
    border-radius: 12px;
    padding: var(--spacing-lg);
  }

  .tips-header {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    font-size: 15px;
    font-weight: 600;
    color: #374151;
    margin-bottom: var(--spacing-md);
  }

  .tips-header .iconify {
    font-size: 20px;
    color: #f59e0b;
  }

  .tips-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .tips-list li {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    font-size: 13px;
    color: #6b7280;
    transition: all var(--transition-fast);
  }

  .tips-list li .iconify {
    font-size: 16px;
    color: #d1d5db;
    flex-shrink: 0;
    margin-top: 1px;
  }

  .tips-list li.active {
    color: #10b981;
  }

  .tips-list li.active .iconify {
    color: #10b981;
  }

  .password-strength {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-md);
    background: #f9fafb;
    border-radius: 10px;
    margin-bottom: var(--spacing-xl);
  }

  .strength-label {
    font-size: 14px;
    color: #6b7280;
  }

  .strength-bars {
    display: flex;
    gap: 6px;
    flex: 1;
  }

  .strength-bar {
    flex: 1;
    height: 4px;
    background: #e5e7eb;
    border-radius: 2px;
    transition: all 0.3s;
  }

  .strength-bar:nth-child(1).active {
    background: #ef4444;
  }

  .strength-bar:nth-child(2).active {
    background: #f59e0b;
  }

  .strength-bar:nth-child(3).active {
    background: #22c55e;
  }

  .strength-bar:nth-child(4).active {
    background: #10b981;
  }

  .strength-text {
    font-size: 13px;
    font-weight: 500;
    color: #6b7280;
    min-width: 52px;
    text-align: right;
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    padding: 0 14px;
    background: linear-gradient(135deg,
        var(--primary-color, #122E8A) 0%,
        var(--secondary-color, #122E8A) 100%);
    border: none;
    border-radius: 10px;
    color: white;
    font-size: 16px;
    font-weight: 500;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    transition: all var(--transition-fast);
  }

  .submit-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(18, 46, 138, 0.4);
  }

  .submit-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
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

  .success-content {
    text-align: center;
    padding: var(--spacing-xxl) 0;
  }

  .success-icon {
    width: 80px;
    height: 80px;
    margin: 0 auto var(--spacing-xxl);
    background: #10b981;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    color: white;
  }

  .success-content h2 {
    font-size: 26px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: var(--spacing-md);
  }

  .success-text {
    font-size: 14px;
    color: #6b7280;
    margin-bottom: var(--spacing-xxl);
    line-height: 1.6;
  }

  .success-actions {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: center;
  }

  .form-fade-enter-active,
  .form-fade-leave-active {
    transition: opacity 0.3s ease;
  }

  .form-fade-enter-from,
  .form-fade-leave-to {
    opacity: 0;
  }

  @media (max-width: 768px) {
    .step2-content {
      max-width: 400px;
    }

    .step2-layout {
      flex-direction: column;
    }

    .tips-panel {
      width: 100%;
    }
  }

  @media (max-width: 480px) {
    .password-settings-page {
      padding: var(--spacing-lg) var(--spacing-md);
    }

    .form-header h1 {
      font-size: 24px;
    }
  }
</style>