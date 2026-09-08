<script setup>
  import { ref, computed, onMounted, onUnmounted } from 'vue'
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

  const currentEmail = ref('')
  const maskedCurrentEmail = computed(() => {
    if (!currentEmail.value) return '未绑定'
    const [localPart, domain] = currentEmail.value.split('@')
    const masked = localPart.length > 6
      ? localPart.slice(0, 3) + '****' + localPart.slice(-3)
      : localPart[0] + '****' + localPart.slice(-1)
    return masked + '@' + domain
  })

  const step = ref(1)

  const currentCode = ref('')
  const currentCountdown = ref(0)
  let currentCountdownTimer = null

  const newEmail = ref('')
  const newCode = ref('')
  const newCountdown = ref(0)
  let newCountdownTimer = null

  const sliderVerified = ref(false)
  const sliderPosition = ref(-2)
  const isDragging = ref(false)
  const sliderStartX = ref(0)
  const sliderTrackRef = ref(null)
  const currentSliderTrackRef = ref(null)

  const isLoading = ref(false)
  const sendingCode = ref(false)

  /** 失焦状态 */
  const newEmailBlurred = ref(false)

  const getSliderWidth = (ref) => {
    return ref?.value ? ref.value.offsetWidth : 280
  }

  const currentEmailValid = computed(() => {
    return !!currentEmail.value
  })

  const newEmailValid = computed(() => {
    return ValidationRules.email.pattern.test(newEmail.value)
  })

  const newEmailInvalid = computed(() => {
    return newEmailBlurred.value && newEmail.value && !ValidationRules.email.pattern.test(newEmail.value)
  })

  const canSendCurrentCode = computed(() => {
    return currentEmailValid.value && sliderVerified.value && currentCountdown.value === 0 && !sendingCode.value
  })

  const canSendNewCode = computed(() => {
    return newEmailValid.value && sliderVerified.value && newCountdown.value === 0 && !sendingCode.value
  })

  const canVerifyCurrent = computed(() => {
    return currentCode.value.length >= 4
  })

  const canBindNew = computed(() => {
    return newEmailValid.value && newCode.value.length >= 4
  })

  const handleSliderMouseDown = (e) => {
    if (sliderVerified.value) return
    isDragging.value = true
    sliderStartX.value = e.clientX - sliderPosition.value
  }

  const handleSliderMouseMove = (e) => {
    if (!isDragging.value) return
    const trackRef = step.value === 1 ? currentSliderTrackRef : sliderTrackRef
    const width = getSliderWidth(trackRef)
    const newPosition = e.clientX - sliderStartX.value
    sliderPosition.value = Math.max(-2, Math.min(newPosition, width - 44))
  }

  const handleSliderMouseUp = () => {
    if (!isDragging.value) return
    isDragging.value = false

    const trackRef = step.value === 1 ? currentSliderTrackRef : sliderTrackRef
    const width = getSliderWidth(trackRef)

    if (sliderPosition.value >= width - 70) {
      sliderVerified.value = true
      sliderPosition.value = width - 44
    } else {
      sliderPosition.value = -2
    }
  }

  const handleTouchStart = (e) => {
    if (sliderVerified.value) return
    isDragging.value = true
    sliderStartX.value = e.touches[0].clientX - sliderPosition.value
  }

  const handleTouchMove = (e) => {
    if (!isDragging.value) return
    e.preventDefault()
    const trackRef = step.value === 1 ? currentSliderTrackRef : sliderTrackRef
    const width = getSliderWidth(trackRef)
    const newPosition = e.touches[0].clientX - sliderStartX.value
    sliderPosition.value = Math.max(-2, Math.min(newPosition, width - 44))
  }

  const handleTouchEnd = () => {
    if (!isDragging.value) return
    isDragging.value = false

    const trackRef = step.value === 1 ? currentSliderTrackRef : sliderTrackRef
    const width = getSliderWidth(trackRef)

    if (sliderPosition.value >= width - 70) {
      sliderVerified.value = true
      sliderPosition.value = width - 44
    } else {
      sliderPosition.value = -2
    }
  }

  const resetSlider = () => {
    sliderVerified.value = false
    sliderPosition.value = -2
  }

  const sendCurrentCode = async () => {
    if (!canSendCurrentCode.value) return

    sendingCode.value = true
    try {
      const result = await userApi.sendEmailCode(currentEmail.value)
      if (result.code === 200) {
        toast.success('验证码已发送到当前邮箱')
        currentCountdown.value = 60
        currentCountdownTimer = setInterval(() => {
          currentCountdown.value--
          if (currentCountdown.value <= 0) {
            clearInterval(currentCountdownTimer)
          }
        }, 1000)
      } else {
        toast.error(result.message || '发送验证码失败')
      }
    } catch (e) {
      toast.error('发送验证码失败')
    } finally {
      sendingCode.value = false
    }
  }

  const sendNewCode = async () => {
    if (!canSendNewCode.value) return

    sendingCode.value = true
    try {
      const result = await userApi.sendEmailCode(newEmail.value)
      if (result.code === 200) {
        toast.success('验证码已发送到新邮箱')
        newCountdown.value = 60
        newCountdownTimer = setInterval(() => {
          newCountdown.value--
          if (newCountdown.value <= 0) {
            clearInterval(newCountdownTimer)
          }
        }, 1000)
      } else {
        toast.error(result.message || '发送验证码失败')
      }
    } catch (e) {
      toast.error('发送验证码失败')
    } finally {
      sendingCode.value = false
    }
  }

  const verifyCurrentEmail = async () => {
    if (!canVerifyCurrent.value) {
      toast.warning('请输入验证码')
      return
    }

    isLoading.value = true
    try {
      const result = await userApi.verifyEmailCode({
        email: currentEmail.value,
        code: currentCode.value,
      })
      if (result.code === 200) {
        toast.success('验证成功')
        resetSlider()
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

  const bindNewEmail = async () => {
    if (!canBindNew.value) {
      if (!newEmailValid.value) {
        toast.warning('请输入有效的邮箱地址')
      } else {
        toast.warning('请输入验证码')
      }
      return
    }

    isLoading.value = true
    try {
      const result = await userApi.updateEmail({
        email: newEmail.value,
        code: newCode.value,
      })
      if (result.code === 200) {
        currentEmail.value = newEmail.value
        await authStore.fetchUserInfo()
        step.value = 3
      } else {
        toast.error(result.message || '换绑失败')
      }
    } catch (e) {
      toast.error('换绑失败')
    } finally {
      isLoading.value = false
    }
  }

  const goBack = () => {
    router.back()
  }

  const goBackToStep1 = () => {
    resetSlider()
    step.value = 1
  }

  onMounted(() => {
    document.addEventListener('mousemove', handleSliderMouseMove)
    document.addEventListener('mouseup', handleSliderMouseUp)
    document.addEventListener('touchmove', handleTouchMove)
    document.addEventListener('touchend', handleTouchEnd)

    if (authStore.userInfo) {
      currentEmail.value = authStore.userInfo.email || ''
    }
  })

  onUnmounted(() => {
    document.removeEventListener('mousemove', handleSliderMouseMove)
    document.removeEventListener('mouseup', handleSliderMouseUp)
    document.removeEventListener('touchmove', handleTouchMove)
    document.removeEventListener('touchend', handleTouchEnd)
    if (currentCountdownTimer) clearInterval(currentCountdownTimer)
    if (newCountdownTimer) clearInterval(newCountdownTimer)
  })
</script>

<template>
  <div class="email-bind-page">
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
          <h1>换绑邮箱</h1>
          <p>请先验证当前邮箱身份</p>
        </div>

        <form class="email-form" @submit.prevent="verifyCurrentEmail">
          <div class="current-email-box">
            <Icon icon="ri:mail-line" />
            <div class="info">
              <span class="label">当前绑定邮箱</span>
              <span class="value">{{ maskedCurrentEmail }}</span>
            </div>
          </div>

          <div class="input-group">
            <label>验证码</label>
            <div class="input-wrapper">
              <Icon icon="ri:shield-check-line" class="input-icon" />
              <input v-model="currentCode" type="text" placeholder="请输入验证码" maxlength="6" />
            </div>
            <div class="verify-row" :class="{ 'is-dragging': isDragging }">
              <div class="slider-verify-wrapper" v-if="!sliderVerified && currentCountdown === 0">
                <div class="slider-track" ref="currentSliderTrackRef">
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
              <button type="button" class="btn btn-sm btn-ghost" :disabled="!canSendCurrentCode"
                @click="sendCurrentCode">
                <Icon v-if="sendingCode" icon="ri:loader-4-line" class="spin" />
                <span v-else>{{ currentCountdown > 0 ? `${currentCountdown}s` : '获取验证码' }}</span>
              </button>
            </div>
          </div>

          <button type="submit" class="btn btn-primary btn-lg" :disabled="!canVerifyCurrent || isLoading">
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
          <h1>绑定新邮箱</h1>
          <p>请输入新的邮箱地址</p>
        </div>

        <div class="step2-layout">
          <form class="email-form" @submit.prevent="bindNewEmail">
            <div class="input-group">
              <label>新邮箱地址</label>
              <ElTooltip :visible="newEmailInvalid" :content="ValidationRules.email.messages.pattern" effect="light"
                placement="top">
                <template #default>
                  <div class="input-wrapper" :class="{
                      'input-success': newEmailBlurred && newEmailValid,
                      'input-error': newEmailInvalid,
                    }">
                    <Icon icon="ri:mail-line" class="input-icon" />
                    <input v-model="newEmail" type="text" placeholder="请输入新邮箱地址" @blur="newEmailBlurred = true"
                      @input="resetSlider" />
                    <Icon v-if="newEmailBlurred && newEmailValid" icon="ri:checkbox-circle-fill"
                      class="status-icon success" />
                    <Icon v-else-if="newEmailInvalid" icon="ri:close-circle-fill" class="status-icon error" />
                  </div>
                </template>
              </ElTooltip>
            </div>

            <div class="input-group">
              <label>验证码</label>
              <div class="input-wrapper">
                <Icon icon="ri:shield-check-line" class="input-icon" />
                <input v-model="newCode" type="text" placeholder="请输入验证码" maxlength="6" />
              </div>
              <div class="verify-row" :class="{ 'is-dragging': isDragging }">
                <div class="slider-verify-wrapper" v-if="!sliderVerified && newCountdown === 0">
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
                <button type="button" class="btn btn-sm btn-ghost" :disabled="!canSendNewCode" @click="sendNewCode">
                  <Icon v-if="sendingCode" icon="ri:loader-4-line" class="spin" />
                  <span v-else>{{ newCountdown > 0 ? `${newCountdown}s` : '获取验证码' }}</span>
                </button>
              </div>
            </div>

            <button type="submit" class="btn btn-primary btn-lg" :disabled="!canBindNew || isLoading">
              <Icon v-if="isLoading" icon="ri:loader-4-line" class="spin" />
              <span v-else>确认换绑</span>
            </button>
          </form>

          <div class="warning-panel">
            <div class="warning-header">
              <Icon icon="ri:error-warning-line" />
              <span>注意事项</span>
            </div>
            <ul class="warning-list">
              <li>
                <Icon icon="ri:alert-line" />
                <span>换绑后，原邮箱将无法用于登录</span>
              </li>
              <li>
                <Icon icon="ri:alert-line" />
                <span>原邮箱将无法用于找回密码</span>
              </li>
              <li>
                <Icon icon="ri:shield-check-line" />
                <span>请确保新邮箱可用</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <div v-else key="success" class="form-content success-content">
        <div class="success-icon">
          <Icon icon="ri:checkbox-circle-fill" />
        </div>
        <h2>换绑成功</h2>
        <p class="success-text">您的邮箱已成功更换为</p>
        <p class="success-email">{{ currentEmail }}</p>
        <button type="button" class="btn btn-primary" @click="goBack">
          返回个人中心
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
  .email-bind-page {
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

  .email-form {
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

  .input-wrapper.input-success input {
    border-color: #22c55e;
  }

  .status-icon {
    position: absolute;
    right: 14px;
    font-size: 18px;
  }

  .status-icon.success {
    color: #22c55e;
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
    background: var(--bg-secondary, #ffffff);
    border: 1px solid var(--border-color, #e5e7eb);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: grab;
    font-size: 18px;
    color: var(--text-secondary, #9ca3af);
    transition: border-color var(--transition-fast), color var(--transition-fast), background var(--transition-fast);
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
    font-size: 12px;
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

  .step2-content {
    max-width: 720px;
  }

  .step2-layout {
    display: flex;
    gap: var(--spacing-xl);
    align-items: flex-start;
  }

  .step2-layout .email-form {
    flex: 1;
    min-width: 0;
  }

  .warning-panel {
    width: 240px;
    flex-shrink: 0;
    background: #fef3c7;
    border-radius: 12px;
    padding: var(--spacing-lg);
  }

  .warning-header {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    font-size: 15px;
    font-weight: 600;
    color: #92400e;
    margin-bottom: var(--spacing-md);
  }

  .warning-header .iconify {
    font-size: 20px;
    color: #f59e0b;
  }

  .warning-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: var(--spacing-sm);
  }

  .warning-list li {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    font-size: 13px;
    color: #92400e;
  }

  .warning-list li .iconify {
    font-size: 16px;
    color: #f59e0b;
    flex-shrink: 0;
    margin-top: 1px;
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
    margin-bottom: var(--spacing-xs);
  }

  .success-email {
    font-size: 17px;
    font-weight: 600;
    color: var(--primary-color, #122E8A);
    margin-bottom: var(--spacing-xxl);
  }

  .form-fade-enter-active,
  .form-fade-leave-active {
    transition: opacity 0.3s ease;
  }

  .form-fade-enter-from,
  .form-fade-leave-to {
    opacity: 0;
  }

  @media (max-width: 480px) {
    .email-bind-page {
      padding: var(--spacing-lg) var(--spacing-md);
    }

    .form-header h1 {
      font-size: 24px;
    }
  }
</style>